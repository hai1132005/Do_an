package com.ktx.ql_ktx.controller;

import com.ktx.ql_ktx.service.HoaDonCaNhanService;
import com.ktx.ql_ktx.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * BO SUNG MOI (diem 3/4 nhan xet cua thay): endpoint ma VNPay sandbox se
 * REDIRECT trinh duyet cua sinh vien ve sau khi thanh toan xong. Trinh
 * duyet goi thang URL nay (khong kem JWT), nen phai la endpoint CONG
 * KHAI (xem SecurityConfig: /api/thanh-toan/** permitAll) - viec xac
 * thuc an toan nam o chu ky HMAC-SHA512 (vnp_SecureHash) do VnPayService
 * kiem tra, khong phai o JWT.
 *
 * Ket qua duoc tra ve thanh 1 TRANG HTML ngan (thay vi JSON tho) kem nut
 * "Quay ve trang sinh vien". Phien dang nhap nam trong sessionStorage cua
 * CHINH tab nay nen khi quay lai sinh-vien.html sinh vien van dang nhap.
 */
@RestController
@RequestMapping("/api/thanh-toan")
public class PaymentController {

    private static final String TRANG_SINH_VIEN = "/sinh-vien.html#sv-hoa-don-ca-nhan";

    @Autowired private VnPayService vnPayService;
    @Autowired private HoaDonCaNhanService hoaDonCaNhanService;

    @GetMapping("/vnpay-callback")
    public ResponseEntity<String> vnPayCallback(HttpServletRequest request) {
        Map<String, String> thamSo = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> thamSo.put(k, v.length > 0 ? v[0] : ""));

        if (!vnPayService.xacThucChuKy(thamSo)) {
            return trang(HttpStatus.BAD_REQUEST, false, "Chữ ký không hợp lệ",
                    "Kết quả trả về từ VNPay không xác thực được nên hệ thống không ghi nhận giao dịch này.", null);
        }

        String maPhanHoi = thamSo.get("vnp_ResponseCode");
        String txnRef = thamSo.get("vnp_TxnRef"); // dang "HDCN{id}_{timestamp}"

        if (!"00".equals(maPhanHoi) || txnRef == null || !txnRef.startsWith("HDCN")) {
            return trang(HttpStatus.OK, false, "Thanh toán không thành công",
                    "Giao dịch đã bị hủy hoặc không hoàn tất. Hóa đơn của bạn vẫn chưa được thanh toán, bạn có thể thử lại.", null);
        }

        try {
            String phanId = txnRef.substring(4, txnRef.indexOf('_'));
            Long hoaDonCaNhanId = Long.parseLong(phanId);
            String maGiaoDich = thamSo.get("vnp_TransactionNo");
            hoaDonCaNhanService.xacNhanDaThanhToan(hoaDonCaNhanId, maGiaoDich);

            String soTien = "";
            try {
                BigDecimal vnd = new BigDecimal(thamSo.get("vnp_Amount")).divide(BigDecimal.valueOf(100));
                soTien = String.format(java.util.Locale.ROOT, "%,.0f đ", vnd).replace(',', '.');
            } catch (Exception ignored) { /* khong hien so tien neu khong doc duoc */ }

            String chiTiet = "Hệ thống đã ghi nhận thanh toán và cập nhật công nợ của bạn."
                    + (soTien.isEmpty() ? "" : " Số tiền: " + soTien + ".");
            return trang(HttpStatus.OK, true, "Thanh toán thành công", chiTiet, maGiaoDich);
        } catch (Exception e) {
            return trang(HttpStatus.BAD_REQUEST, false, "Không xác định được hóa đơn",
                    "Không tìm thấy hóa đơn cần thanh toán. Vui lòng liên hệ ban quản lý.", null);
        }
    }

    private ResponseEntity<String> trang(HttpStatus status, boolean thanhCong, String tieuDe,
                                         String chiTiet, String maGiaoDich) {
        String mau = thanhCong ? "#4C7A56" : "#9C4230";
        String bieuTuong = thanhCong ? "&#10003;" : "&#33;";
        String giaoDich = (maGiaoDich == null || maGiaoDich.isBlank()) ? ""
                : "<p class=\"ma\">Mã giao dịch VNPay: <strong>" + HtmlUtils.htmlEscape(maGiaoDich) + "</strong></p>";

        String html = "<!DOCTYPE html><html lang=\"vi\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>" + HtmlUtils.htmlEscape(tieuDe) + " — KTX Portal</title>"
                + "<style>"
                + "body{margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;"
                + "background:#EFECE3;font-family:'IBM Plex Sans',Arial,sans-serif;color:#222}"
                + ".card{background:#fff;border:1px solid #DAD5C6;border-radius:8px;padding:40px 36px;"
                + "max-width:440px;width:calc(100% - 32px);text-align:center}"
                + ".icon{width:64px;height:64px;border-radius:50%;margin:0 auto 18px;color:#fff;font-size:34px;"
                + "line-height:64px;background:" + mau + "}"
                + "h1{margin:0 0 10px;font-size:22px;color:" + mau + "}"
                + "p{margin:8px 0;color:#5B6357;line-height:1.5}"
                + ".ma{font-family:'IBM Plex Mono',monospace;font-size:14px}"
                + ".btn{display:inline-block;margin-top:22px;padding:11px 22px;background:#0F5734;color:#fff;"
                + "text-decoration:none;border-radius:6px;font-weight:600}"
                + ".btn:hover{background:#3D6350}"
                + "</style></head><body><div class=\"card\">"
                + "<div class=\"icon\">" + bieuTuong + "</div>"
                + "<h1>" + HtmlUtils.htmlEscape(tieuDe) + "</h1>"
                + "<p>" + HtmlUtils.htmlEscape(chiTiet) + "</p>"
                + giaoDich
                + "<a class=\"btn\" href=\"" + TRANG_SINH_VIEN + "\">Quay về trang sinh viên</a>"
                + "</div></body></html>";

        return ResponseEntity.status(status)
                .contentType(new MediaType("text", "html", java.nio.charset.StandardCharsets.UTF_8))
                .body(html);
    }
}