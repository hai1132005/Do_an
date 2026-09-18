package com.ktx.ql_ktx.controller;

import com.ktx.ql_ktx.dto.DangKySinhVienRequest;
import com.ktx.ql_ktx.dto.DangNhapRequest;
import com.ktx.ql_ktx.entity.NguoiDung;
import com.ktx.ql_ktx.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/dang-ky")
    public ResponseEntity<?> dangKy(@Valid @RequestBody DangKySinhVienRequest req) {
        NguoiDung nd = authService.dangKySinhVien(req);
        return ResponseEntity.ok().body("Dang ky thanh cong. Tai khoan ID: " + nd.getId());
    }

    @PostMapping("/dang-nhap")
    public ResponseEntity<?> dangNhap(@RequestBody DangNhapRequest req) {
        return ResponseEntity.ok(authService.dangNhap(req));
    }
}