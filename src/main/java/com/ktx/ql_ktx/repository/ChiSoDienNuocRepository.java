package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.ChiSoDienNuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChiSoDienNuocRepository extends JpaRepository<ChiSoDienNuoc, Long> {
    Optional<ChiSoDienNuoc> findByPhongIdAndThangAndNam(Long phongId, Integer thang, Integer nam);
    List<ChiSoDienNuoc> findByThangAndNam(Integer thang, Integer nam);
    Optional<ChiSoDienNuoc> findTopByPhongIdOrderByNamDescThangDesc(Long phongId);
}
