package com.ktx.ql_ktx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Hoa don hang thang cho 1 phong: tien phong (chia deu theo so nguoi o) + tien dien + tien nuoc.
 */
@Entity
@Table(name = "hoa_don")
@Getter
@Setter
@NoArgsConstructor
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_id", nullable = false)
    private Phong phong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chi_so_id")
    private ChiSoDienNuoc chiSo;

    @Column(nullable = false)
    private Integer thang;

    @Column(nullable = false)
    private Integer nam;

    @Column(nullable = false)
    private BigDecimal tienPhong;

    @Column(nullable = false)
    private BigDecimal tienDien;

    @Column(nullable = false)
    private BigDecimal tienNuoc;

    @Column(nullable = false)
    private BigDecimal tongTien;

    @Column(nullable = false)
    private LocalDate hanThanhToan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrangThaiHoaDon trangThai = TrangThaiHoaDon.CHUA_THANH_TOAN;

    private LocalDate ngayThanhToan;
}