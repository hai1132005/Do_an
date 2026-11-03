package com.ktx.ql_ktx.dto;

import com.ktx.ql_ktx.entity.TrangThaiHoaDon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dong "phan cua tung sinh vien" cho man hinh Hoa don cua QUAN LY TOA NHA:
 * cho biet trong moi hoa don phong, ai da thanh toan, ai chua, va thanh
 * toan bang cach nao (VNPay hay tien mat).
 */
@Getter
@Setter
@AllArgsConstructor
public class HoaDonCaNhanQuanLyResponse {
    private Long id;
    private Long hoaDonPhongId;
    private Long sinhVienId;
    private String hoTen;
    private String mssv;
    private Integer soNgayO;
    private BigDecimal tongTien;
    private TrangThaiHoaDon trangThai;
    private LocalDate ngayThanhToan;
    private String hinhThuc; // "VNPay", "Tien mat" hoac null neu chua thanh toan
}