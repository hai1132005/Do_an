package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.BacGiaDien;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BacGiaDienRepository extends JpaRepository<BacGiaDien, Long> {
    List<BacGiaDien> findAllByOrderByBacThuAsc();
}
