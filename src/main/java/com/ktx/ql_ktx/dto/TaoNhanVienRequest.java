package com.ktx.ql_ktx.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaoNhanVienRequest {
    private String tenDangNhap;
    private String matKhau;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private Long toaNhaPhuTrachId;
}