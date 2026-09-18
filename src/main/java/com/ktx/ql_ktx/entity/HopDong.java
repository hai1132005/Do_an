package com.ktx.ql_ktx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "hop_dong")
@Getter
@Setter
@NoArgsConstructor
public class HopDong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinh_vien_id", nullable = false)
    private NguoiDung sinhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giuong_id", nullable = false)
    private Giuong giuong;

    @Column(nullable = false)
    private LocalDate ngayBatDau;

    private LocalDate ngayKetThuc; // null neu con hieu luc

    @Column(nullable = false)
    private boolean conHieuLuc = true;
}