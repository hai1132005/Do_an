package com.ktx.ql_ktx.controller;

import com.ktx.ql_ktx.entity.HoaDon;
import com.ktx.ql_ktx.entity.NguoiDung;
import com.ktx.ql_ktx.entity.ToaNha;
import com.ktx.ql_ktx.repository.HopDongRepository;
import com.ktx.ql_ktx.repository.NguoiDungRepository;
import com.ktx.ql_ktx.security.NguoiDungPrincipal;
import com.ktx.ql_ktx.service.DangKyService;
import com.ktx.ql_ktx.service.ExportPdfService;
import com.ktx.ql_ktx.service.HoaDonService;
import com.ktx.ql_ktx.service.PhongService;
import com.ktx.ql_ktx.service.ViPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API danh cho SINH_VIEN: xem so do phong trong, dang ky phong, xem lich su dang ky,
 * xem hoa don / cong no cua chinh minh.
 */
@RestController
@RequestMapping("/api/sinh-vien")
public class SinhVienController {

    @Autowired private PhongService phongService;
    @Autowired private DangKyService dangKyService;
    @Autowired private HoaDonService hoaDonService;
    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private HopDongRepository hopDongRepository;
    @Autowired private ViPhamService viPhamService;
    @Autowired private ExportPdfService exportPdfService;

    private NguoiDung nguoiDungHienTai(Authentication auth) {
        String tenDangNhap = ((NguoiDungPrincipal) auth.getPrincipal()).getUsername();
        return nguoiDungRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new IllegalStateException("Khong xac dinh duoc nguoi dung"));
    }

    @GetMapping("/toa-nha")
    public ResponseEntity<List<ToaNha>> danhSachToaNha() {
        return ResponseEntity.ok(phongService.danhSachToaNha());
    }

    /** Xem so do phong con trong cua 1 toa nha (de chon dang ky) */
    @GetMapping("/phong-trong/{toaNhaId}")
    public ResponseEntity<?> phongConTrong(@PathVariable Long toaNhaId) {
        return ResponseEntity.ok(phongService.danhSachPhongConTrong(toaNhaId));
    }

    @PostMapping("/dang-ky-phong/{phongId}")
    public ResponseEntity<?> dangKyPhong(@PathVariable Long phongId, Authentication auth) {
        NguoiDung sv = nguoiDungHienTai(auth);
        return ResponseEntity.ok(dangKyService.dangKy(sv.getId(), phongId));
    }

    @GetMapping("/lich-su-dang-ky")
    public ResponseEntity<?> lichSuDangKy(Authentication auth) {
        NguoiDung sv = nguoiDungHienTai(auth);
        return ResponseEntity.ok(dangKyService.lichSuDangKy(sv.getId()));
    }

    @GetMapping("/hop-dong")
    public ResponseEntity<?> hopDongCuaToi(Authentication auth) {
        NguoiDung sv = nguoiDungHienTai(auth);
        return ResponseEntity.ok(hopDongRepository.findBySinhVienId(sv.getId()));
    }

    /** Xem hoa don cua phong minh dang o (sinh vien tim phong qua hop dong hien hanh) */
    @GetMapping("/hoa-don")
    public ResponseEntity<?> hoaDonCuaToi(Authentication auth) {
        NguoiDung sv = nguoiDungHienTai(auth);
        var hopDong = hopDongRepository.findBySinhVienId(sv.getId()).stream()
                .filter(h -> h.isConHieuLuc())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Ban chua co phong o"));
        Long phongId = hopDong.getGiuong().getPhong().getId();
        return ResponseEntity.ok(hoaDonService.hoaDonTheoPhong(phongId));
    }

    /** Sinh vien xem cac bien ban vi pham noi quy cua chinh minh */
    @GetMapping("/vi-pham")
    public ResponseEntity<?> viPhamCuaToi(Authentication auth) {
        NguoiDung sv = nguoiDungHienTai(auth);
        return ResponseEntity.ok(viPhamService.viPhamCuaSinhVien(sv.getId()));
    }

    /** Sinh vien tai hoa don PDF cua phong minh dang o */
    @GetMapping("/hoa-don/{hoaDonId}/pdf")
    public ResponseEntity<byte[]> taiHoaDonPdf(@PathVariable Long hoaDonId, Authentication auth) throws Exception {
        NguoiDung sv = nguoiDungHienTai(auth);
        HoaDon hd = hoaDonService.timHoaDon(hoaDonId);

        // Chi cho phep tai hoa don cua phong minh dang o
        boolean laPhongCuaMinh = hopDongRepository.findBySinhVienId(sv.getId()).stream()
                .filter(h -> h.isConHieuLuc())
                .anyMatch(h -> h.getGiuong().getPhong().getId().equals(hd.getPhong().getId()));
        if (!laPhongCuaMinh) {
            throw new IllegalStateException("Ban khong co quyen xem hoa don nay");
        }

        byte[] pdf = exportPdfService.xuatHoaDon(hd);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=hoa_don_" + hd.getThang() + "_" + hd.getNam() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}