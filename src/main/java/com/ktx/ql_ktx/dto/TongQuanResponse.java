package com.ktx.ql_ktx.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/** So lieu tong hop hien thi tren trang Tong quan (dashboard) */
@Getter
@Setter
@AllArgsConstructor
public class TongQuanResponse {
    private int tongSoToaNha;
    private int tongSoPhong;
    private int tongSoGiuong;
    private int soGiuongDaO;
    private double tyLeLapDayChung;
    private int soDonChoDuyet;
    private int soHoaDonChuaThanhToan;
    private BigDecimal tongCongNo;
    private BigDecimal doanhThuThangNay;
    private int soViPhamThangNay;
}