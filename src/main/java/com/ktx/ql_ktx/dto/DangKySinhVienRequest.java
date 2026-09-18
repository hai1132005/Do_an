package com.ktx.ql_ktx.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DangKySinhVienRequest {
    @NotBlank
    private String tenDangNhap;
    @NotBlank
    private String matKhau;
    @NotBlank
    private String hoTen;
    @NotBlank
    private String mssv;
    private String email;
    private String soDienThoai;
}
