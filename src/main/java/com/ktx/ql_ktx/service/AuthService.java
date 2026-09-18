package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.dto.DangKySinhVienRequest;
import com.ktx.ql_ktx.dto.DangNhapRequest;
import com.ktx.ql_ktx.dto.DangNhapResponse;
import com.ktx.ql_ktx.dto.TaoNhanVienRequest;
import com.ktx.ql_ktx.entity.NguoiDung;
import com.ktx.ql_ktx.entity.ToaNha;
import com.ktx.ql_ktx.entity.VaiTro;
import com.ktx.ql_ktx.repository.NguoiDungRepository;
import com.ktx.ql_ktx.repository.ToaNhaRepository;
import com.ktx.ql_ktx.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private ToaNhaRepository toaNhaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /** Ban quan ly tao tai khoan cho nhan vien Quan ly toa nha */
    @Transactional
    public NguoiDung taoNhanVienQuanLy(TaoNhanVienRequest req) {
        if (nguoiDungRepository.existsByTenDangNhap(req.getTenDangNhap())) {
            throw new IllegalArgumentException("Ten dang nhap da ton tai");
        }
        ToaNha toaNha = toaNhaRepository.findById(req.getToaNhaPhuTrachId())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay toa nha"));

        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap(req.getTenDangNhap());
        nd.setMatKhau(passwordEncoder.encode(req.getMatKhau()));
        nd.setHoTen(req.getHoTen());
        nd.setEmail(req.getEmail());
        nd.setSoDienThoai(req.getSoDienThoai());
        nd.setVaiTro(VaiTro.QUAN_LY_TOA_NHA);
        nd.setToaNhaPhuTrach(toaNha);

        return nguoiDungRepository.save(nd);
    }

    /** Sinh vien tu dang ky tai khoan */
    @Transactional
    public NguoiDung dangKySinhVien(DangKySinhVienRequest req) {
        if (nguoiDungRepository.existsByTenDangNhap(req.getTenDangNhap())) {
            throw new IllegalArgumentException("Ten dang nhap da ton tai");
        }
        if (nguoiDungRepository.existsByMssv(req.getMssv())) {
            throw new IllegalArgumentException("MSSV da duoc dang ky");
        }

        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap(req.getTenDangNhap());
        nd.setMatKhau(passwordEncoder.encode(req.getMatKhau()));
        nd.setHoTen(req.getHoTen());
        nd.setMssv(req.getMssv());
        nd.setEmail(req.getEmail());
        nd.setSoDienThoai(req.getSoDienThoai());
        nd.setVaiTro(VaiTro.SINH_VIEN);

        return nguoiDungRepository.save(nd);
    }

    public DangNhapResponse dangNhap(DangNhapRequest req) {
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(req.getTenDangNhap())
                .orElseThrow(() -> new IllegalArgumentException("Sai ten dang nhap hoac mat khau"));

        if (!passwordEncoder.matches(req.getMatKhau(), nd.getMatKhau())) {
            throw new IllegalArgumentException("Sai ten dang nhap hoac mat khau");
        }
        if (!nd.isActive()) {
            throw new IllegalArgumentException("Tai khoan da bi khoa");
        }

        String token = jwtUtil.taoToken(nd.getTenDangNhap(), nd.getVaiTro().name());
        return new DangNhapResponse(token, nd.getHoTen(), nd.getVaiTro());
    }
}