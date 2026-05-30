package com.freedoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedoc.common.constants.Constants;
import com.freedoc.common.exception.BusinessException;
import com.freedoc.common.util.JwtUtil;
import com.freedoc.common.util.SnowflakeIdUtil;
import com.freedoc.dto.ShareCreateRequest;
import com.freedoc.dto.ShareVO;
import com.freedoc.entity.Directory;
import com.freedoc.entity.Doc;
import com.freedoc.entity.Project;
import com.freedoc.entity.ProjectUser;
import com.freedoc.entity.Share;
import com.freedoc.entity.User;
import com.freedoc.mapper.DirectoryMapper;
import com.freedoc.mapper.DocMapper;
import com.freedoc.mapper.ProjectMapper;
import com.freedoc.mapper.ProjectUserMapper;
import com.freedoc.mapper.ShareMapper;
import com.freedoc.mapper.UserMapper;
import com.freedoc.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share> implements ShareService {

    private final ProjectUserMapper projectUserMapper;
    private final ProjectMapper projectMapper;
    private final DocMapper docMapper;
    private final DirectoryMapper directoryMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public ShareVO createShare(ShareCreateRequest request, String userId) {
        String targetId = request.getTargetId();
        String targetType = request.getTargetType();

        // 校验权限：项目成员才能分享
        if (Constants.SHARE_TYPE_PROJECT.equals(targetType)) {
            checkProjectMember(targetId, userId);
        } else if (Constants.SHARE_TYPE_DOC.equals(targetType)) {
            Doc doc = docMapper.selectById(targetId);
            if (doc == null) {
                throw new BusinessException("error.doc.notFound");
            }
            checkProjectMember(doc.getProjectId(), userId);
        } else {
            throw new BusinessException("error.share.unsupportedType");
        }

        Share share = new Share();
        share.setShareId(SnowflakeIdUtil.nextId());
        // 使用UUID生成更安全的分享码
        share.setShareCode(java.util.UUID.randomUUID().toString().replace("-", ""));
        share.setTargetType(targetType);
        share.setTargetId(targetId);
        share.setCreateUser(userId);
        share.setCreateTime(LocalDateTime.now());

        // 密码加密
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            share.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // 有效期
        if (request.getExpireHours() != null && request.getExpireHours() > 0) {
            share.setExpireTime(LocalDateTime.now().plusHours(request.getExpireHours()));
        }

        save(share);

        return convertToVO(share, true);
    }

    @Override
    public void deleteShare(String shareId, String userId) {
        Share share = getById(shareId);
        if (share == null) {
            throw new BusinessException("error.share.notFound");
        }
        if (!share.getCreateUser().equals(userId)) {
            throw new BusinessException("error.share.noPermissionDelete");
        }
        removeById(shareId);
    }

    @Override
    public List<ShareVO> getShareList(String targetType, String targetId, String userId) {
        LambdaQueryWrapper<Share> wrapper = new LambdaQueryWrapper<Share>()
                .eq(Share::getCreateUser, userId);
        if (targetType != null) {
            wrapper.eq(Share::getTargetType, targetType);
        }
        if (targetId != null) {
            wrapper.eq(Share::getTargetId, targetId);
        }
        wrapper.orderByDesc(Share::getCreateTime);
        return list(wrapper).stream()
                .map(s -> convertToVO(s, true))
                .collect(Collectors.toList());
    }

    @Override
    public ShareVO getSharePublicInfo(String shareCode) {
        Share share = getByShareCode(shareCode);
        return convertToVO(share, false);
    }

    @Override
    public ShareVO verifyShare(String shareCode, String password) {
        Share share = getByShareCode(shareCode);

        // 检查有效期
        if (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("error.share.expired");
        }

        // 检查密码
        if (share.getPassword() != null && !share.getPassword().isEmpty()) {
            if (password == null || password.isEmpty() || !passwordEncoder.matches(password, share.getPassword())) {
                throw new BusinessException("error.share.passwordWrong");
            }
        }

        ShareVO vo = convertToVO(share, false);
        // 生成临时token
        vo.setShareCode(generateShareToken(shareCode));
        return vo;
    }

    @Override
    public Object getShareProjectContent(String shareCode, String shareToken) {
        Share share = validateAndGetShare(shareCode, shareToken);

        if (!Constants.SHARE_TYPE_PROJECT.equals(share.getTargetType())) {
            throw new BusinessException("error.share.notProjectType");
        }

        String projectId = share.getTargetId();
        Project project = getProjectById(projectId);
        if (project == null) {
            throw new BusinessException("error.project.notFound");
        }

        // 获取项目目录和文档
        List<Directory> directories = directoryMapper.selectList(
                new LambdaQueryWrapper<Directory>().eq(Directory::getProjectId, projectId));
        List<Doc> docs = docMapper.selectList(
                new LambdaQueryWrapper<Doc>().eq(Doc::getProjectId, projectId));

        Map<String, Object> result = new HashMap<>();
        result.put("project", project);
        result.put("directories", directories);
        result.put("docs", docs.stream().map(this::convertDocToPublic).collect(Collectors.toList()));
        return result;
    }

    @Override
    public Object getShareDocContent(String shareCode, String docId, String shareToken) {
        Share share = validateAndGetShare(shareCode, shareToken);

        Doc doc;
        if (Constants.SHARE_TYPE_DOC.equals(share.getTargetType())) {
            // 文档分享只能看该文档
            doc = docMapper.selectById(share.getTargetId());
        } else if (Constants.SHARE_TYPE_PROJECT.equals(share.getTargetType())) {
            // 项目分享可以看项目下任何文档
            doc = docMapper.selectById(docId);
            if (doc != null && !doc.getProjectId().equals(share.getTargetId())) {
                throw new BusinessException("error.share.docNotInProject");
            }
        } else {
            throw new BusinessException("error.share.unsupportedType");
        }

        if (doc == null) {
            throw new BusinessException("error.doc.notFound");
        }

        return convertDocToPublic(doc);
    }

    @Override
    public String generateShareToken(String shareCode) {
        // 使用JWT生成短期token，有效期5分钟
        Map<String, Object> claims = new HashMap<>();
        claims.put("shareCode", shareCode);
        claims.put("type", "shareToken");
        return jwtUtil.generateToken(shareCode, "share");
    }

    @Override
    public boolean validateShareToken(String shareCode, String shareToken) {
        if (shareToken == null || shareToken.isEmpty()) {
            return false;
        }
        try {
            String subject = jwtUtil.parseToken(shareToken).getSubject();
            return shareCode.equals(subject);
        } catch (Exception e) {
            return false;
        }
    }

    // ===== 私有方法 =====

    private Share getByShareCode(String shareCode) {
        Share share = getOne(new LambdaQueryWrapper<Share>()
                .eq(Share::getShareCode, shareCode));
        if (share == null) {
            throw new BusinessException("error.share.notFound");
        }
        return share;
    }

    private void checkProjectMember(String projectId, String userId) {
        ProjectUser projectUser = projectUserMapper.selectOne(
                new LambdaQueryWrapper<ProjectUser>()
                        .eq(ProjectUser::getProjectId, projectId)
                        .eq(ProjectUser::getUserId, userId));
        if (projectUser == null) {
            throw new BusinessException("error.share.noPermissionOperate");
        }
        // 只有OWNER或拥有读写权限的成员才能分享
        if (!Constants.OWNER.equals(projectUser.getType()) &&
                !Constants.PERMISSION_READ_WRITE.equals(projectUser.getPermission())) {
            throw new BusinessException("error.share.noPermissionShare");
        }
    }

    private Project getProjectById(String projectId) {
        return projectMapper.selectById(projectId);
    }

    private Share validateAndGetShare(String shareCode, String shareToken) {
        Share share = getByShareCode(shareCode);

        // 检查有效期
        if (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("error.share.expired");
        }

        boolean needPassword = share.getPassword() != null && !share.getPassword().isEmpty();

        // 有密码时必须校验临时token；无密码时无需校验
        if (needPassword && !validateShareToken(shareCode, shareToken)) {
            throw new BusinessException("error.share.invalidToken");
        }

        return share;
    }

    private ShareVO convertToVO(Share share, boolean includeLink) {
        ShareVO vo = new ShareVO();
        vo.setShareId(share.getShareId());
        vo.setShareCode(share.getShareCode());
        vo.setTargetType(share.getTargetType());
        vo.setTargetId(share.getTargetId());
        vo.setNeedPassword(share.getPassword() != null && !share.getPassword().isEmpty());
        vo.setIsExpired(share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now()));
        vo.setExpireTime(share.getExpireTime());
        vo.setCreateUser(share.getCreateUser());
        vo.setCreateTime(share.getCreateTime());

        // 填充目标名称
        if (Constants.SHARE_TYPE_PROJECT.equals(share.getTargetType())) {
            Project project = projectMapper.selectById(share.getTargetId());
            if (project != null) {
                vo.setProjectName(project.getProjectName());
                vo.setProjectDesc(project.getProjectDesc());
                vo.setProjectIcon(project.getProjectIcon());
            }
        } else if (Constants.SHARE_TYPE_DOC.equals(share.getTargetType())) {
            Doc doc = docMapper.selectById(share.getTargetId());
            if (doc != null) {
                vo.setDocTitle(doc.getDocTitle());
                vo.setDocIcon(doc.getDocIcon());
            }
        }

        // 填充分享人名称
        User user = userMapper.selectById(share.getCreateUser());
        if (user != null) {
            vo.setShareUserName(user.getUserName());
        }

        return vo;
    }

    private Map<String, Object> convertDocToPublic(Doc doc) {
        Map<String, Object> map = new HashMap<>();
        map.put("docId", doc.getDocId());
        map.put("docTitle", doc.getDocTitle());
        map.put("docIcon", doc.getDocIcon());
        map.put("docContent", doc.getDocContent());
        map.put("directoryId", doc.getDirectoryId());
        map.put("projectId", doc.getProjectId());
        map.put("createUser", doc.getCreateUser());
        map.put("createTime", doc.getCreateTime());
        map.put("updateUser", doc.getUpdateUser());
        map.put("updateTime", doc.getUpdateTime());
        
        // 填充用户名称，避免前端额外请求
        if (doc.getCreateUser() != null && !doc.getCreateUser().isEmpty()) {
            User createUser = userMapper.selectById(doc.getCreateUser());
            map.put("createUserName", createUser != null ? createUser.getUserName() : null);
        } else {
            map.put("createUserName", null);
        }
        
        if (doc.getUpdateUser() != null && !doc.getUpdateUser().isEmpty()) {
            User updateUser = userMapper.selectById(doc.getUpdateUser());
            map.put("updateUserName", updateUser != null ? updateUser.getUserName() : null);
        } else {
            map.put("updateUserName", null);
        }
        
        return map;
    }

}
