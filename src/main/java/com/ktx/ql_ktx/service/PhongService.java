package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.dto.TaoPhongRequest;
import com.ktx.ql_ktx.dto.TaoToaNhaRequest;
import com.ktx.ql_ktx.entity.*;
import com.ktx.ql_ktx.repository.GiuongRepository;
import com.ktx.ql_ktx.repository.PhongRepository;
import com.ktx.ql_ktx.repository.ToaNhaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Quan ly Toa nha - Phong - Giuong (so do phong/giuong, tinh trang con trong).
 */
@Service
public class PhongService {

    @Autowired
    private ToaNhaRepository toaNhaRepository;

    @Autowired
    private PhongRepository phongRepository;

    @Autowired
    private GiuongRepository giuongRepository;

    @Transactional
    public ToaNha taoToaNha(TaoToaNhaRequest req) {
        if (toaNhaRepository.existsByTenToaNha(req.getTenToaNha())) {
            throw new IllegalArgumentException("Toa nha da ton tai");
        }
        ToaNha tn = new ToaNha();
        tn.setTenToaNha(req.getTenToaNha());
        tn.setDiaChi(req.getDiaChi());
        tn.setSoTang(req.getSoTang());
        return toaNhaRepository.save(tn);
    }

    public List<ToaNha> danhSachToaNha() {
        return toaNhaRepository.findAll();
    }

    /** Tao phong va tu dong sinh cac giuong theo so giuong toi da (ky hieu A, B, C...) */
    @Transactional
    public Phong taoPhong(TaoPhongRequest req) {
        ToaNha toaNha = toaNhaRepository.findById(req.getToaNhaId())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay toa nha"));

        Phong phong = new Phong();
        phong.setToaNha(toaNha);
        phong.setSoPhong(req.getSoPhong());
        phong.setTang(req.getTang());
        phong.setSoGiuongToiDa(req.getSoGiuongToiDa());
        phong.setGiaPhongThang(req.getGiaPhongThang());
        phong.setTrangThai(TrangThaiPhong.TRONG);
        phong = phongRepository.save(phong);

        for (int i = 0; i < req.getSoGiuongToiDa(); i++) {
            Giuong g = new Giuong();
            g.setPhong(phong);
            g.setKyHieu(String.valueOf((char) ('A' + i)));
            giuongRepository.save(g);
        }
        return phong;
    }

    /** So do phong/giuong theo toa nha - tra ve tinh trang trong theo thoi gian thuc */
    public List<Phong> soDoPhong(Long toaNhaId) {
        return phongRepository.findByToaNhaId(toaNhaId);
    }

    public List<Phong> danhSachPhongConTrong(Long toaNhaId) {
        return phongRepository.findByToaNhaId(toaNhaId).stream()
                .filter(p -> p.getSoChoTrong() > 0 && p.getTrangThai() != TrangThaiPhong.KHOA)
                .toList();
    }

    /** Cap nhat lai trang thai phong (TRONG/DAY) dua tren so giuong da o - goi sau khi xep/tra phong */
    @Transactional
    public void capNhatTrangThaiPhong(Phong phong) {
        if (phong.getTrangThai() == TrangThaiPhong.KHOA) return;
        if (phong.getSoChoTrong() <= 0) {
            phong.setTrangThai(TrangThaiPhong.DAY);
        } else {
            phong.setTrangThai(TrangThaiPhong.TRONG);
        }
        phongRepository.save(phong);
    }
}