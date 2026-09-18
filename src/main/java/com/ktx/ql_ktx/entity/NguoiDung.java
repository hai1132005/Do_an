package com.ktx.ql_ktx.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "nguoi_dung")
@Getter
@Setter
@NoArgsConstructor
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String tenDangNhap;

    @JsonIgnore
    @Column(nullable = false)
    private String matKhau; // da ma hoa BCrypt

    @Column(nullable = false, length = 100)
    private String hoTen;

    @Column(unique = true, length = 20)
    private String mssv; // ma so sinh vien, null neu la nhan vien quan ly

    private String email;

    private String soDienThoai;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VaiTro vaiTro;

    // Neu la QUAN_LY_TOA_NHA thi gan voi 1 toa nha phu trach (co the null neu la BAN_QUAN_LY / SINH_VIEN)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toa_nha_phu_trach_id")
    private ToaNha toaNhaPhuTrach;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime ngayTao = LocalDateTime.now();
}
