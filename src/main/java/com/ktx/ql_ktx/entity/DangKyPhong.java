package com.ktx.ql_ktx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "dang_ky_phong")
@Getter
@Setter
@NoArgsConstructor
public class DangKyPhong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinh_vien_id", nullable = false)
    private NguoiDung sinhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_id", nullable = false)
    private Phong phong;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrangThaiDangKy trangThai = TrangThaiDangKy.CHO_DUYET;

    private LocalDateTime ngayDangKy = LocalDateTime.now();

    private LocalDateTime ngayXetDuyet;

    // Nguoi (quan ly toa nha) da xet duyet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_duyet_id")
    private NguoiDung nguoiDuyet;

    private String ghiChu; // ly do tu choi (neu co)
}