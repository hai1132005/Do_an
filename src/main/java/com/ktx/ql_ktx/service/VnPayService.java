package com.ktx.ql_ktx.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * BO SUNG MOI (diem 3/4 nhan xet cua thay): tich hop VNPay SANDBOX de
 * sinh vien thanh toan hoa don ca nhan (HoaDonCaNhan) truc tuyen, thay
 * vi chi "xac nhan thanh toan thu cong" nhu HoaDonService cu.
 *
 * Day la cong thanh toan SANDBOX cua VNPay (moi truong thu nghiem danh
 * cho sinh vien/doanh nghiep tich hop, khong phai tien that) - dung
 * dung tai lieu tich hop chinh thuc: ky URL bang HMAC-SHA512 tren bo
 * tham so vnp_*, sap xep theo thu tu alphabet cua key.
 * Tai lieu: https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html
 */
@Service
public class VnPayService {

    @Value("${vnpay.tmn-code:SANDBOXCODE}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:SANDBOXSECRET}")
    private String hashSecret;

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    @Value("${vnpay.return-url:http://localhost:8081/api/thanh-toan/vnpay-callback}")
    private String returnUrl;

    /**
     * Tao URL thanh toan VNPay sandbox cho 1 hoa don ca nhan.
     * soTien: don vi VND (VNPay yeu cau nhan 100 theo quy dinh cua ho).
     */
    public String taoUrlThanhToan(Long hoaDonCaNhanId, BigDecimal soTien, String diaChiIp) {
        SortedMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", soTien.multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", "HDCN" + hoaDonCaNhanId + "_" + System.currentTimeMillis());
        params.put("vnp_OrderInfo", "Thanh toan hoa don ky tuc xa #" + hoaDonCaNhanId);
        params.put("vnp_OrderType", "billpayment");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", (diaChiIp == null || diaChiIp.isBlank()) ? "127.0.0.1" : diaChiIp);
        params.put("vnp_CreateDate", LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        // Theo tai lieu/demo chinh thuc cua VNPay: chuoi tinh checksum PHAI lay tu cac
        // gia tri DA URL-encode (giong het chuoi query gui di), khong phai gia tri tho.
        String query = buildQuery(params);
        String secureHash = hmacSha512(hashSecret, query);

        return payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    /** Xac minh chu ky tra ve tu VNPay (callback) truoc khi ghi nhan da thanh toan. */
    public boolean xacThucChuKy(Map<String, String> thamSo) {
        String chuKyNhanDuoc = thamSo.get("vnp_SecureHash");
        if (chuKyNhanDuoc == null) return false;

        SortedMap<String, String> params = new TreeMap<>(thamSo);
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        // Tham so servlet da duoc giai ma, nen encode lai dung cach luc tao URL truoc khi ky.
        String chuKyTinhLai = hmacSha512(hashSecret, buildQuery(params));
        return chuKyTinhLai.equalsIgnoreCase(chuKyNhanDuoc);
    }

    private String buildQuery(SortedMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) continue;
            if (sb.length() > 0) sb.append('&');
            sb.append(e.getKey()).append('=');
            sb.append(URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII));
        }
        return sb.toString();
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Khong the tao chu ky VNPay", e);
        }
    }
}