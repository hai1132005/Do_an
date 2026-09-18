package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.dto.TaoViPhamRequest;
import com.ktx.ql_ktx.entity.NguoiDung;
import com.ktx.ql_ktx.entity.ViPham;
import com.ktx.ql_ktx.repository.GiuongRepository;
import com.ktx.ql_ktx.repository.NguoiDungRepository;
import com.ktx.ql_ktx.repository.ViPhamRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Quan ly vi pham noi quy ky tuc xa (theo yeu cau de bai:
 * "hoa don - cong no va VI PHAM NOI QUY").
 */
@Service
public class ViPhamService {

    @Autowired private ViPhamRepository viPhamRepository;
    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private GiuongRepository giuongRepository;

    /** Quan ly toa nha lap bien ban vi pham cho 1 sinh vien */
    @Transactional
    public ViPham lapViPham(TaoViPhamRequest req, Long nguoiLapId) {
        NguoiDung sv = nguoiDungRepository.findById(req.getSinhVienId())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay sinh vien"));
        NguoiDung nguoiLap = nguoiDungRepository.findById(nguoiLapId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi lap"));

        if (req.getNoiDung() == null || req.getNoiDung().isBlank()) {
            throw new IllegalArgumentException("Noi dung vi pham khong duoc de trong");
        }

        ViPham vp = new ViPham();
        vp.setSinhVien(sv);
        vp.setNoiDung(req.getNoiDung());
        vp.setHinhThucXuLy(req.getHinhThucXuLy());
        vp.setNgayViPham(LocalDate.now());
        vp.setNguoiLap(nguoiLap);
        return viPhamRepository.save(vp);
    }

    /** Danh sach vi pham cua toan bo sinh vien dang o trong 1 toa nha */
    public List<ViPham> viPhamTheoToaNha(Long toaNhaId) {
        List<Long> svIds = giuongRepository.findByPhongToaNhaIdAndSinhVienIsNotNull(toaNhaId)
                .stream().map(g -> g.getSinhVien().getId()).toList();
        if (svIds.isEmpty()) return List.of();
        return viPhamRepository.findBySinhVienIdIn(svIds);
    }

    public List<ViPham> viPhamCuaSinhVien(Long sinhVienId) {
        return viPhamRepository.findBySinhVienId(sinhVienId);
    }

    @Transactional
    public void xoaViPham(Long id) {
        if (!viPhamRepository.existsById(id)) {
            throw new IllegalArgumentException("Khong tim thay bien ban vi pham");
        }
        viPhamRepository.deleteById(id);
    }
}