package com.ktx.ql_ktx.dto;

import com.ktx.ql_ktx.entity.VaiTro;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NguoiDungInfoResponse {
    private Long id;
    private String tenDangNhap;
    private String hoTen;
    private String mssv;
    private String email;
    private String soDienThoai;
    private VaiTro vaiTro;
    private Long toaNhaPhuTrachId;
    private String tenToaNhaPhuTrach;
    private boolean active;

    // Constructor 6 tham so phuc vu cho MeController
    public NguoiDungInfoResponse(Long id, String tenDangNhap, String hoTen, VaiTro vaiTro, Long toaNhaPhuTrachId, String tenToaNhaPhuTrach) {
        this.id = id;
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;
        this.toaNhaPhuTrachId = toaNhaPhuTrachId;
        this.tenToaNhaPhuTrach = tenToaNhaPhuTrach;
    }
}