package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.ViPham;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ViPhamRepository extends JpaRepository<ViPham, Long> {
    List<ViPham> findBySinhVienId(Long sinhVienId);
    List<ViPham> findBySinhVienIdIn(List<Long> sinhVienIds);
}