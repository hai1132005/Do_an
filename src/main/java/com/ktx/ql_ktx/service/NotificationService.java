package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.entity.Giuong;
import com.ktx.ql_ktx.entity.HoaDonCaNhan;
import com.ktx.ql_ktx.entity.NguoiDung;
import com.ktx.ql_ktx.entity.Phong;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * BO SUNG MOI theo yeu cau cua thay (diem 4): gui email thong bao cho
 * sinh vien o cac moc quan trong (duyet dang ky, co hoa don ca nhan
 * moi, thanh toan thanh cong). Dung JavaMailSender chuan cua Spring Boot
 * (them dependency spring-boot-starter-mail trong pom.xml).
 *
 * De khong lam sap luong nghiep vu chinh khi chua cau hinh SMTP that
 * (vi du luc chay demo/local), mac dinh ktx.mail.bat-kich-hoat=false -
 * khi do email chi duoc ghi ra log (xem application.properties). Khi
 * trien khai that, bat co che nay len va khai bao SMTP that.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${ktx.mail.from:no-reply@ktx.local}")
    private String tuDiaChi;

    @Value("${ktx.mail.bat-kich-hoat:false}")
    private boolean batKichHoat;

    public void guiEmailDuyetDangKy(NguoiDung sinhVien, Phong phong, Giuong giuong) {
        String noiDung = String.format(
                "Chào %s,%n%nĐăng ký ở của bạn đã được duyệt.%nPhòng: %s%nGiường: %s%n%nTrân trọng,%nBan quản lý KTX.",
                tenHienThi(sinhVien), phong.getSoPhong(), giuong.getKyHieu());
        gui(sinhVien, "[KTX] Đăng ký phòng đã được duyệt", noiDung);
    }

    public void guiEmailHoaDonMoi(HoaDonCaNhan hd) {
        NguoiDung sv = hd.getSinhVien();
        String noiDung = String.format(
                "Chào %s,%n%nBạn có hóa đơn mới:%n- Số ngày ở: %d%n- Tiền phòng: %s%n- Tiền điện: %s%n- Tiền nước: %s%n- Tổng tiền: %s%n- Hạn thanh toán: %s%n%nVui lòng đăng nhập hệ thống để thanh toán qua VNPay.%n%nTrân trọng,%nBan quản lý KTX.",
                tenHienThi(sv), hd.getSoNgayO(), tien(hd.getTienPhong()), tien(hd.getTienDien()),
                tien(hd.getTienNuoc()), tien(hd.getTongTien()), ngay(hd.getHanThanhToan()));
        gui(sv, "[KTX] Hóa đơn mới cần thanh toán", noiDung);
    }

    public void guiEmailThanhToanThanhCong(HoaDonCaNhan hd) {
        NguoiDung sv = hd.getSinhVien();
        String hinhThuc = hd.getMaGiaoDichVnPay() != null && !hd.getMaGiaoDichVnPay().isBlank()
                ? "qua VNPay (mã giao dịch: " + hd.getMaGiaoDichVnPay() + ")"
                : "bằng tiền mặt";
        String noiDung = String.format(
                "Chào %s,%n%nHệ thống đã ghi nhận thanh toán thành công hóa đơn #%d, số tiền %s, %s.%n%nTrân trọng,%nBan quản lý KTX.",
                tenHienThi(sv), hd.getId(), tien(hd.getTongTien()), hinhThuc);
        gui(sv, "[KTX] Thanh toán thành công", noiDung);
    }

    private String tenHienThi(NguoiDung nd) {
        return nd.getHoTen() != null ? nd.getHoTen() : nd.getTenDangNhap();
    }

    private String tien(BigDecimal v) {
        if (v == null) return "0 đ";
        return String.format(Locale.ROOT, "%,.0f", v).replace(',', '.') + " đ";
    }

    private String ngay(LocalDate d) {
        return d == null ? "—" : d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private void gui(NguoiDung nguoiNhan, String tieuDe, String noiDung) {
        if (!batKichHoat || mailSender == null) {
            log.info("[MAIL-TAT] Toi={} | {} | {}", nguoiNhan.getEmail(), tieuDe, noiDung);
            return;
        }
        if (nguoiNhan.getEmail() == null || nguoiNhan.getEmail().isBlank()) {
            log.warn("Khong gui duoc email: nguoi dung id={} chua co email", nguoiNhan.getId());
            return;
        }
        final String den = nguoiNhan.getEmail();
        // Gui o luong rieng, va chi sau khi giao dich DB da commit: thao tac duyet/tao hoa don
        // khong bi cham boi SMTP, va khong gui thu neu giao dich bi huy.
        Runnable guiThu = () -> NOI_GUI.submit(() -> guiThuThat(den, tieuDe, noiDung));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { guiThu.run(); }
            });
        } else {
            guiThu.run();
        }
    }

    private static final ExecutorService NOI_GUI = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ktx-mail");
        t.setDaemon(true);
        return t;
    });

    private void guiThuThat(String den, String tieuDe, String noiDung) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
            helper.setFrom(tuDiaChi);
            helper.setTo(den);
            helper.setSubject(tieuDe);
            helper.setText(noiDung, false);
            mailSender.send(mime);
        } catch (Exception e) {
            log.warn("Gui email that bai toi {}: {}", den, e.getMessage());
        }
    }
}