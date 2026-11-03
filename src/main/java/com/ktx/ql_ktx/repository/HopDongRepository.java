package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.HopDong;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HopDongRepository extends JpaRepository<HopDong, Long> {

    List<HopDong> findBySinhVienId(Long sinhVienId);

    List<HopDong> findByConHieuLucTrue();

    // Hop dong dang hieu luc cua 1 giuong - toi da 1 ket qua tai 1 thoi diem
    Optional<HopDong> findByGiuongIdAndConHieuLucTrue(Long giuongId);

    // Toan bo hop dong (ke ca da ket thuc) tung gan voi 1 giuong - dung de
    // xem lai lich su cua rieng 1 giuong
    List<HopDong> findByGiuongId(Long giuongId);

    // Toan bo hop dong dang hieu luc cua ca 1 toa nha - dung cho trang
    // "Sinh vien dang o"
    List<HopDong> findByGiuongPhongToaNhaIdAndConHieuLucTrue(Long toaNhaId);

    // Hop dong (ke ca da ket thuc) cua toan bo giuong trong 1 phong, dung de
    // chia hoa don thang: can biet AI da o phong nay trong thang do, du
    // con hieu luc hay khong
    List<HopDong> findByGiuongPhongId(Long phongId);
}
