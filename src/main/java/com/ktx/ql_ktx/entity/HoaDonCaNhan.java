package com.ktx.ql_ktx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * BO SUNG MOI (diem 3 nhan xet cua thay): "hoa don dien nuoc dang tinh
 * theo PHONG, khong chia theo tung sinh vien; khong co cong no ca nhan".
 *
 * HoaDon (entity cu) van la hoa don CUA CA PHONG trong 1 thang - khong
 * doi, van do ChiSoService tao ra nhu truoc. HoaDonCaNhan la phan tien
 * cua TUNG sinh vien trong 1 HoaDon phong do, duoc HoaDonCaNhanService
 * tu dong tinh ngay sau khi hoa don phong duoc tao, bang cach CHIA tong
 * tien theo ty le so ngay moi sinh vien THUC SU o trong thang do (tinh
 * qua HopDong.soNgayChongLap) - khong chia deu theo dau nguoi mot cach
 * may moc.
 *
 * Moi sinh vien tu xem va tu thanh toan dung phan hoa don ca nhan cua
 * minh; "cong no cua tung sinh vien" = tong cac HoaDonCaNhan chua thanh
 * toan cua sinh vien do (xem HoaDonCaNhanService.tongCongNoCuaSinhVien).
 */
@Entity
@Table(name = "hoa_don_ca_nhan")
@Getter
@Setter
@NoArgsConstructor
public class HoaDonCaNhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_phong_id", nullable = false)
    private HoaDon hoaDonPhong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinh_vien_id", nullable = false)
    private NguoiDung sinhVien;

    @Column(nullable = false)
    private Integer soNgayO; // so ngay sinh vien nay o trong thang cua hoa don

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

    // Ma giao dich VNPay (sandbox) neu thanh toan qua cong VNPay
    private String maGiaoDichVnPay;
}
