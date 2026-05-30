package com.freedoc.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedoc.common.exception.BusinessException;
import com.freedoc.common.util.SnowflakeIdUtil;
import com.freedoc.dto.DocCreateRequest;
import com.freedoc.dto.DocDetailVO;
import com.freedoc.dto.DocUpdateRequest;
import com.freedoc.entity.Directory;
import com.freedoc.entity.Doc;
import com.freedoc.entity.DocVersion;
import com.freedoc.entity.User;
import com.freedoc.mapper.DocMapper;
import com.freedoc.mapper.UserMapper;
import com.freedoc.service.DirectoryService;
import com.freedoc.service.DocService;
import com.freedoc.service.DocVersionService;
import com.freedoc.service.ProjectService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocServiceImpl extends ServiceImpl<DocMapper, Doc> implements DocService {

    private final DirectoryService directoryService;
    private final ProjectService projectService;
    private final UserMapper userMapper;
    @Lazy
    @Resource
    private DocVersionService docVersionService;
    private final com.freedoc.mapper.DocVersionMapper docVersionMapper;
    private final com.freedoc.mapper.CommentMapper commentMapper;

    @Override
    @Transactional
    public Doc createDoc(DocCreateRequest request, String userId) {
        Directory directory = directoryService.getById(request.getDirectoryId());
        if (directory == null) {
            throw new BusinessException("error.directory.notFound");
        }

        if (!projectService.hasPermission(directory.getProjectId(), userId, "rw")) {
            throw new BusinessException("error.doc.noPermissionCreate");
        }

        Doc doc = new Doc();
        doc.setDocId(SnowflakeIdUtil.nextId());
        doc.setDocTitle(request.getDocTitle());
        doc.setDocIcon(request.getDocIcon());
        doc.setDocContent(request.getDocContent());
        doc.setDirectoryId(directory.getId());
        doc.setTeamId(directory.getTeamId());
        doc.setProjectId(directory.getProjectId());
        doc.setCreateUser(userId);
        doc.setCreateTime(LocalDateTime.now());
        save(doc);

        if (request.getDocContent() != null && !request.getDocContent().isEmpty()) {
            createVersion(doc, userId);
        }

        return doc;
    }

    @Override
    @Transactional
    public Doc updateDoc(DocUpdateRequest request, String userId) {
        Doc doc = getById(request.getDocId());
        if (doc == null) {
            throw new BusinessException("error.doc.notFound");
        }

        Directory directory = directoryService.getById(doc.getDirectoryId());
        if (directory == null) {
            throw new BusinessException("error.directory.notFound");
        }

        if (!projectService.hasPermission(directory.getProjectId(), userId, "rw")) {
            throw new BusinessException("error.doc.noPermissionModify");
        }

        String oldContent = doc.getDocContent();
        String newContent = request.getDocContent() != null ? request.getDocContent() : oldContent;

        if (request.getDocTitle() != null) {
            doc.setDocTitle(request.getDocTitle());
        }
        if (request.getDocIcon() != null) {
            doc.setDocIcon(request.getDocIcon());
        }
        if (request.getDocContent() != null) {
            doc.setDocContent(request.getDocContent());
        }
        if (request.getDirectoryId() != null) {
            Directory newDir = directoryService.getById(request.getDirectoryId());
            if (newDir == null) {
                throw new BusinessException("error.doc.targetDirNotFound");
            }
            doc.setDirectoryId(newDir.getId());
        }

        doc.setUpdateUser(userId);
        doc.setUpdateTime(LocalDateTime.now());

        boolean shouldUpdateCurrentVersion = request.getUpdateCurrentVersion() != null && request.getUpdateCurrentVersion();
        boolean shouldCreateVersion = request.getCreateVersion() == null || request.getCreateVersion();

        if (shouldUpdateCurrentVersion) {
            updateCurrentVersion(doc, userId);
        } else if (shouldCreateVersion) {
            String newHash = DigestUtil.sha256Hex(newContent);
            DocVersion lastVersion = docVersionService.getLatestVersion(doc.getDocId());
            String lastVersionHash = lastVersion != null ? lastVersion.getContentHash() : null;
            if (!newHash.equals(lastVersionHash)) {
                createVersion(doc, userId);
            }
        }

        updateById(doc);
        return doc;
    }

    @Override
    @Transactional
    public void deleteDoc(String docId, String userId) {
        Doc doc = getById(docId);
        if (doc == null) {
            throw new BusinessException("error.doc.notFound");
        }

        Directory directory = directoryService.getById(doc.getDirectoryId());
        if (directory == null) {
            throw new BusinessException("error.directory.notFound");
        }

        if (!projectService.hasPermission(directory.getProjectId(), userId, "rw")) {
            throw new BusinessException("error.doc.noPermissionDelete");
        }

        // 删除文档的版本和评论
        docVersionMapper.delete(new LambdaQueryWrapper<DocVersion>().eq(DocVersion::getDocId, docId));
        commentMapper.delete(new LambdaQueryWrapper<com.freedoc.entity.Comment>()
                .eq(com.freedoc.entity.Comment::getDocId, docId));

        removeById(docId);
    }

    @Override
    public Doc getDocById(String docId, String userId) {
        Doc doc = getById(docId);
        if (doc == null) {
            throw new BusinessException("error.doc.notFound");
        }

        Directory directory = directoryService.getById(doc.getDirectoryId());
        if (directory != null) {
            projectService.getProjectById(directory.getProjectId(), userId);
        }

        return doc;
    }

    @Override
    public List<Doc> getDirectoryDocs(String directoryId, String userId) {
        Directory directory = directoryService.getById(directoryId);
        if (directory == null) {
            throw new BusinessException("error.directory.notFound");
        }

        projectService.getProjectById(directory.getProjectId(), userId);

        return list(new LambdaQueryWrapper<Doc>()
                .eq(Doc::getDirectoryId, directoryId)
                .orderByDesc(Doc::getUpdateTime));
    }

    @Override
    public Page<Doc> searchDocs(String keyword, int page, int size, String userId, String projectId) {
        List<Doc> docs = baseMapper.searchByKeyword(keyword, projectId);
        Page<Doc> result = new Page<>(page, size);
        result.setRecords(docs);
        result.setTotal(docs.size());
        return result;
    }

    @Override
    public List<Doc> getRecentDocs(String userId, int limit) {
        return list(new LambdaQueryWrapper<Doc>()
                .eq(Doc::getCreateUser, userId)
                .orderByDesc(Doc::getUpdateTime)
                .last("LIMIT " + limit));
    }

    @Override
    public Doc getDocByIdWithUser(String docId, String userId) {
        Doc doc = getById(docId);
        if (doc == null) {
            throw new BusinessException("error.doc.notFound");
        }

        Directory directory = directoryService.getById(doc.getDirectoryId());
        if (directory != null) {
            projectService.getProjectById(directory.getProjectId(), userId);
        }

        // 查询用户信息
        User createUser = null;
        User updateUser = null;
        
        if (doc.getCreateUser() != null && !doc.getCreateUser().isEmpty()) {
            createUser = userMapper.selectById(doc.getCreateUser());
        }
        
        if (doc.getUpdateUser() != null && !doc.getUpdateUser().isEmpty()) {
            updateUser = userMapper.selectById(doc.getUpdateUser());
        }

        // 转换为VO
        DocDetailVO vo = convertToDetailVO(doc, createUser, updateUser);
        return vo;
    }
    
    private DocDetailVO convertToDetailVO(Doc doc, User createUser, User updateUser) {
        DocDetailVO vo = new DocDetailVO();
        vo.setDocId(doc.getDocId());
        vo.setDirectoryId(doc.getDirectoryId());
        vo.setDocIcon(doc.getDocIcon());
        vo.setDocTitle(doc.getDocTitle());
        vo.setDocContent(doc.getDocContent());
        vo.setTeamId(doc.getTeamId());
        vo.setProjectId(doc.getProjectId());
        vo.setCreateTime(doc.getCreateTime());
        vo.setUpdateTime(doc.getUpdateTime());
        
        // 设置创建者信息
        if (createUser != null) {
            vo.setCreateUser(createUser.getUserId());
            vo.setCreateUserName(createUser.getUserName());
            vo.setCreateUserAccount(createUser.getAccount());
        } else {
            vo.setCreateUser(doc.getCreateUser());
        }
        
        // 设置更新者信息
        if (updateUser != null) {
            vo.setUpdateUser(updateUser.getUserId());
            vo.setUpdateUserName(updateUser.getUserName());
            vo.setUpdateUserAccount(updateUser.getAccount());
        } else {
            vo.setUpdateUser(doc.getUpdateUser());
        }
        
        return vo;
    }

    @Override
    public List<Doc> getProjectDocs(String projectId, String userId) {
        // 验证用户是否有该项目的访问权限
        projectService.getProjectById(projectId, userId);
        return list(new LambdaQueryWrapper<Doc>()
                .eq(Doc::getProjectId, projectId)
                .orderByDesc(Doc::getUpdateTime));
    }

    private void createVersion(Doc doc, String userId) {
        String contentHash = DigestUtil.sha256Hex(doc.getDocContent());

        DocVersion lastVersion = docVersionService.getLatestVersion(doc.getDocId());
        if (lastVersion != null && contentHash.equals(lastVersion.getContentHash())) {
            return;
        }

        DocVersion version = new DocVersion();
        version.setVersionId(SnowflakeIdUtil.nextId());
        version.setVersionNum(lastVersion != null ? lastVersion.getVersionNum() + 1 : 0);
        version.setDocId(doc.getDocId());
        version.setDocContent(doc.getDocContent());
        version.setIsCurrent(true);
        version.setContentHash(contentHash);
        version.setCreateTime(LocalDateTime.now());
        version.setCreateUser(userId);

        if (lastVersion != null) {
            String diff = generateDiff(lastVersion.getDocContent(), doc.getDocContent());
            version.setDiffContent(diff);
            lastVersion.setIsCurrent(false);
            docVersionService.updateById(lastVersion);
        }

        docVersionService.save(version);
    }

    private void updateCurrentVersion(Doc doc, String userId) {
        DocVersion currentVersion = docVersionService.getLatestVersion(doc.getDocId());
        if (currentVersion == null) {
            createVersion(doc, userId);
            return;
        }

        String contentHash = DigestUtil.sha256Hex(doc.getDocContent());
        if (contentHash.equals(currentVersion.getContentHash())) {
            return;
        }

        DocVersion previousVersion = docVersionMapper.selectOne(
                new LambdaQueryWrapper<DocVersion>()
                        .eq(DocVersion::getDocId, doc.getDocId())
                        .ne(DocVersion::getVersionId, currentVersion.getVersionId())
                        .orderByDesc(DocVersion::getCreateTime)
                        .last("LIMIT 1"));

        currentVersion.setDocContent(doc.getDocContent());
        currentVersion.setContentHash(contentHash);
        currentVersion.setCreateTime(LocalDateTime.now());
        currentVersion.setCreateUser(userId);

        if (previousVersion != null) {
            String diff = generateDiff(previousVersion.getDocContent(), doc.getDocContent());
            currentVersion.setDiffContent(diff);
        }

        docVersionService.updateById(currentVersion);
    }

    private String generateDiff(String oldContent, String newContent) {
        return "--- \n+++ \n@@ -1 +1 @@\n-" + oldContent + "\n+" + newContent;
    }

}
