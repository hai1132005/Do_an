package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.NguoiDung;
import com.ktx.ql_ktx.entity.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    Optional<NguoiDung> findByTenDangNhap(String tenDangNhap);
    boolean existsByTenDangNhap(String tenDangNhap);
    boolean existsByMssv(String mssv);
    List<NguoiDung> findByVaiTro(VaiTro vaiTro);
}
