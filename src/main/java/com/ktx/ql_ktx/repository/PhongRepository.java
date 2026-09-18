package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.Phong;
import com.ktx.ql_ktx.entity.TrangThaiPhong;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PhongRepository extends JpaRepository<Phong, Long> {
    List<Phong> findByToaNhaId(Long toaNhaId);
    List<Phong> findByTrangThai(TrangThaiPhong trangThai);
    List<Phong> findByToaNhaIdAndTrangThai(Long toaNhaId, TrangThaiPhong trangThai);
}
