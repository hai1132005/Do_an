package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.Giuong;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GiuongRepository extends JpaRepository<Giuong, Long> {
    List<Giuong> findByPhongId(Long phongId);
    Optional<Giuong> findBySinhVienId(Long sinhVienId);
    List<Giuong> findByPhongIdAndSinhVienIsNull(Long phongId);
    List<Giuong> findByPhongToaNhaIdAndSinhVienIsNotNull(Long toaNhaId);
}