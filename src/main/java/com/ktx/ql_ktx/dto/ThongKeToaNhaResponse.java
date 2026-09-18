package com.ktx.ql_ktx.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ThongKeToaNhaResponse {
    private String tenToaNha;
    private int tongSoPhong;
    private int tongSoGiuong;
    private int soGiuongDaO;
    private double tyLeLapDay; // %
    private BigDecimal doanhThuThangHienTai;
    private BigDecimal tongCongNo;
}
