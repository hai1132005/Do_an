package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.dto.ThongKeToaNhaResponse;
import com.ktx.ql_ktx.entity.HoaDon;
import com.ktx.ql_ktx.entity.Phong;
import com.ktx.ql_ktx.entity.TrangThaiHoaDon;
import com.ktx.ql_ktx.repository.HoaDonRepository;
import com.ktx.ql_ktx.repository.PhongRepository;
import com.ktx.ql_ktx.repository.ToaNhaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ThongKeService {

    @Autowired private ToaNhaRepository toaNhaRepository;
    @Autowired private PhongRepository phongRepository;
    @Autowired private HoaDonRepository hoaDonRepository;

    /** Thong ke ty le lap day, doanh thu thang hien tai, tong cong no cho 1 toa nha */
    public ThongKeToaNhaResponse thongKeToaNha(Long toaNhaId) {
        var toaNha = toaNhaRepository.findById(toaNhaId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay toa nha"));

        List<Phong> danhSachPhong = phongRepository.findByToaNhaId(toaNhaId);
        int tongSoGiuong = danhSachPhong.stream().mapToInt(Phong::getSoGiuongToiDa).sum();
        int soGiuongDaO = danhSachPhong.stream().mapToInt(Phong::getSoGiuongDaO).sum();
        double tyLeLapDay = tongSoGiuong == 0 ? 0 : (soGiuongDaO * 100.0 / tongSoGiuong);

        List<Long> phongIds = danhSachPhong.stream().map(Phong::getId).toList();
        LocalDate now = LocalDate.now();

        BigDecimal doanhThuThang = hoaDonRepository.findByPhongIdIn(phongIds).stream()
                .filter(hd -> hd.getThang() == now.getMonthValue() && hd.getNam() == now.getYear())
                .map(HoaDon::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tongCongNo = hoaDonRepository.findByPhongIdIn(phongIds).stream()
                .filter(hd -> hd.getTrangThai() != TrangThaiHoaDon.DA_THANH_TOAN)
                .map(HoaDon::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ThongKeToaNhaResponse(
                toaNha.getTenToaNha(),
                danhSachPhong.size(),
                tongSoGiuong,
                soGiuongDaO,
                Math.round(tyLeLapDay * 100.0) / 100.0,
                doanhThuThang,
                tongCongNo
        );
    }
}