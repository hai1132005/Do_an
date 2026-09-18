package com.ktx.ql_ktx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "vi_pham")
@Getter
@Setter
@NoArgsConstructor
public class ViPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinh_vien_id", nullable = false)
    private NguoiDung sinhVien;

    @Column(nullable = false)
    private String noiDung;

    private LocalDate ngayViPham = LocalDate.now();

    private String hinhThucXuLy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_lap_id")
    private NguoiDung nguoiLap;
}