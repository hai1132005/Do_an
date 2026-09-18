package com.ktx.ql_ktx.controller;

import com.ktx.ql_ktx.dto.TaoNhanVienRequest;
import com.ktx.ql_ktx.dto.TaoToaNhaRequest;
import com.ktx.ql_ktx.entity.VaiTro;
import com.ktx.ql_ktx.repository.NguoiDungRepository;
import com.ktx.ql_ktx.service.AuthService;
import com.ktx.ql_ktx.service.ExportExcelService;
import com.ktx.ql_ktx.service.HoaDonService;
import com.ktx.ql_ktx.service.PhongService;
import com.ktx.ql_ktx.service.ThongKeService;
import com.ktx.ql_ktx.service.TongQuanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * API danh cho BAN_QUAN_LY (quyen cao nhat):
 * - Tao toa nha moi
 * - Xem thong ke toan he thong (ty le lap day, doanh thu, cong no)
 * - Xuat bao cao Excel
 * - Quan ly tai khoan nguoi dung (kich hoat/khoa)
 */
@RestController
@RequestMapping("/api/ban-quan-ly")
public class BanQuanLyController {

    @Autowired private PhongService phongService;
    @Autowired private ThongKeService thongKeService;
    @Autowired private HoaDonService hoaDonService;
    @Autowired private ExportExcelService exportExcelService;
    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private AuthService authService;
    @Autowired private TongQuanService tongQuanService;

    @PostMapping("/tao-toa-nha")
    public ResponseEntity<?> taoToaNha(@RequestBody TaoToaNhaRequest req) {
        return ResponseEntity.ok(phongService.taoToaNha(req));
    }

    /** Tao tai khoan cho nhan vien Quan ly toa nha, gan phu trach 1 toa nha cu the */
    @PostMapping("/tao-nhan-vien")
    public ResponseEntity<?> taoNhanVien(@RequestBody TaoNhanVienRequest req) {
        return ResponseEntity.ok(authService.taoNhanVienQuanLy(req));
    }

    @GetMapping("/thong-ke/{toaNhaId}")
    public ResponseEntity<?> thongKe(@PathVariable Long toaNhaId) {
        return ResponseEntity.ok(thongKeService.thongKeToaNha(toaNhaId));
    }

    /** Tong quan TOAN HE THONG (tat ca toa nha) */
    @GetMapping("/tong-quan")
    public ResponseEntity<?> tongQuanToanHeThong() {
        return ResponseEntity.ok(tongQuanService.tongQuan(null));
    }

    @GetMapping("/danh-sach-nhan-vien")
    public ResponseEntity<?> danhSachNhanVien() {
        return ResponseEntity.ok(nguoiDungRepository.findByVaiTro(VaiTro.QUAN_LY_TOA_NHA));
    }

    /** Xuat file Excel danh sach hoa don/cong no toan he thong */
    @GetMapping("/xuat-excel-cong-no")
    public ResponseEntity<byte[]> xuatExcelCongNo() throws IOException {
        var danhSach = hoaDonService.danhSachCongNo();
        byte[] excelBytes = exportExcelService.xuatDanhSachHoaDon(danhSach);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cong_no.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    /** Khoa / mo khoa tai khoan */
    @PostMapping("/khoa-tai-khoan/{id}")
    public ResponseEntity<?> khoaTaiKhoan(@PathVariable Long id) {
        var nd = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay tai khoan"));
        nd.setActive(!nd.isActive());
        nguoiDungRepository.save(nd);
        return ResponseEntity.ok(nd);
    }
}