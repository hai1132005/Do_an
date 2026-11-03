package com.ktx.ql_ktx.controller;

import com.ktx.ql_ktx.service.HoaDonCaNhanService;
import com.ktx.ql_ktx.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * BO SUNG MOI (diem 3/4 nhan xet cua thay): endpoint ma VNPay sandbox se
 * REDIRECT trinh duyet cua sinh vien ve sau khi thanh toan xong. Trinh
 * duyet goi thang URL nay (khong kem JWT), nen phai la endpoint CONG
 * KHAI (xem SecurityConfig: /api/thanh-toan/** permitAll) - viec xac
 * thuc an toan nam o chu ky HMAC-SHA512 (vnp_SecureHash) do VnPayService
 * kiem tra, khong phai o JWT.
 */
@RestController
@RequestMapping("/api/thanh-toan")
public class PaymentController {

    @Autowired private VnPayService vnPayService;
    @Autowired private HoaDonCaNhanService hoaDonCaNhanService;

    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> vnPayCallback(HttpServletRequest request) {
        Map<String, String> thamSo = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> thamSo.put(k, v.length > 0 ? v[0] : ""));

        if (!vnPayService.xacThucChuKy(thamSo)) {
            return ResponseEntity.badRequest().body("{\"thongBao\":\"Chu ky khong hop le\"}");
        }

        String maPhanHoi = thamSo.get("vnp_ResponseCode");
        String txnRef = thamSo.get("vnp_TxnRef"); // dang "HDCN{id}_{timestamp}"

        if (!"00".equals(maPhanHoi) || txnRef == null || !txnRef.startsWith("HDCN")) {
            return ResponseEntity.ok().body("{\"thongBao\":\"Thanh toan khong thanh cong hoac da bi huy\"}");
        }

        try {
            String phanId = txnRef.substring(4, txnRef.indexOf('_'));
            Long hoaDonCaNhanId = Long.parseLong(phanId);
            String maGiaoDich = thamSo.get("vnp_TransactionNo");
            hoaDonCaNhanService.xacNhanDaThanhToan(hoaDonCaNhanId, maGiaoDich);
            return ResponseEntity.ok().body("{\"thongBao\":\"Thanh toan thanh cong, da ghi nhan cong no\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"thongBao\":\"Khong xac dinh duoc hoa don can thanh toan\"}");
        }
    }
}
