package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.Giuong;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * SUA SO VOI BAN GOC: cac truy van dua tren Giuong.sinhVien (findBySinhVienId,
 * findByPhongIdAndSinhVienIsNull, findByPhongToaNhaIdAndSinhVienIsNotNull) da
 * bi xoa vi truong do khong con ton tai tren Giuong - xem Giuong.java va
 * GiuongService (thay the hoan toan cho cac truy van nay).
 */
public interface GiuongRepository extends JpaRepository<Giuong, Long> {
    List<Giuong> findByPhongId(Long phongId);
}
