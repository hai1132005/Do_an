package com.ktx.ql_ktx.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SUA SO VOI BAN GOC (theo gop y cua GVHD - diem 3): ban cu gan Giuong
 * 1-1 truc tiep voi NguoiDung (cot sinh_vien_id), nen KHONG luu duoc
 * lich su: khong biet sinh vien nao da tung o giuong nay, o tu ngay nao
 * den ngay nao, khong ho tro "chuyen phong" hay "tra phong roi co nguoi
 * khac vao o lai" ma van giu dau vet.
 *
 * Ban moi: Giuong KHONG con luu truc tiep ai dang o. Tinh trang "dang co
 * nguoi o hay khong" va "ai dang o" duoc suy ra DONG tu bang HOP_DONG
 * (tim hop dong co con_hieu_luc = true ung voi giuong nay) - xem
 * GiuongService. Nho vay mot giuong co the co nhieu hop dong noi tiep
 * nhau theo thoi gian ma khong mat lich su cua hop dong truoc.
 */
@Entity
@Table(name = "giuong")
@Getter
@Setter
@NoArgsConstructor
public class Giuong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_id", nullable = false)
    private Phong phong;

    @Column(nullable = false, length = 10)
    private String kyHieu; // vd: A, B, C, D trong 1 phong
}
