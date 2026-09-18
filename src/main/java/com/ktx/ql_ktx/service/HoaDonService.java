package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.entity.HoaDon;
import com.ktx.ql_ktx.entity.TrangThaiHoaDon;
import com.ktx.ql_ktx.repository.HoaDonRepository;
import com.ktx.ql_ktx.repository.PhongRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HoaDonService {

    @Autowired private HoaDonRepository hoaDonRepository;

    @Autowired private PhongRepository phongRepository;

    public List<HoaDon> hoaDonTheoPhong(Long phongId) {
        return hoaDonRepository.findByPhongId(phongId);
    }

    public HoaDon timHoaDon(Long id) {
        return hoaDonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay hoa don"));
    }

    /** Toan bo hoa don cua cac phong thuoc 1 toa nha */
    public List<HoaDon> hoaDonTheoToaNha(Long toaNhaId) {
        List<Long> phongIds = phongRepository.findByToaNhaId(toaNhaId)
                .stream().map(p -> p.getId()).toList();
        if (phongIds.isEmpty()) return List.of();
        return hoaDonRepository.findByPhongIdIn(phongIds);
    }

    public List<HoaDon> hoaDonChuaThanhToan() {
        return hoaDonRepository.findByTrangThai(TrangThaiHoaDon.CHUA_THANH_TOAN);
    }

    /** Danh sach cong no: hoa don qua han ma van chua thanh toan */
    @Transactional
    public List<HoaDon> danhSachCongNo() {
        List<HoaDon> chuaThanhToan = hoaDonChuaThanhToan();
        LocalDate homNay = LocalDate.now();
        for (HoaDon hd : chuaThanhToan) {
            if (hd.getHanThanhToan().isBefore(homNay) && hd.getTrangThai() != TrangThaiHoaDon.QUA_HAN) {
                hd.setTrangThai(TrangThaiHoaDon.QUA_HAN);
                hoaDonRepository.save(hd);
            }
        }
        return hoaDonRepository.findByTrangThai(TrangThaiHoaDon.QUA_HAN);
    }

    @Transactional
    public HoaDon xacNhanThanhToan(Long hoaDonId) {
        HoaDon hd = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay hoa don"));
        hd.setTrangThai(TrangThaiHoaDon.DA_THANH_TOAN);
        hd.setNgayThanhToan(LocalDate.now());
        return hoaDonRepository.save(hd);
    }
}