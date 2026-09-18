package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.HoaDon;
import com.ktx.ql_ktx.entity.TrangThaiHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {
    List<HoaDon> findByPhongId(Long phongId);
    List<HoaDon> findByThangAndNam(Integer thang, Integer nam);
    List<HoaDon> findByTrangThai(TrangThaiHoaDon trangThai);
    List<HoaDon> findByPhongIdIn(List<Long> phongIds);
}
