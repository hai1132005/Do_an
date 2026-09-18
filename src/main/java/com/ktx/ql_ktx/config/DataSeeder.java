package com.ktx.ql_ktx.config;

import com.ktx.ql_ktx.entity.BacGiaDien;
import com.ktx.ql_ktx.entity.NguoiDung;
import com.ktx.ql_ktx.entity.VaiTro;
import com.ktx.ql_ktx.repository.BacGiaDienRepository;
import com.ktx.ql_ktx.repository.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Khoi tao du lieu mac dinh khi ung dung chay lan dau:
 * - Tai khoan Ban quan ly mac dinh: admin / admin123
 * - Bang bac gia dien luy tien mau
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private BacGiaDienRepository bacGiaDienRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!nguoiDungRepository.existsByTenDangNhap("admin")) {
            NguoiDung admin = new NguoiDung();
            admin.setTenDangNhap("admin");
            admin.setMatKhau(passwordEncoder.encode("admin123"));
            admin.setHoTen("Ban Quan Ly KTX");
            admin.setVaiTro(VaiTro.BAN_QUAN_LY);
            nguoiDungRepository.save(admin);
            System.out.println(">> Da tao tai khoan Ban quan ly mac dinh: admin / admin123");
        }

        if (bacGiaDienRepository.count() == 0) {
            luuBacGia(1, 0, 50.0, "1800");
            luuBacGia(2, 50, 100.0, "2000");
            luuBacGia(3, 100, null, "2500");
            System.out.println(">> Da tao bang bac gia dien mac dinh");
        }
    }

    private void luuBacGia(int bac, double tu, Double den, String gia) {
        BacGiaDien b = new BacGiaDien();
        b.setBacThu(bac);
        b.setTuSoKw(tu);
        b.setDenSoKw(den);
        b.setDonGia(new BigDecimal(gia));
        bacGiaDienRepository.save(b);
    }
}