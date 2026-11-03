package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.dto.XetDuyetRequest;
import com.ktx.ql_ktx.entity.*;
import com.ktx.ql_ktx.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Nghiep vu: Sinh vien dang ky phong -> Quan ly toa nha xet duyet -> Tu dong xep giuong + tao hop dong.
 *
 * SUA SO VOI BAN GOC: khi duyet don, KHONG con goi giuong.setSinhVien(...)
 * nua (truong do da bo) - thay vao do chi can tao 1 ban ghi HopDong moi,
 * con viec "giuong da co nguoi" duoc GiuongService tu suy ra tu chinh hop
 * dong nay. Dieu kien chan dang ky trung cung khong con la "sinh vien
 * chua tung co giuong nao" ma la "sinh vien hien KHONG co hop dong nao
 * dang hieu luc" (vi 1 sinh vien co the da tung o va tra phong truoc do).
 */
@Service
public class DangKyService {

    @Autowired private DangKyPhongRepository dangKyRepository;
    @Autowired private PhongRepository phongRepository;
    @Autowired private GiuongService giuongService;
    @Autowired private HopDongRepository hopDongRepository;
    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private PhongService phongService;
    @Autowired private NotificationService notificationService;

    /** B1: Sinh vien dang ky nguyen vong o mot phong con trong */
    @Transactional
    public DangKyPhong dangKy(Long sinhVienId, Long phongId) {
        NguoiDung sv = nguoiDungRepository.findById(sinhVienId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay sinh vien"));
        Phong phong = phongRepository.findById(phongId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong"));

        boolean dangCoCho = hopDongRepository.findBySinhVienId(sinhVienId).stream()
                .anyMatch(HopDong::isConHieuLuc);
        if (dangCoCho) {
            throw new IllegalStateException("Sinh vien dang co cho o, khong the dang ky them");
        }
        if (phong.getTrangThai() == TrangThaiPhong.KHOA) {
            throw new IllegalStateException("Phong dang tam khoa, khong nhan dang ky");
        }
        if (giuongService.soGiuongConTrong(phong.getId()) <= 0) {
            throw new IllegalStateException("Phong da het cho trong");
        }

        DangKyPhong dk = new DangKyPhong();
        dk.setSinhVien(sv);
        dk.setPhong(phong);
        dk.setTrangThai(TrangThaiDangKy.CHO_DUYET);
        return dangKyRepository.save(dk);
    }

    /** Danh sach dang ky cho toa nha ma quan ly dang phu trach, trang thai CHO_DUYET */
    public List<DangKyPhong> danhSachChoDuyet(Long toaNhaId) {
        return dangKyRepository.findByPhongToaNhaIdAndTrangThai(toaNhaId, TrangThaiDangKy.CHO_DUYET);
    }

    /** B2: Quan ly toa nha xet duyet - neu dong y se tu dong xep giuong trong dau tien va tao hop dong */
    @Transactional
    public DangKyPhong xetDuyet(Long dangKyId, Long nguoiDuyetId, XetDuyetRequest req) {
        DangKyPhong dk = dangKyRepository.findById(dangKyId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don dang ky"));

        if (dk.getTrangThai() != TrangThaiDangKy.CHO_DUYET) {
            throw new IllegalStateException("Don da duoc xu ly truoc do");
        }

        NguoiDung nguoiDuyet = nguoiDungRepository.findById(nguoiDuyetId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi duyet"));

        dk.setNguoiDuyet(nguoiDuyet);
        dk.setNgayXetDuyet(LocalDateTime.now());
        dk.setGhiChu(req.getGhiChu());

        if (req.isDongY()) {
            Phong phong = dk.getPhong();
            List<Giuong> giuongTrong = giuongService.giuongConTrong(phong.getId());
            if (giuongTrong.isEmpty()) {
                throw new IllegalStateException("Phong da het giuong trong, khong the duyet");
            }
            Giuong giuong = giuongTrong.get(0);

            HopDong hd = new HopDong();
            hd.setSinhVien(dk.getSinhVien());
            hd.setGiuong(giuong);
            hd.setNgayBatDau(LocalDate.now());
            hd.setConHieuLuc(true);
            hopDongRepository.save(hd);

            phongService.capNhatTrangThaiPhong(phong);
            dk.setTrangThai(TrangThaiDangKy.DA_DUYET);

            notificationService.guiEmailDuyetDangKy(dk.getSinhVien(), phong, giuong);
        } else {
            dk.setTrangThai(TrangThaiDangKy.TU_CHOI);
        }

        return dangKyRepository.save(dk);
    }

    public List<DangKyPhong> lichSuDangKy(Long sinhVienId) {
        return dangKyRepository.findBySinhVienId(sinhVienId);
    }
}
