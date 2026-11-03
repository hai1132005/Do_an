package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.entity.*;
import com.ktx.ql_ktx.repository.HoaDonCaNhanRepository;
import com.ktx.ql_ktx.repository.HoaDonRepository;
import com.ktx.ql_ktx.repository.HopDongRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * BO SUNG MOI - giai quyet truc tiep diem 3 trong nhan xet cua thay:
 * "hoa don dien nuoc dang tinh theo PHONG, khong chia theo tung sinh
 * vien; khong co cong no ca nhan".
 *
 * Cach lam: HoaDon (hoa don cua ca phong trong 1 thang) do ChiSoService
 * tao ra nhu cu, KHONG doi. Service nay chi them MOT BUOC SAU DO: lay
 * tong tien phong/dien/nuoc cua HoaDon do, roi chia theo dung ty le so
 * ngay moi sinh vien THUC SU o trong thang (lay qua HopDong.soNgayChongLap
 * voi toan bo hop dong - ke ca da ket thuc - gan voi cac giuong cua
 * phong do), ra N ban ghi HoaDonCaNhan.
 *
 * Viec chia theo NGAY THUC TE (khong chia deu dau nguoi mot cach may
 * moc) la phan xu ly "kho" tuong duong thuat toan bac gia luy tien o
 * ChiSoService, va la doi tuong duoc unit test trong
 * HoaDonCaNhanServiceTest.
 */
@Service
public class HoaDonCaNhanService {

    @Autowired private HopDongRepository hopDongRepository;
    @Autowired private HoaDonCaNhanRepository hoaDonCaNhanRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private HoaDonRepository hoaDonRepository;

    /**
     * Chia 1 hoa don phong (da tinh san tong tien phong/dien/nuoc cua
     * thang thang/nam) thanh cac hoa don ca nhan, theo dung so ngay tung
     * sinh vien da o trong thang do. Idempotent: neu hoa don nay da duoc
     * chia truoc do thi xoa cac ban ghi cu va chia lai (phong truong hop
     * chi so dien nuoc duoc sua/tinh lai).
     */
    @Transactional
    public List<HoaDonCaNhan> chiaHoaDonTheoSinhVien(HoaDon hoaDon, int thang, int nam) {
        List<HoaDonCaNhan> cuTruoc = hoaDonCaNhanRepository.findByHoaDonPhongId(hoaDon.getId());
        hoaDonCaNhanRepository.deleteAll(cuTruoc);

        YearMonth ym = YearMonth.of(nam, thang);
        LocalDate tuNgay = ym.atDay(1);
        LocalDate denNgay = ym.atEndOfMonth();

        Phong phong = hoaDon.getPhong();
        List<HopDong> tatCaHopDongCuaPhong = hopDongRepository.findByGiuongPhongId(phong.getId());

        record PhanBo(NguoiDung sinhVien, long soNgay) {}
        List<PhanBo> phanBoList = new ArrayList<>();
        long tongSoNgayDaPhanBo = 0;
        for (HopDong hd : tatCaHopDongCuaPhong) {
            long soNgay = hd.soNgayChongLap(tuNgay, denNgay);
            if (soNgay > 0) {
                phanBoList.add(new PhanBo(hd.getSinhVien(), soNgay));
                tongSoNgayDaPhanBo += soNgay;
            }
        }

        if (phanBoList.isEmpty()) {
            // Phong khong co ai o trong thang nay - khong tao hoa don ca
            // nhan nao, tranh chia cho 0.
            return List.of();
        }

        // Mau so de chia ty le: dung tong so "nguoi-ngay" thuc te da o,
        // KHONG dung so_giuong_toi_da * so_ngay_trong_thang, de khong bi
        // chia nho hon thuc te nhung thang phong khong o day giuong.
        long mauSoChiaTyLe = tongSoNgayDaPhanBo;

        List<HoaDonCaNhan> ketQua = new ArrayList<>();
        for (PhanBo pb : phanBoList) {
            BigDecimal tyLe = BigDecimal.valueOf(pb.soNgay())
                    .divide(BigDecimal.valueOf(mauSoChiaTyLe), 6, RoundingMode.HALF_UP);

            HoaDonCaNhan hdcn = new HoaDonCaNhan();
            hdcn.setHoaDonPhong(hoaDon);
            hdcn.setSinhVien(pb.sinhVien());
            hdcn.setSoNgayO((int) pb.soNgay());
            hdcn.setTienPhong(chiaTien(hoaDon.getTienPhong(), tyLe));
            hdcn.setTienDien(chiaTien(hoaDon.getTienDien(), tyLe));
            hdcn.setTienNuoc(chiaTien(hoaDon.getTienNuoc(), tyLe));
            hdcn.setTongTien(hdcn.getTienPhong().add(hdcn.getTienDien()).add(hdcn.getTienNuoc()));
            hdcn.setHanThanhToan(denNgay.plusDays(10));
            hdcn.setTrangThai(TrangThaiHoaDon.CHUA_THANH_TOAN);

            HoaDonCaNhan daLuu = hoaDonCaNhanRepository.save(hdcn);
            ketQua.add(daLuu);
            notificationService.guiEmailHoaDonMoi(daLuu);
        }
        return ketQua;
    }

    private BigDecimal chiaTien(BigDecimal tongCuaPhong, BigDecimal tyLe) {
        if (tongCuaPhong == null) return BigDecimal.ZERO;
        return tongCuaPhong.multiply(tyLe).setScale(0, RoundingMode.HALF_UP);
    }

    /** Tong cong no (cac hoa don ca nhan CHUA thanh toan) cua 1 sinh vien. */
    public BigDecimal tongCongNoCuaSinhVien(Long sinhVienId) {
        return hoaDonCaNhanRepository.findBySinhVienIdAndTrangThaiNot(sinhVienId, TrangThaiHoaDon.DA_THANH_TOAN)
                .stream()
                .map(HoaDonCaNhan::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<HoaDonCaNhan> hoaDonCuaSinhVien(Long sinhVienId) {
        return hoaDonCaNhanRepository.findBySinhVienId(sinhVienId);
    }

    public HoaDonCaNhan timHoaDonCaNhan(Long id) {
        return hoaDonCaNhanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay hoa don ca nhan"));
    }

    @Transactional
    public void xacNhanDaThanhToan(Long hoaDonCaNhanId, String maGiaoDichVnPay) {
        HoaDonCaNhan hd = timHoaDonCaNhan(hoaDonCaNhanId);
        hd.setTrangThai(TrangThaiHoaDon.DA_THANH_TOAN);
        hd.setNgayThanhToan(LocalDate.now());
        hd.setMaGiaoDichVnPay(maGiaoDichVnPay);
        hoaDonCaNhanRepository.save(hd);

        // Khi MOI phan ca nhan cua 1 hoa don phong deu da thanh toan thi hoa don phong
        // cung duoc danh dau da thanh toan, de cac man hinh/bao cao theo phong khop so lieu.
        HoaDon hoaDonPhong = hd.getHoaDonPhong();
        boolean tatCaDaTra = hoaDonCaNhanRepository.findByHoaDonPhongId(hoaDonPhong.getId()).stream()
                .allMatch(x -> x.getTrangThai() == TrangThaiHoaDon.DA_THANH_TOAN);
        if (tatCaDaTra) {
            hoaDonPhong.setTrangThai(TrangThaiHoaDon.DA_THANH_TOAN);
            hoaDonPhong.setNgayThanhToan(LocalDate.now());
            hoaDonRepository.save(hoaDonPhong);
        }

        notificationService.guiEmailThanhToanThanhCong(hd);
    }
}