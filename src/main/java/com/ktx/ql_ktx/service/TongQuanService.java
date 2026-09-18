package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.dto.TongQuanResponse;
import com.ktx.ql_ktx.entity.*;
import com.ktx.ql_ktx.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Tong hop so lieu cho trang Tong quan (dashboard).
 * - Ban quan ly: xem toan he thong (toaNhaId = null)
 * - Quan ly toa nha: chi xem toa nha minh phu trach
 */
@Service
public class TongQuanService {

    @Autowired private ToaNhaRepository toaNhaRepository;
    @Autowired private PhongRepository phongRepository;
    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private DangKyPhongRepository dangKyRepository;
    @Autowired private ViPhamService viPhamService;

    public TongQuanResponse tongQuan(Long toaNhaId) {
        List<ToaNha> toaNhas = (toaNhaId == null)
                ? toaNhaRepository.findAll()
                : toaNhaRepository.findById(toaNhaId).map(List::of).orElse(List.of());

        List<Phong> phongs = toaNhas.stream()
                .flatMap(tn -> phongRepository.findByToaNhaId(tn.getId()).stream())
                .toList();

        int tongSoGiuong = phongs.stream().mapToInt(Phong::getSoGiuongToiDa).sum();
        int soGiuongDaO = phongs.stream().mapToInt(Phong::getSoGiuongDaO).sum();
        double tyLe = tongSoGiuong == 0 ? 0 : (soGiuongDaO * 100.0 / tongSoGiuong);

        int soDonChoDuyet = toaNhas.stream()
                .mapToInt(tn -> dangKyRepository
                        .findByPhongToaNhaIdAndTrangThai(tn.getId(), TrangThaiDangKy.CHO_DUYET).size())
                .sum();

        List<Long> phongIds = phongs.stream().map(Phong::getId).toList();
        List<HoaDon> hoaDons = phongIds.isEmpty() ? List.of() : hoaDonRepository.findByPhongIdIn(phongIds);

        LocalDate now = LocalDate.now();

        int soHoaDonChuaTT = (int) hoaDons.stream()
                .filter(h -> h.getTrangThai() != TrangThaiHoaDon.DA_THANH_TOAN).count();

        BigDecimal tongCongNo = hoaDons.stream()
                .filter(h -> h.getTrangThai() != TrangThaiHoaDon.DA_THANH_TOAN)
                .map(HoaDon::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal doanhThuThangNay = hoaDons.stream()
                .filter(h -> h.getThang() == now.getMonthValue() && h.getNam() == now.getYear())
                .map(HoaDon::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int soViPhamThangNay = toaNhas.stream()
                .flatMap(tn -> viPhamService.viPhamTheoToaNha(tn.getId()).stream())
                .filter(vp -> vp.getNgayViPham() != null
                        && vp.getNgayViPham().getMonthValue() == now.getMonthValue()
                        && vp.getNgayViPham().getYear() == now.getYear())
                .toList().size();

        return new TongQuanResponse(
                toaNhas.size(), phongs.size(), tongSoGiuong, soGiuongDaO,
                Math.round(tyLe * 100.0) / 100.0,
                soDonChoDuyet, soHoaDonChuaTT, tongCongNo, doanhThuThangNay, soViPhamThangNay);
    }
}