package com.ktx.ql_ktx.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    /** So giuong dang co sinh vien o (tinh dong, khong luu DB) */
    @Transient
    public int getSoGiuongDaO() {
        if (danhSachGiuong == null) return 0;
        return (int) danhSachGiuong.stream().filter(g -> g.getSinhVien() != null).count();
    }

    @Transient
    public int getSoChoTrong() {
        return soGiuongToiDa - getSoGiuongDaO();
    }
}
