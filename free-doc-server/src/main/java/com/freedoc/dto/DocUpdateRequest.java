package com.freedoc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocUpdateRequest {

    @NotBlank(message = "{validation.docId.notBlank}")
    private String docId;

    private String docTitle;

    private String docIcon;

    private String docContent;

    private String directoryId;

    private Boolean createVersion;

    private Boolean updateCurrentVersion;

}
