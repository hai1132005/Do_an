package com.ktx.ql_ktx.dto;

import java.time.LocalDate;

/**
 * Hop dong cua sinh vien dang dang nhap, da "lam phang" thong tin phong/toa nha
 * (Giuong.phong bi @JsonIgnore nen khong the tra thang entity HopDong cho giao dien).
 */
public record HopDongCuaToiResponse(
        Long id,
        String tenToaNha,
        String soPhong,
        String kyHieuGiuong,
        LocalDate ngayBatDau,
        LocalDate ngayKetThuc,
        LocalDate ngayTraThucTe,
        boolean conHieuLuc,
        String lyDoKetThuc) {
}