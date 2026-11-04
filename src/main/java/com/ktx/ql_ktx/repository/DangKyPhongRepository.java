package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.DangKyPhong;
import com.ktx.ql_ktx.entity.TrangThaiDangKy;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DangKyPhongRepository extends JpaRepository<DangKyPhong, Long> {
    List<DangKyPhong> findBySinhVienId(Long sinhVienId);
    List<DangKyPhong> findByTrangThai(TrangThaiDangKy trangThai);
    List<DangKyPhong> findByPhongToaNhaIdAndTrangThai(Long toaNhaId, TrangThaiDangKy trangThai);

    /** Khoa dong (SELECT ... FOR UPDATE) de 2 yeu cau duyet cung luc khong cung xu ly 1 don */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DangKyPhong d where d.id = :id")
    Optional<DangKyPhong> findByIdForUpdate(@Param("id") Long id);
}