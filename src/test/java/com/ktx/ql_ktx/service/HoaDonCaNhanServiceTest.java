package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.entity.*;
import com.ktx.ql_ktx.repository.HoaDonCaNhanRepository;
import com.ktx.ql_ktx.repository.HopDongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit test cho HoaDonCaNhanService - giai quyet diem 3 nhan xet cua
 * thay: "hoa don dang tinh theo PHONG, khong chia theo tung sinh vien;
 * khong co cong no ca nhan". Day la thuat toan "kho" thu 2 cua de tai
 * (sau bac gia dien luy tien), nen duoc yeu cau unit test rieng theo
 * gop y cua GVHD (diem 4).
 */
@ExtendWith(MockitoExtension.class)
class HoaDonCaNhanServiceTest {

    @Mock private HopDongRepository hopDongRepository;
    @Mock private HoaDonCaNhanRepository hoaDonCaNhanRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private HoaDonCaNhanService hoaDonCaNhanService;

    private NguoiDung sinhVienA;
    private NguoiDung sinhVienB;
    private Phong phong;
    private HoaDon hoaDonPhong;

    @BeforeEach
    void setUp() {
        sinhVienA = new NguoiDung();
        sinhVienA.setId(1L);
        sinhVienA.setHoTen("Nguyen Van A");

        sinhVienB = new NguoiDung();
        sinhVienB.setId(2L);
        sinhVienB.setHoTen("Tran Thi B");

        ToaNha toaNha = new ToaNha();
        toaNha.setId(1L);
        toaNha.setTenToaNha("Toa A");

        phong = new Phong();
        phong.setId(10L);
        phong.setToaNha(toaNha);
        phong.setSoPhong("101");

        // So tien duoc chon chia het cho 31 ngay cua thang 10/2026 de
        // phep tinh ra con so tron, de kiem tra chinh xac tuyet doi:
        // 620.000 / 31 = 20.000 d/ngay tien phong
        // 310.000 / 31 = 10.000 d/ngay tien dien
        // 155.000 / 31 =  5.000 d/ngay tien nuoc
        hoaDonPhong = new HoaDon();
        hoaDonPhong.setId(100L);
        hoaDonPhong.setPhong(phong);
        hoaDonPhong.setThang(10);
        hoaDonPhong.setNam(2026);
        hoaDonPhong.setTienPhong(new BigDecimal("620000"));
        hoaDonPhong.setTienDien(new BigDecimal("310000"));
        hoaDonPhong.setTienNuoc(new BigDecimal("155000"));
        hoaDonPhong.setTongTien(new BigDecimal("1085000"));

        when(hoaDonCaNhanRepository.findByHoaDonPhongId(100L)).thenReturn(List.of());
        when(hoaDonCaNhanRepository.save(any(HoaDonCaNhan.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private HopDong hopDong(NguoiDung sv, LocalDate batDau, LocalDate traThucTe, boolean conHieuLuc) {
        HopDong hd = new HopDong();
        hd.setSinhVien(sv);
        hd.setGiuong(giuong());
        hd.setNgayBatDau(batDau);
        hd.setNgayTraThucTe(traThucTe);
        hd.setConHieuLuc(conHieuLuc);
        return hd;
    }

    private Giuong giuong() {
        Giuong g = new Giuong();
        g.setPhong(phong);
        g.setKyHieu("A");
        return g;
    }

    @Test
    void chiaHoaDon_2SinhVienKhongTronThang_chiaDungTyLeNgayO() {
        // Sinh vien A o tu 01/10 den het 15/10 (15 ngay), roi chuyen phong
        HopDong hdA = hopDong(sinhVienA, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), false);
        // Sinh vien B don vao tu 16/10 den het thang, van dang o (16 ngay)
        HopDong hdB = hopDong(sinhVienB, LocalDate.of(2026, 10, 16), null, true);

        when(hopDongRepository.findByGiuongPhongId(10L)).thenReturn(List.of(hdA, hdB));

        List<HoaDonCaNhan> ketQua = hoaDonCaNhanService.chiaHoaDonTheoSinhVien(hoaDonPhong, 10, 2026);

        assertEquals(2, ketQua.size());

        HoaDonCaNhan cuaA = ketQua.stream().filter(h -> h.getSinhVien() == sinhVienA).findFirst().orElseThrow();
        HoaDonCaNhan cuaB = ketQua.stream().filter(h -> h.getSinhVien() == sinhVienB).findFirst().orElseThrow();

        assertEquals(15, cuaA.getSoNgayO());
        assertEquals(16, cuaB.getSoNgayO());

        assertEquals(new BigDecimal("300000"), cuaA.getTienPhong());
        assertEquals(new BigDecimal("150000"), cuaA.getTienDien());
        assertEquals(new BigDecimal("75000"), cuaA.getTienNuoc());
        assertEquals(new BigDecimal("525000"), cuaA.getTongTien());

        assertEquals(new BigDecimal("320000"), cuaB.getTienPhong());
        assertEquals(new BigDecimal("160000"), cuaB.getTienDien());
        assertEquals(new BigDecimal("80000"), cuaB.getTienNuoc());
        assertEquals(new BigDecimal("560000"), cuaB.getTongTien());

        // Tong 2 phan phai khop dung tong tien hoa don phong - khong ro ri, khong du
        BigDecimal tongSau = cuaA.getTongTien().add(cuaB.getTongTien());
        assertEquals(hoaDonPhong.getTongTien(), tongSau);
    }

    @Test
    void hopDongKhongLienQuanDenThangHoaDon_bịLoaiKhoiPhanBo() {
        // Hop dong da ket thuc tu thang 1, khong lien quan gi den thang 10
        HopDong hdCu = hopDong(sinhVienA, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20), false);
        // Hop dong duy nhat con lai, o tron ca thang 10
        HopDong hdHienTai = hopDong(sinhVienB, LocalDate.of(2026, 9, 1), null, true);

        when(hopDongRepository.findByGiuongPhongId(10L)).thenReturn(List.of(hdCu, hdHienTai));

        List<HoaDonCaNhan> ketQua = hoaDonCaNhanService.chiaHoaDonTheoSinhVien(hoaDonPhong, 10, 2026);

        assertEquals(1, ketQua.size());
        assertEquals(sinhVienB, ketQua.get(0).getSinhVien());
        assertEquals(31, ketQua.get(0).getSoNgayO());
        assertEquals(hoaDonPhong.getTongTien(), ketQua.get(0).getTongTien());
    }

    @Test
    void phongKhongCoAiO_khongTaoHoaDonCaNhanNao() {
        when(hopDongRepository.findByGiuongPhongId(10L)).thenReturn(List.of());

        List<HoaDonCaNhan> ketQua = hoaDonCaNhanService.chiaHoaDonTheoSinhVien(hoaDonPhong, 10, 2026);

        assertTrue(ketQua.isEmpty());
    }

    @Test
    void tongCongNo_chiCongCacHoaDonChuaThanhToan() {
        HoaDonCaNhan chuaTT1 = hoaDonCaNhan(sinhVienA, new BigDecimal("300000"), TrangThaiHoaDon.CHUA_THANH_TOAN);
        HoaDonCaNhan chuaTT2 = hoaDonCaNhan(sinhVienA, new BigDecimal("150000"), TrangThaiHoaDon.QUA_HAN);

        when(hoaDonCaNhanRepository.findBySinhVienIdAndTrangThaiNot(eq(1L), eq(TrangThaiHoaDon.DA_THANH_TOAN)))
                .thenReturn(List.of(chuaTT1, chuaTT2));

        BigDecimal tongNo = hoaDonCaNhanService.tongCongNoCuaSinhVien(1L);

        assertEquals(new BigDecimal("450000"), tongNo);
    }

    private HoaDonCaNhan hoaDonCaNhan(NguoiDung sv, BigDecimal tongTien, TrangThaiHoaDon trangThai) {
        HoaDonCaNhan hd = new HoaDonCaNhan();
        hd.setSinhVien(sv);
        hd.setTongTien(tongTien);
        hd.setTrangThai(trangThai);
        return hd;
    }
}
