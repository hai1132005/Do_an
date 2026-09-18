package com.ktx.ql_ktx.repository;

import com.ktx.ql_ktx.entity.ToaNha;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToaNhaRepository extends JpaRepository<ToaNha, Long> {
    boolean existsByTenToaNha(String tenToaNha);
}
