package com.ktx.ql_ktx.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GhiChiSoRequest {
    private Long phongId;
    private Integer thang;
    private Integer nam;
    private Double chiSoDienMoi;
    private Double chiSoNuocMoi;
}
