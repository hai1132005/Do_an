package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.dto.SinhVienDangOResponse;
import com.ktx.ql_ktx.entity.Giuong;
import com.ktx.ql_ktx.entity.HopDong;
import com.ktx.ql_ktx.entity.NguoiDung;
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
 * - Tra phong: ket thuc hop dong, giai phong giuong, cap nhat trang thai phong
 */
@Service
public class CuDanService {

    @Autowired private GiuongRepository giuongRepository;
    @Autowired private HopDongRepository hopDongRepository;
    @Autowired private ViPhamRepository viPhamRepository;
    @Autowired private PhongService phongService;

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
     * Tra phong: ket thuc hop dong, go sinh vien khoi giuong,
     * cap nhat lai trang thai phong (DAY -> TRONG neu con cho).
     */
    @Transactional
    public void traPhong(Long hopDongId) {
        HopDong hd = hopDongRepository.findById(hopDongId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay hop dong"));

        if (!hd.isConHieuLuc()) {
            throw new IllegalStateException("Hop dong nay da ket thuc truoc do");
        }

        Giuong giuong = hd.getGiuong();
        giuong.setSinhVien(null);
        giuongRepository.save(giuong);

        hd.setConHieuLuc(false);
        hd.setNgayKetThuc(LocalDate.now());
        hopDongRepository.save(hd);

        phongService.capNhatTrangThaiPhong(giuong.getPhong());
    }
}