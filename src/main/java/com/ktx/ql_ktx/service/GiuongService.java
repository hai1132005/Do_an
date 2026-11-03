package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.entity.Giuong;
import com.ktx.ql_ktx.entity.HopDong;
import com.ktx.ql_ktx.entity.NguoiDung;
import com.ktx.ql_ktx.repository.GiuongRepository;
import com.ktx.ql_ktx.repository.HopDongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * BO SUNG MOI: thay cho viec doc truc tiep Giuong.sinhVien (da bo, xem
 * Giuong.java), tat ca truy van "giuong nay co dang trong khong, ai dang
 * o" deu di qua Service nay, luon tra ve dung trang thai tai THOI DIEM
 * HIEN TAI bang cach tim hop dong con_hieu_luc = true gan voi giuong do.
 */
@Service
public class GiuongService {

    @Autowired private GiuongRepository giuongRepository;
    @Autowired private HopDongRepository hopDongRepository;

    /** Giuong con trong hay khong (khong co hop dong nao dang hieu luc) */
    public boolean conTrong(Long giuongId) {
        return hopDongRepository.findByGiuongIdAndConHieuLucTrue(giuongId).isEmpty();
    }

    /** Sinh vien dang o giuong nay (rong neu giuong dang trong) */
    public Optional<NguoiDung> sinhVienDangO(Long giuongId) {
        return hopDongRepository.findByGiuongIdAndConHieuLucTrue(giuongId)
                .map(HopDong::getSinhVien);
    }

    /** Danh sach giuong con trong trong 1 phong */
    public List<Giuong> giuongConTrong(Long phongId) {
        return giuongRepository.findByPhongId(phongId).stream()
                .filter(g -> conTrong(g.getId()))
                .toList();
    }

    public int soGiuongConTrong(Long phongId) {
        return giuongConTrong(phongId).size();
    }

    public int soGiuongDaO(Long phongId) {
        List<Giuong> ds = giuongRepository.findByPhongId(phongId);
        return (int) ds.stream().filter(g -> !conTrong(g.getId())).count();
    }
}
