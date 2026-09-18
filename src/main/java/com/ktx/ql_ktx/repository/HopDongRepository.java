package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.HopDong;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HopDongRepository extends JpaRepository<HopDong, Long> {
    List<HopDong> findBySinhVienId(Long sinhVienId);
    List<HopDong> findByConHieuLucTrue();
    List<HopDong> findByGiuongPhongToaNhaIdAndConHieuLucTrue(Long toaNhaId);
}