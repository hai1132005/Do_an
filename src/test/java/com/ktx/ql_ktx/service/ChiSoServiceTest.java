package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.entity.BacGiaDien;
import com.ktx.ql_ktx.repository.BacGiaDienRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit test cho thuat toan tinh tien dien theo BAC GIA LUY TIEN
 * (ChiSoService.tinhTienDien) - diem kho / diem nhan manh cua de tai,
 * duoc yeu cau kiem thu tu dong theo gop y cua GVHD (diem 4).
 *
 * Bang gia dung dung bang mac dinh trong DataSeeder:
 *   Bac 1: 0  - 50  kWh : 1.800 d/kWh
 *   Bac 2: 50 - 100 kWh : 2.000 d/kWh
 *   Bac 3: > 100        : 2.500 d/kWh
 */
@ExtendWith(MockitoExtension.class)
class ChiSoServiceTest {

    @Mock private BacGiaDienRepository bacGiaDienRepository;

    @InjectMocks
    private ChiSoService chiSoService;

    @BeforeEach
    void setUp() {
        List<BacGiaDien> bacGia = List.of(
                bac(1, 0, 50.0, "1800"),
                bac(2, 50, 100.0, "2000"),
                bac(3, 100, null, "2500")
        );
        when(bacGiaDienRepository.findAllByOrderByBacThuAsc()).thenReturn(bacGia);
    }

    private BacGiaDien bac(int thu, double tu, Double den, String gia) {
        BacGiaDien b = new BacGiaDien();
        b.setBacThu(thu);
        b.setTuSoKw(tu);
        b.setDenSoKw(den);
        b.setDonGia(new BigDecimal(gia));
        return b;
    }

    @Test
    void tieuThuBang0_tienDienBang0() {
        assertEquals(new BigDecimal("0"), chiSoService.tinhTienDien(0));
    }

    @Test
    void tieuThuTrongBac1_tinhDungDonGiaBac1() {
        // 30 kWh, tat ca nam trong bac 1 (0-50): 30 * 1800 = 54.000
        assertEquals(new BigDecimal("54000"), chiSoService.tinhTienDien(30));
    }

    @Test
    void tieuThuDungRanhGioiBac1_khongTinhLemSangBac2() {
        // Dung 50 kWh = het bac 1, chua cham bac 2: 50 * 1800 = 90.000
        assertEquals(new BigDecimal("90000"), chiSoService.tinhTienDien(50));
    }

    @Test
    void tieuThuVuotBac1SangBac2_chiaDungTungBac() {
        // 80 kWh = 50 (bac 1) + 30 (bac 2) = 50*1800 + 30*2000 = 90.000 + 60.000 = 150.000
        assertEquals(new BigDecimal("150000"), chiSoService.tinhTienDien(80));
    }

    @Test
    void tieuThuVuotCaBaBac_chiaDungTungBac() {
        // 150 kWh = 50(bac1) + 50(bac2) + 50(bac3) = 90.000 + 100.000 + 125.000 = 315.000
        assertEquals(new BigDecimal("315000"), chiSoService.tinhTienDien(150));
    }

    @Test
    void chuaCauHinhBacGia_dungGiaMacDinh2000() {
        when(bacGiaDienRepository.findAllByOrderByBacThuAsc()).thenReturn(List.of());
        // 40 kWh * 2000 = 80.000
        assertEquals(new BigDecimal("80000"), chiSoService.tinhTienDien(40));
    }
}
