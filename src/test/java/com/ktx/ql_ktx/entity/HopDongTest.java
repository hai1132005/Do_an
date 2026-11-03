package com.ktx.ql_ktx.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test cho HopDong.soNgayChongLap() - thuat toan nen tang cua toan
 * bo viec chia hoa don ca nhan (HoaDonCaNhanService). Kiem thu rieng o
 * day de cach ly dung logic tinh ngay, khong phu thuoc Repository/DB.
 */
class HopDongTest {

    @Test
    void hopDongTronThang_traVeDuSoNgayCuaThang() {
        HopDong hd = moi(LocalDate.of(2026, 9, 1), null, null);
        long soNgay = hd.soNgayChongLap(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals(31, soNgay);
    }

    @Test
    void hopDongBatDauGiuaThang_chiTinhTuNgayBatDau() {
        HopDong hd = moi(LocalDate.of(2026, 10, 16), null, null);
        long soNgay = hd.soNgayChongLap(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals(16, soNgay); // 16..31 = 16 ngay
    }

    @Test
    void hopDongTraThucTeGiuaThang_chiTinhDenNgayTraThucTe() {
        HopDong hd = moi(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), null);
        long soNgay = hd.soNgayChongLap(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals(15, soNgay); // 1..15 = 15 ngay
    }

    @Test
    void ngayTraThucTeUuTienHonNgayKetThucDuKien() {
        // Du kien tra 20/10 nhung thuc te tra som hon vao 10/10
        HopDong hd = moi(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 20));
        long soNgay = hd.soNgayChongLap(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals(10, soNgay);
    }

    @Test
    void hopDongKhongLienQuanThang_traVe0() {
        HopDong hd = moi(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20), null);
        long soNgay = hd.soNgayChongLap(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals(0, soNgay);
    }

    @Test
    void hopDongChiMot1Ngay_traVe1() {
        HopDong hd = moi(LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 31), null);
        long soNgay = hd.soNgayChongLap(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals(1, soNgay);
    }

    private HopDong moi(LocalDate batDau, LocalDate traThucTe, LocalDate ketThucDuKien) {
        HopDong hd = new HopDong();
        hd.setNgayBatDau(batDau);
        hd.setNgayTraThucTe(traThucTe);
        hd.setNgayKetThuc(ketThucDuKien);
        return hd;
    }
}
