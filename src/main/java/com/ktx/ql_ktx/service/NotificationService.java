package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.entity.Giuong;
import com.ktx.ql_ktx.entity.HoaDonCaNhan;
import com.ktx.ql_ktx.entity.NguoiDung;
import com.ktx.ql_ktx.entity.Phong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
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
                "Chao %s,%n%nDang ky o cua ban da duoc duyet.%nPhong: %s%nGiuong: %s%n%nTran trong, Ban quan ly KTX.",
                tenHienThi(sinhVien), phong.getSoPhong(), giuong.getKyHieu());
        gui(sinhVien, "[KTX] Dang ky phong da duoc duyet", noiDung);
    }

    public void guiEmailHoaDonMoi(HoaDonCaNhan hd) {
        NguoiDung sv = hd.getSinhVien();
        String noiDung = String.format(
                "Chao %s,%n%nBan co hoa don moi:%n- So ngay o: %d%n- Tien phong: %s%n- Tien dien: %s%n- Tien nuoc: %s%n- Tong tien: %s%n- Han thanh toan: %s%n%nVui long dang nhap he thong de thanh toan qua VNPay.%n%nTran trong, Ban quan ly KTX.",
                tenHienThi(sv), hd.getSoNgayO(), hd.getTienPhong(), hd.getTienDien(),
                hd.getTienNuoc(), hd.getTongTien(), hd.getHanThanhToan());
        gui(sv, "[KTX] Hoa don moi can thanh toan", noiDung);
    }

    public void guiEmailThanhToanThanhCong(HoaDonCaNhan hd) {
        NguoiDung sv = hd.getSinhVien();
        String noiDung = String.format(
                "Chao %s,%n%nHe thong da ghi nhan thanh toan thanh cong hoa don #%d, so tien %s qua VNPay (ma giao dich: %s).%n%nTran trong, Ban quan ly KTX.",
                tenHienThi(sv), hd.getId(), hd.getTongTien(), hd.getMaGiaoDichVnPay());
        gui(sv, "[KTX] Thanh toan thanh cong", noiDung);
    }

    private String tenHienThi(NguoiDung nd) {
        return nd.getHoTen() != null ? nd.getHoTen() : nd.getTenDangNhap();
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
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(tuDiaChi);
            msg.setTo(nguoiNhan.getEmail());
            msg.setSubject(tieuDe);
            msg.setText(noiDung);
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Gui email that bai toi {}: {}", nguoiNhan.getEmail(), e.getMessage());
        }
    }
}
