package com.ktx.ql_ktx.dto;

import com.ktx.ql_ktx.entity.VaiTro;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DangNhapResponse {
    private String token;
    private String hoTen;
    private VaiTro vaiTro;
}
