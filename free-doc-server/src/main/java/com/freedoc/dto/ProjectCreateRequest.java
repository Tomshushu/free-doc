package com.freedoc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectCreateRequest {

    @NotBlank(message = "{validation.projectName.notBlank}")
    private String projectName;

    private String projectIcon;

    private String projectDesc;

    @NotBlank(message = "{validation.teamId.notBlank}")
    private String teamId;

}
