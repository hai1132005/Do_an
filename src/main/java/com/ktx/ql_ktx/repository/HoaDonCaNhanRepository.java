package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.HoaDonCaNhan;
import com.ktx.ql_ktx.entity.TrangThaiHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoaDonCaNhanRepository extends JpaRepository<HoaDonCaNhan, Long> {
    List<HoaDonCaNhan> findByHoaDonPhongId(Long hoaDonPhongId);
    List<HoaDonCaNhan> findBySinhVienId(Long sinhVienId);
    List<HoaDonCaNhan> findBySinhVienIdAndTrangThaiNot(Long sinhVienId, TrangThaiHoaDon trangThai);
    List<HoaDonCaNhan> findByTrangThai(TrangThaiHoaDon trangThai);
}
