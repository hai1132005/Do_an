package com.ktx.ql_ktx.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaoToaNhaRequest {
    @NotBlank
    private String tenToaNha;
    private String diaChi;
    private Integer soTang;
}
