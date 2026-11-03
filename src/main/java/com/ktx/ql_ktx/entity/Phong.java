package com.ktx.ql_ktx.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * SUA SO VOI BAN GOC: da bo 2 ham tien ich getSoGiuongDaO()/getSoChoTrong()
 * tinh truc tiep qua Giuong.sinhVien (vi truong do khong con ton tai).
 * Viec tinh so giuong trong/da o gio phai di qua GiuongService (dua vao
 * hop dong con hieu luc), vi do la nghiep vu can Repository nen khong
 * the dat lam ham @Transient ngay trong Entity duoc nua - xem
 * PhongTinhTrangResponse / PhongService.
 */
@Entity
@Table(name = "phong", uniqueConstraints = @UniqueConstraint(columnNames = {"toa_nha_id", "soPhong"}))
@Getter
@Setter
@NoArgsConstructor
public class Phong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toa_nha_id", nullable = false)
    private ToaNha toaNha;

    @Column(nullable = false, length = 20)
    private String soPhong; // vd: 101, 202

    private Integer tang;

    @Column(nullable = false)
    private Integer soGiuongToiDa; // suc chua

    @Column(nullable = false)
    private BigDecimal giaPhongThang; // tien phong / thang

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrangThaiPhong trangThai = TrangThaiPhong.TRONG;

    @JsonIgnore
    @OneToMany(mappedBy = "phong", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Giuong> danhSachGiuong = new ArrayList<>();
}
