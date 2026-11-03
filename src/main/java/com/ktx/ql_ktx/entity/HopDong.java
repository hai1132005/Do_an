package com.ktx.ql_ktx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Hop dong o la NGUON SU THAT DUY NHAT (single source of truth) cho viec
 * "giuong nao dang co nguoi o, ai dang o, o tu ngay nao" - thay cho cach
 * cu gan thang sinh_vien_id len bang Giuong (diem 3 nhan xet cua GVHD).
 *
 * Mot giuong co the co NHIEU hop dong theo thoi gian (sinh vien A o tu
 * 01/09 den 15/12, sau do sinh vien B vao o tu 20/12...), mien la tai
 * mot thoi diem chi co TOI DA 1 hop dong con_hieu_luc = true cho cung
 * 1 giuong (rang buoc nay duoc kiem tra o tang Service, xem
 * DangKyService.xetDuyet() va CuDanService).
 */
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

    // Ngay du kien ket thuc (co the null = chua xac dinh truoc)
    private LocalDate ngayKetThuc;

    // Ngay THUC TE sinh vien tra phong - dung de tinh dung so ngay o
    // trong thang khi chia hoa don, khac voi ngayKetThuc (du kien)
    private LocalDate ngayTraThucTe;

    @Column(nullable = false)
    private boolean conHieuLuc = true;

    /**
     * Ly do ket thuc hop dong: TRA_PHONG (tra phong han) hoac
     * CHUYEN_PHONG (chuyen sang giuong khac) - null neu hop dong con
     * hieu luc.
     */
    @Enumerated(EnumType.STRING)
    private LyDoKetThucHopDong lyDoKetThuc;

    /**
     * Tinh so ngay hop dong nay "chong lap" (overlap) voi 1 khoang thoi
     * gian cho truoc (thuong la khoang ngay 1 den ngay cuoi cua 1 thang
     * hoa don). Dung de chia tien phong/dien/nuoc theo dung so ngay thuc
     * te sinh vien da o trong thang do - xem HoaDonCaNhanService.
     */
    @Transient
    public long soNgayChongLap(LocalDate tuNgay, LocalDate denNgay) {
        LocalDate batDauThucTe = ngayBatDau.isAfter(tuNgay) ? ngayBatDau : tuNgay;
        LocalDate ketThucMoc = ngayTraThucTe != null ? ngayTraThucTe
                : (ngayKetThuc != null ? ngayKetThuc : denNgay);
        LocalDate ketThucThucTe = ketThucMoc.isBefore(denNgay) ? ketThucMoc : denNgay;

        if (ketThucThucTe.isBefore(batDauThucTe)) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(batDauThucTe, ketThucThucTe) + 1;
    }
}
