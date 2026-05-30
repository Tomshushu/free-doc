package com.freedoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedoc.common.exception.BusinessException;
import com.freedoc.common.util.SnowflakeIdUtil;
import com.freedoc.dto.DirectoryCreateRequest;
import com.freedoc.entity.Directory;
import com.freedoc.entity.Doc;
import com.freedoc.entity.Project;
import com.freedoc.mapper.DirectoryMapper;
import com.freedoc.mapper.DocMapper;
import com.freedoc.service.DirectoryService;
import com.freedoc.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl extends ServiceImpl<DirectoryMapper, Directory> implements DirectoryService {

    private final ProjectService projectService;
    private final DocMapper docMapper;
    private final com.freedoc.mapper.DocVersionMapper docVersionMapper;
    private final com.freedoc.mapper.CommentMapper commentMapper;

    @Override
    @Transactional
    public Directory createDirectory(DirectoryCreateRequest request, String userId) {
        Project project = projectService.getProjectById(request.getProjectId(), userId);

        String pids = "[0],";
        if (!"0".equals(request.getPid())) {
            Directory parent = getById(request.getPid());
            if (parent == null) {
                throw new BusinessException("error.directory.parentNotFound");
            }
            pids = parent.getPids() + "[" + request.getPid() + "],";
        }

        Directory directory = new Directory();
        directory.setId(SnowflakeIdUtil.nextId());
        directory.setName(request.getName());
        directory.setPid(request.getPid());
        directory.setPids(pids);
        directory.setProjectId(project.getProjectId());
        directory.setTeamId(project.getTeamId());
        directory.setCreateUser(userId);
        directory.setCreateTime(LocalDateTime.now());
        save(directory);

        return directory;
    }

    @Override
    public List<Directory> getProjectDirectories(String projectId, String userId) {
        projectService.getProjectById(projectId, userId);
        return list(new LambdaQueryWrapper<Directory>()
                .eq(Directory::getProjectId, projectId)
                .orderByAsc(Directory::getCreateTime));
    }

    @Override
    @Transactional
    public void deleteDirectory(String directoryId, String userId) {
        Directory directory = getById(directoryId);
        if (directory == null) {
            throw new BusinessException("error.directory.notFound");
        }

        projectService.getProjectById(directory.getProjectId(), userId);

        if (!projectService.hasPermission(directory.getProjectId(), userId, "rw")) {
            throw new BusinessException("error.directory.noPermissionDelete");
        }

        // 获取所有子目录
        List<Directory> children = list(new LambdaQueryWrapper<Directory>()
                .likeRight(Directory::getPids, directory.getPids() + "[" + directoryId));

        // 删除子目录下的文档及其版本和评论
        for (Directory child : children) {
            List<Doc> childDocs = docMapper.selectList(new LambdaQueryWrapper<Doc>().eq(Doc::getDirectoryId, child.getId()));
            List<String> childDocIds = childDocs.stream().map(Doc::getDocId).collect(java.util.stream.Collectors.toList());
            if (!childDocIds.isEmpty()) {
                docVersionMapper.delete(new LambdaQueryWrapper<com.freedoc.entity.DocVersion>()
                        .in(com.freedoc.entity.DocVersion::getDocId, childDocIds));
                commentMapper.delete(new LambdaQueryWrapper<com.freedoc.entity.Comment>()
                        .in(com.freedoc.entity.Comment::getDocId, childDocIds));
            }
            docMapper.delete(new LambdaQueryWrapper<Doc>().eq(Doc::getDirectoryId, child.getId()));
            removeById(child.getId());
        }

        // 删除当前目录下的文档及其版本和评论
        List<Doc> docs = docMapper.selectList(new LambdaQueryWrapper<Doc>().eq(Doc::getDirectoryId, directoryId));
        List<String> docIds = docs.stream().map(Doc::getDocId).collect(java.util.stream.Collectors.toList());
        if (!docIds.isEmpty()) {
            docVersionMapper.delete(new LambdaQueryWrapper<com.freedoc.entity.DocVersion>()
                    .in(com.freedoc.entity.DocVersion::getDocId, docIds));
            commentMapper.delete(new LambdaQueryWrapper<com.freedoc.entity.Comment>()
                    .in(com.freedoc.entity.Comment::getDocId, docIds));
        }
        docMapper.delete(new LambdaQueryWrapper<Doc>().eq(Doc::getDirectoryId, directoryId));
        removeById(directoryId);
    }

    @Override
    public Directory updateDirectoryName(String directoryId, String name, String userId) {
        Directory directory = getById(directoryId);
        if (directory == null) {
            throw new BusinessException("error.directory.notFound");
        }

        projectService.getProjectById(directory.getProjectId(), userId);

        if (!projectService.hasPermission(directory.getProjectId(), userId, "rw")) {
            throw new BusinessException("error.directory.noPermissionModify");
        }

        directory.setName(name);
        directory.setUpdateTime(LocalDateTime.now());
        directory.setUpdateUser(userId);
        updateById(directory);

        return directory;
    }

    @Override
    public List<Doc> getAllDocumentsRecursively(String directoryId, String userId) {
        // 获取当前目录
        Directory directory = getById(directoryId);
        if (directory == null) {
            throw new BusinessException("error.directory.notFound");
        }

        // 验证权限
        projectService.getProjectById(directory.getProjectId(), userId);

        // 获取当前目录及所有子目录
        // 修复：使用正确的查询条件，包含当前目录和所有子目录
        List<Directory> allDirectories = list(new LambdaQueryWrapper<Directory>()
                .eq(Directory::getProjectId, directory.getProjectId())
                .and(wrapper -> wrapper
                        .eq(Directory::getId, directoryId)
                        .or()
                        .like(Directory::getPids, "[" + directoryId + "]")
                ));

        // 获取所有目录下的文档
        List<String> directoryIds = allDirectories.stream()
                .map(Directory::getId)
                .collect(java.util.stream.Collectors.toList());

        if (directoryIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return docMapper.selectList(new LambdaQueryWrapper<Doc>()
                .in(Doc::getDirectoryId, directoryIds)
                .orderByAsc(Doc::getCreateTime));
    }

    @Override
    public String getDocumentRelativePath(String docId, String rootDirectoryId) {
        // 获取文档信息
        Doc doc = docMapper.selectById(docId);
        if (doc == null || doc.getDirectoryId() == null) {
            return "";
        }

        // 获取文档所在目录
        Directory docDirectory = getById(doc.getDirectoryId());
        if (docDirectory == null) {
            return "";
        }

        // 如果文档就在根目录下，返回空路径
        if (docDirectory.getId().equals(rootDirectoryId)) {
            return "";
        }

        // 如果rootDirectoryId是"0"（项目根），构建从文档目录到项目根的完整路径
        boolean isProjectRoot = "0".equals(rootDirectoryId);
        
        // 构建路径：从文档目录向上遍历到根目录
        StringBuilder pathBuilder = new StringBuilder();
        Directory current = docDirectory;

        // 从文档目录向上遍历到根目录
        while (current != null && !current.getId().equals(rootDirectoryId)) {
            if (pathBuilder.length() > 0) {
                pathBuilder.insert(0, "/");
            }
            pathBuilder.insert(0, current.getName());

            // 获取父目录
            if ("0".equals(current.getPid())) {
                // 已经到达项目根目录
                if (isProjectRoot) {
                    // 如果目标就是项目根，停止遍历
                    break;
                } else {
                    // 如果目标不是项目根，说明文档不在指定根目录的子树中
                    break;
                }
            }
            current = getById(current.getPid());
        }

        return pathBuilder.toString();
    }

    @Override
    public String getDirectoryName(String directoryId) {
        Directory directory = getById(directoryId);
        return directory != null ? directory.getName() : "unknown";
    }

}
