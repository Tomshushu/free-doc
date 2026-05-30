package com.freedoc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.freedoc.dto.DocCreateRequest;
import com.freedoc.dto.DocUpdateRequest;
import com.freedoc.entity.Doc;

import java.util.List;

public interface DocService extends IService<Doc> {

    Doc createDoc(DocCreateRequest request, String userId);

    Doc updateDoc(DocUpdateRequest request, String userId);

    void deleteDoc(String docId, String userId);

    Doc getDocById(String docId, String userId);

    List<Doc> getDirectoryDocs(String directoryId, String userId);

    Page<Doc> searchDocs(String keyword, int page, int size, String userId, String projectId);

    List<Doc> getRecentDocs(String userId, int limit);

    /**
     * 获取项目下的所有文档
     */
    List<Doc> getProjectDocs(String projectId, String userId);
    
    /**
     * 获取文档详情（包含用户信息）
     */
    Doc getDocByIdWithUser(String docId, String userId);

}
