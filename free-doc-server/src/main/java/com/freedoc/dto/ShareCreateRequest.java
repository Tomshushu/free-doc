package com.freedoc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShareCreateRequest {

    @NotBlank(message = "{validation.shareType.notBlank}")
    private String targetType;

    @NotBlank(message = "{validation.shareTargetId.notBlank}")
    private String targetId;

    private String password;

    private Integer expireHours;

}
