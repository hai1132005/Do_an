package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.DangKyPhong;
import com.ktx.ql_ktx.entity.TrangThaiDangKy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DangKyPhongRepository extends JpaRepository<DangKyPhong, Long> {
    List<DangKyPhong> findBySinhVienId(Long sinhVienId);
    List<DangKyPhong> findByTrangThai(TrangThaiDangKy trangThai);
    List<DangKyPhong> findByPhongToaNhaIdAndTrangThai(Long toaNhaId, TrangThaiDangKy trangThai);
}
