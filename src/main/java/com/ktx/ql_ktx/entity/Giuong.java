package com.ktx.ql_ktx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "giuong")
@Getter
@Setter
@NoArgsConstructor
public class Giuong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_id", nullable = false)
    @JsonIgnore
    private Phong phong;

    @Column(nullable = false, length = 10)
    private String kyHieu; // vd: A, B, C, D trong 1 phong

    // Sinh vien dang o giuong nay, null neu con trong
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinh_vien_id")
    private NguoiDung sinhVien;
}