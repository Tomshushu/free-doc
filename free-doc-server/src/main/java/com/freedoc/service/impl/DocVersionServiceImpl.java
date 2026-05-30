package com.freedoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedoc.common.exception.BusinessException;
import com.freedoc.entity.Doc;
import com.freedoc.entity.DocVersion;
import com.freedoc.mapper.DocVersionMapper;
import com.freedoc.service.DocService;
import com.freedoc.service.DocVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocVersionServiceImpl extends ServiceImpl<DocVersionMapper, DocVersion> implements DocVersionService {

    private final DocService docService;

    @Override
    public List<DocVersion> getDocVersions(String docId, String userId) {
        docService.getDocById(docId, userId);
        return list(new LambdaQueryWrapper<DocVersion>()
                .eq(DocVersion::getDocId, docId)
                .orderByDesc(DocVersion::getCreateTime));
    }

    @Override
    public DocVersion getVersion(String versionId, String userId) {
        DocVersion version = getById(versionId);
        if (version == null) {
            throw new BusinessException("error.version.notFound");
        }
        docService.getDocById(version.getDocId(), userId);
        return version;
    }

    @Override
    public DocVersion getLatestVersion(String docId) {
        return getOne(new LambdaQueryWrapper<DocVersion>()
                .eq(DocVersion::getDocId, docId)
                .orderByDesc(DocVersion::getCreateTime)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional
    public void rollbackToVersion(String docId, String versionId, String userId) {
        Doc doc = docService.getDocById(docId, userId);

        DocVersion targetVersion = getById(versionId);
        if (targetVersion == null || !targetVersion.getDocId().equals(docId)) {
            throw new BusinessException("error.version.notBelongToDoc");
        }

        // 删除所有版本号大于目标版本的版本 (即删除目标版本之后的所有版本)
        remove(new LambdaQueryWrapper<DocVersion>()
                .eq(DocVersion::getDocId, docId)
                .gt(DocVersion::getVersionNum, targetVersion.getVersionNum()));

        // 将所有版本设为非当前
        update(new LambdaUpdateWrapper<DocVersion>()
                .eq(DocVersion::getDocId, docId)
                .set(DocVersion::getIsCurrent, false));

        // 将目标版本设为当前版本
        targetVersion.setIsCurrent(true);
        updateById(targetVersion);

        // 同步更新文档内容
        doc.setDocContent(targetVersion.getDocContent());
        doc.setUpdateUser(userId);
        doc.setUpdateTime(LocalDateTime.now());
        docService.updateById(doc);
    }

    @Override
    @Transactional
    public void deleteVersionsAfter(String docId, Integer versionNum, String userId) {
        docService.getDocById(docId, userId);
        remove(new LambdaQueryWrapper<DocVersion>()
                .eq(DocVersion::getDocId, docId)
                .gt(DocVersion::getVersionNum, versionNum));
    }

}
