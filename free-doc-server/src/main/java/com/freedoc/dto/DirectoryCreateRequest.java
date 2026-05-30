package com.freedoc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DirectoryCreateRequest {

    @NotBlank(message = "{validation.directoryName.notBlank}")
    private String name;

    private String pid = "0";

    @NotBlank(message = "{validation.projectId.notBlank}")
    private String projectId;

}
