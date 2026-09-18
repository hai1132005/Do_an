package com.ktx.ql_ktx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ghi chi so dien nuoc hang thang cho 1 phong.
 * Moi phong - moi thang chi co 1 ban ghi (unique thang+nam+phong).
 */
@Entity
@Table(name = "chi_so_dien_nuoc", uniqueConstraints = @UniqueConstraint(columnNames = {"phong_id", "thang", "nam"}))
@Getter
@Setter
@NoArgsConstructor
public class ChiSoDienNuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_id", nullable = false)
    private Phong phong;

    @Column(nullable = false)
    private Integer thang; // 1-12

    @Column(nullable = false)
    private Integer nam;

    @Column(nullable = false)
    private Double chiSoDienCu;

    @Column(nullable = false)
    private Double chiSoDienMoi;

    @Column(nullable = false)
    private Double chiSoNuocCu;

    @Column(nullable = false)
    private Double chiSoNuocMoi;

    // Nguoi ghi (quan ly toa nha)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_ghi_id")
    private NguoiDung nguoiGhi;

    @Transient
    public double getSoDienTieuThu() {
        return chiSoDienMoi - chiSoDienCu;
    }

    @Transient
    public double getSoNuocTieuThu() {
        return chiSoNuocMoi - chiSoNuocCu;
    }
}