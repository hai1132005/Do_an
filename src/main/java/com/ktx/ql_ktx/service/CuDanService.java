package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.dto.SinhVienDangOResponse;
import com.ktx.ql_ktx.entity.Giuong;
import com.ktx.ql_ktx.entity.HopDong;
import com.ktx.ql_ktx.entity.LyDoKetThucHopDong;
import com.ktx.ql_ktx.entity.NguoiDung;
import com.ktx.ql_ktx.entity.Phong;
import com.ktx.ql_ktx.repository.GiuongRepository;
import com.ktx.ql_ktx.repository.HopDongRepository;
import com.ktx.ql_ktx.repository.ViPhamRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Quan ly cu dan dang o trong ky tuc xa:
 * - Danh sach sinh vien theo tung toa nha (kem so lan vi pham)
 * - Tra phong: ket thuc hop dong, cap nhat trang thai phong
 * - Chuyen phong: nghiep vu MOI (diem 3 nhan xet cua GVHD) ma thiet ke
 *   Giuong 1-1 cu KHONG THE lam duoc, vi khong co hop dong thi khong
 *   the biet ai tung o giuong cu luc nao sau khi chuyen.
 *
 * SUA SO VOI BAN GOC: truoc day "tra phong" chi la goi
 * giuong.setSinhVien(null) - mat het lich su ai da tung o giuong do.
 * Gio "tra phong" = KET THUC hop dong hien tai (conHieuLuc=false, set
 * ngayTraThucTe + lyDoKetThuc), giu nguyen ban ghi HopDong trong DB lam
 * lich su - day chinh la du lieu HoaDonCaNhanService dung de chia hoa
 * don theo dung so ngay thuc te da o.
 */
@Service
public class CuDanService {

    @Autowired private GiuongRepository giuongRepository;
    @Autowired private HopDongRepository hopDongRepository;
    @Autowired private ViPhamRepository viPhamRepository;
    @Autowired private PhongService phongService;
    @Autowired private GiuongService giuongService;

    /** Danh sach sinh vien dang o trong 1 toa nha */
    public List<SinhVienDangOResponse> danhSachCuDan(Long toaNhaId) {
        List<HopDong> hopDongs = hopDongRepository.findByGiuongPhongToaNhaIdAndConHieuLucTrue(toaNhaId);
        List<SinhVienDangOResponse> ketQua = new ArrayList<>();

        for (HopDong hd : hopDongs) {
            NguoiDung sv = hd.getSinhVien();
            Giuong g = hd.getGiuong();
            int soViPham = viPhamRepository.findBySinhVienId(sv.getId()).size();

            ketQua.add(new SinhVienDangOResponse(
                    sv.getId(), sv.getHoTen(), sv.getMssv(), sv.getEmail(), sv.getSoDienThoai(),
                    g.getPhong().getToaNha().getTenToaNha(), g.getPhong().getSoPhong(),
                    g.getKyHieu(), hd.getId(), soViPham));
        }
        return ketQua;
    }

    /**
     * Tra phong: ket thuc hop dong (giu lam lich su), cap nhat lai trang
     * thai phong (DAY -> TRONG neu con cho).
     */
    @Transactional
    public void traPhong(Long hopDongId) {
        HopDong hd = hopDongRepository.findById(hopDongId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay hop dong"));

        if (!hd.isConHieuLuc()) {
            throw new IllegalStateException("Hop dong nay da ket thuc truoc do");
        }

        Phong phong = hd.getGiuong().getPhong();

        hd.setConHieuLuc(false);
        hd.setNgayTraThucTe(LocalDate.now());
        hd.setLyDoKetThuc(LyDoKetThucHopDong.TRA_PHONG);
        hopDongRepository.save(hd);

        phongService.capNhatTrangThaiPhong(phong);
    }

    /**
     * Chuyen phong/giuong: ket thuc hop dong hien tai cua sinh vien
     * (lyDoKetThuc=CHUYEN_PHONG) va tao hop dong moi tren giuong dich,
     * trong CUNG 1 giao dich - dam bao khong co khoang trong du lieu va
     * khong lam mat lich su o giuong truoc do.
     */
    @Transactional
    public HopDong chuyenPhong(Long sinhVienId, Long giuongMoiId) {
        HopDong hdCu = hopDongRepository.findBySinhVienId(sinhVienId).stream()
                .filter(HopDong::isConHieuLuc)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Sinh vien hien khong co hop dong nao dang hieu luc"));

        if (hdCu.getGiuong().getId().equals(giuongMoiId)) {
            throw new IllegalArgumentException("Giuong dich trung voi giuong hien tai");
        }

        Giuong giuongMoi = giuongRepository.findById(giuongMoiId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay giuong dich"));

        if (!giuongService.conTrong(giuongMoiId)) {
            throw new IllegalStateException("Giuong dich da co nguoi o");
        }

        Phong phongCu = hdCu.getGiuong().getPhong();
        LocalDate homNay = LocalDate.now();

        hdCu.setConHieuLuc(false);
        hdCu.setNgayTraThucTe(homNay);
        hdCu.setLyDoKetThuc(LyDoKetThucHopDong.CHUYEN_PHONG);
        hopDongRepository.save(hdCu);

        HopDong hdMoi = new HopDong();
        hdMoi.setSinhVien(hdCu.getSinhVien());
        hdMoi.setGiuong(giuongMoi);
        hdMoi.setNgayBatDau(homNay);
        hdMoi.setConHieuLuc(true);
        hopDongRepository.save(hdMoi);

        phongService.capNhatTrangThaiPhong(phongCu);
        phongService.capNhatTrangThaiPhong(giuongMoi.getPhong());

        return hdMoi;
    }
}
