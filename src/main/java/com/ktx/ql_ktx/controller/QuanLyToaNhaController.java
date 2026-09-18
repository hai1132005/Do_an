package com.ktx.ql_ktx.controller;

import com.ktx.ql_ktx.dto.GhiChiSoRequest;
import com.ktx.ql_ktx.dto.TaoPhongRequest;
import com.ktx.ql_ktx.dto.TaoViPhamRequest;
import com.ktx.ql_ktx.dto.XetDuyetRequest;
import com.ktx.ql_ktx.entity.HoaDon;
import com.ktx.ql_ktx.entity.NguoiDung;
import com.ktx.ql_ktx.entity.VaiTro;
import com.ktx.ql_ktx.repository.NguoiDungRepository;
import com.ktx.ql_ktx.security.NguoiDungPrincipal;
import com.ktx.ql_ktx.service.ChiSoService;
import com.ktx.ql_ktx.service.CuDanService;
import com.ktx.ql_ktx.service.DangKyService;
import com.ktx.ql_ktx.service.ExportPdfService;
import com.ktx.ql_ktx.service.HoaDonService;
import com.ktx.ql_ktx.service.PhongService;
import com.ktx.ql_ktx.service.TongQuanService;
import com.ktx.ql_ktx.service.ViPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API danh cho QUAN_LY_TOA_NHA (va BAN_QUAN_LY):
 * - Xet duyet dang ky phong cua sinh vien
 * - Tao phong moi trong toa nha minh phu trach
 * - Ghi chi so dien nuoc hang thang -> tu dong tao hoa don
 * - Xac nhan thanh toan hoa don
 */
@RestController
@RequestMapping("/api/quan-ly-toa-nha")
public class QuanLyToaNhaController {

    @Autowired private DangKyService dangKyService;
    @Autowired private PhongService phongService;
    @Autowired private ChiSoService chiSoService;
    @Autowired private HoaDonService hoaDonService;
    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private CuDanService cuDanService;
    @Autowired private ViPhamService viPhamService;
    @Autowired private TongQuanService tongQuanService;
    @Autowired private ExportPdfService exportPdfService;

    private NguoiDung nguoiDungHienTai(Authentication auth) {
        String tenDangNhap = ((NguoiDungPrincipal) auth.getPrincipal()).getUsername();
        return nguoiDungRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new IllegalStateException("Khong xac dinh duoc nguoi dung"));
    }

    @GetMapping("/dang-ky-cho-duyet/{toaNhaId}")
    public ResponseEntity<?> danhSachChoDuyet(@PathVariable Long toaNhaId) {
        return ResponseEntity.ok(dangKyService.danhSachChoDuyet(toaNhaId));
    }

    @PostMapping("/xet-duyet/{dangKyId}")
    public ResponseEntity<?> xetDuyet(@PathVariable Long dangKyId, @RequestBody XetDuyetRequest req, Authentication auth) {
        NguoiDung nguoiDuyet = nguoiDungHienTai(auth);
        return ResponseEntity.ok(dangKyService.xetDuyet(dangKyId, nguoiDuyet.getId(), req));
    }

    @PostMapping("/tao-phong")
    public ResponseEntity<?> taoPhong(@RequestBody TaoPhongRequest req) {
        return ResponseEntity.ok(phongService.taoPhong(req));
    }

    @GetMapping("/so-do-phong/{toaNhaId}")
    public ResponseEntity<?> soDoPhong(@PathVariable Long toaNhaId) {
        return ResponseEntity.ok(phongService.soDoPhong(toaNhaId));
    }

    /** Ghi chi so dien nuoc thang - tu dong tao hoa don ngay sau khi ghi */
    @PostMapping("/ghi-chi-so")
    public ResponseEntity<?> ghiChiSo(@RequestBody GhiChiSoRequest req, Authentication auth) {
        NguoiDung nguoiGhi = nguoiDungHienTai(auth);
        return ResponseEntity.ok(chiSoService.ghiChiSo(req, nguoiGhi.getId()));
    }

    @PostMapping("/xac-nhan-thanh-toan/{hoaDonId}")
    public ResponseEntity<?> xacNhanThanhToan(@PathVariable Long hoaDonId) {
        return ResponseEntity.ok(hoaDonService.xacNhanThanhToan(hoaDonId));
    }

    @GetMapping("/cong-no")
    public ResponseEntity<?> danhSachCongNo() {
        return ResponseEntity.ok(hoaDonService.danhSachCongNo());
    }

    // ===================== TONG QUAN =====================

    /** So lieu tong quan. Quan ly toa nha chi thay toa nha minh phu trach. */
    @GetMapping("/tong-quan")
    public ResponseEntity<?> tongQuan(@RequestParam(required = false) Long toaNhaId, Authentication auth) {
        NguoiDung nd = nguoiDungHienTai(auth);
        Long phamVi = (nd.getVaiTro() == VaiTro.QUAN_LY_TOA_NHA && nd.getToaNhaPhuTrach() != null)
                ? nd.getToaNhaPhuTrach().getId()
                : toaNhaId;
        return ResponseEntity.ok(tongQuanService.tongQuan(phamVi));
    }

    // ===================== CU DAN & TRA PHONG =====================

    /** Danh sach sinh vien dang o trong toa nha (kem so lan vi pham) */
    @GetMapping("/cu-dan/{toaNhaId}")
    public ResponseEntity<?> danhSachCuDan(@PathVariable Long toaNhaId) {
        return ResponseEntity.ok(cuDanService.danhSachCuDan(toaNhaId));
    }

    /** Tra phong: ket thuc hop dong, giai phong giuong */
    @PostMapping("/tra-phong/{hopDongId}")
    public ResponseEntity<?> traPhong(@PathVariable Long hopDongId) {
        cuDanService.traPhong(hopDongId);
        return ResponseEntity.ok().body("{\"thongBao\":\"Da tra phong thanh cong\"}");
    }

    // ===================== VI PHAM NOI QUY =====================

    @GetMapping("/vi-pham/{toaNhaId}")
    public ResponseEntity<?> danhSachViPham(@PathVariable Long toaNhaId) {
        return ResponseEntity.ok(viPhamService.viPhamTheoToaNha(toaNhaId));
    }

    @PostMapping("/vi-pham")
    public ResponseEntity<?> lapViPham(@RequestBody TaoViPhamRequest req, Authentication auth) {
        NguoiDung nguoiLap = nguoiDungHienTai(auth);
        return ResponseEntity.ok(viPhamService.lapViPham(req, nguoiLap.getId()));
    }

    @DeleteMapping("/vi-pham/{id}")
    public ResponseEntity<?> xoaViPham(@PathVariable Long id) {
        viPhamService.xoaViPham(id);
        return ResponseEntity.ok().body("{\"thongBao\":\"Da xoa bien ban vi pham\"}");
    }

    // ===================== XUAT HOA DON PDF =====================

    @GetMapping("/hoa-don/{hoaDonId}/pdf")
    public ResponseEntity<byte[]> xuatHoaDonPdf(@PathVariable Long hoaDonId) throws Exception {
        HoaDon hd = hoaDonService.timHoaDon(hoaDonId);
        byte[] pdf = exportPdfService.xuatHoaDon(hd);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=hoa_don_" + hd.getPhong().getSoPhong() + "_" + hd.getThang() + "_" + hd.getNam() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /** Danh sach toan bo hoa don cua 1 toa nha (de quan ly xem & tai PDF) */
    @GetMapping("/hoa-don/toa-nha/{toaNhaId}")
    public ResponseEntity<?> hoaDonTheoToaNha(@PathVariable Long toaNhaId) {
        return ResponseEntity.ok(hoaDonService.hoaDonTheoToaNha(toaNhaId));
    }
}