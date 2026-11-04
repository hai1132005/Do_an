package com.ktx.ql_ktx.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XetDuyetRequest {
    private boolean dongY; // true = duyet, false = tu choi
    private String ghiChu;
    private java.time.LocalDate ngayVaoO; // ngay bat dau o; bo trong = hom nay
}