package com.freedoc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedoc.entity.DocVersion;

import java.util.List;

public interface DocVersionService extends IService<DocVersion> {

    List<DocVersion> getDocVersions(String docId, String userId);

    DocVersion getVersion(String versionId, String userId);

    DocVersion getLatestVersion(String docId);

    void rollbackToVersion(String docId, String versionId, String userId);

    void deleteVersionsAfter(String docId, Integer versionNum, String userId);

}
