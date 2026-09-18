package com.ktx.ql_ktx.controller;

import com.ktx.ql_ktx.dto.NguoiDungInfoResponse;
import com.ktx.ql_ktx.entity.NguoiDung;
import com.ktx.ql_ktx.repository.NguoiDungRepository;
import com.ktx.ql_ktx.security.NguoiDungPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API dung chung cho ca 3 vai tro: lay thong tin ban than dang dang nhap
 * (dung de frontend biet hien thi menu nao, toa nha phu trach la gi...).
 */
@RestController
@RequestMapping("/api/nguoi-dung")
public class MeController {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @GetMapping("/toi")
    public ResponseEntity<?> thongTinCuaToi(Authentication auth) {
        String tenDangNhap = ((NguoiDungPrincipal) auth.getPrincipal()).getUsername();
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new IllegalStateException("Khong xac dinh duoc nguoi dung"));

        Long toaNhaId = nd.getToaNhaPhuTrach() != null ? nd.getToaNhaPhuTrach().getId() : null;
        String toaNhaTen = nd.getToaNhaPhuTrach() != null ? nd.getToaNhaPhuTrach().getTenToaNha() : null;

        return ResponseEntity.ok(new NguoiDungInfoResponse(
                nd.getId(), nd.getHoTen(), nd.getMssv(), nd.getVaiTro(), toaNhaId, toaNhaTen));
    }
}
