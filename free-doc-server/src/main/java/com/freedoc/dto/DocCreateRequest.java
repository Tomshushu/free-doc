package com.freedoc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocCreateRequest {

    @NotBlank(message = "{validation.docTitle.notBlank}")
    private String docTitle;

    private String docIcon = "fa-solid fa-file-lines";

    private String docContent = "";

    @NotBlank(message = "{validation.directoryId.notBlank}")
    private String directoryId;

}
