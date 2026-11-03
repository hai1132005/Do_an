package com.ktx.ql_ktx.dto;

import com.ktx.ql_ktx.entity.TrangThaiPhong;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Thay cho viec tra thang Entity Phong (truoc day dua vao 2 ham
 * @Transient tren Entity), DTO nay duoc PhongService tu tinh toan so
 * giuong da o / con trong qua GiuongService (dua tren hop dong con hieu
 * luc) roi moi tra ve cho Controller - giu dung nguyen tac khong tinh
 * nghiep vu ngay trong Entity. Cac truong deu phang (khong long
 * toaNha.tenToaNha) de khop voi cach frontend (app.js) dang doc du lieu
 * so do phong / phong con trong.
 */
@Getter
@Setter
@AllArgsConstructor
public class PhongTinhTrangResponse {
    private Long id;
    private String tenToaNha;
    private String soPhong;
    private Integer tang;
    private BigDecimal giaPhongThang;
    private Integer soGiuongToiDa;
    private int soGiuongDaO;
    private int soChoTrong;
    private TrangThaiPhong trangThai;
}
