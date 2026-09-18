package com.ktx.ql_ktx.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Thong tin sinh vien dang o trong 1 toa nha - dung cho danh sach cu dan & lap vi pham */
@Getter
@Setter
@AllArgsConstructor
public class SinhVienDangOResponse {
    private Long sinhVienId;
    private String hoTen;
    private String mssv;
    private String email;
    private String soDienThoai;
    private String tenToaNha;
    private String soPhong;
    private String kyHieuGiuong;
    private Long hopDongId;
    private int soLanViPham;
}