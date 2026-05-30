package com.freedoc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamCreateRequest {

    @NotBlank(message = "{validation.teamName.notBlank}")
    private String teamName;

    private String teamIcon;

    private String teamDesc;

}
