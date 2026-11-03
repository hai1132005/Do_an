package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.dto.PhongTinhTrangResponse;
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
 *
 * SUA SO VOI BAN GOC: so giuong da o / con trong khong con doc truc tiep
 * tu Entity (vi Giuong da bo truong sinhVien), ma duoc GiuongService tinh
 * dong tu cac hop dong con hieu luc - xem toResponse() -> PhongTinhTrangResponse.
 */
@Service
public class PhongService {

    @Autowired private ToaNhaRepository toaNhaRepository;
    @Autowired private PhongRepository phongRepository;
    @Autowired private GiuongRepository giuongRepository;
    @Autowired private GiuongService giuongService;

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

    private PhongTinhTrangResponse toResponse(Phong p) {
        int daO = giuongService.soGiuongDaO(p.getId());
        int conTrong = p.getSoGiuongToiDa() - daO;
        return new PhongTinhTrangResponse(
                p.getId(), p.getToaNha().getTenToaNha(), p.getSoPhong(), p.getTang(),
                p.getGiaPhongThang(), p.getSoGiuongToiDa(), daO, conTrong, p.getTrangThai());
    }

    /** So do phong/giuong theo toa nha - tra ve tinh trang trong theo thoi gian thuc */
    public List<PhongTinhTrangResponse> soDoPhong(Long toaNhaId) {
        return phongRepository.findByToaNhaId(toaNhaId).stream().map(this::toResponse).toList();
    }

    public List<PhongTinhTrangResponse> danhSachPhongConTrong(Long toaNhaId) {
        return phongRepository.findByToaNhaId(toaNhaId).stream()
                .map(this::toResponse)
                .filter(dto -> dto.getSoChoTrong() > 0 && dto.getTrangThai() != TrangThaiPhong.KHOA)
                .toList();
    }

    public Phong layPhong(Long phongId) {
        return phongRepository.findById(phongId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong"));
    }

    /** Cap nhat lai trang thai phong (TRONG/DAY) dua tren so giuong da o - goi sau khi xep/tra/chuyen giuong */
    @Transactional
    public void capNhatTrangThaiPhong(Phong phong) {
        if (phong.getTrangThai() == TrangThaiPhong.KHOA) return;
        int conTrong = phong.getSoGiuongToiDa() - giuongService.soGiuongDaO(phong.getId());
        phong.setTrangThai(conTrong <= 0 ? TrangThaiPhong.DAY : TrangThaiPhong.TRONG);
        phongRepository.save(phong);
    }
}
