package com.freedoc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamMemberRequest {

    @NotBlank(message = "{validation.userId.notBlank}")
    private String userId;

    private String type = "PARTICIPANT";

}
