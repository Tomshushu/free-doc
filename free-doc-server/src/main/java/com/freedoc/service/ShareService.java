package com.freedoc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedoc.dto.ShareCreateRequest;
import com.freedoc.dto.ShareVO;
import com.freedoc.entity.Share;

import java.util.List;

public interface ShareService extends IService<Share> {

    ShareVO createShare(ShareCreateRequest request, String userId);

    void deleteShare(String shareId, String userId);

    List<ShareVO> getShareList(String targetType, String targetId, String userId);

    ShareVO getSharePublicInfo(String shareCode);

    ShareVO verifyShare(String shareCode, String password);

    Object getShareProjectContent(String shareCode, String shareToken);

    Object getShareDocContent(String shareCode, String docId, String shareToken);

    String generateShareToken(String shareCode);

    boolean validateShareToken(String shareCode, String shareToken);

}
