package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.dto.GhiChiSoRequest;
import com.ktx.ql_ktx.entity.*;
import com.ktx.ql_ktx.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Nghiep vu quan trong nhat: ghi chi so dien nuoc hang thang va tinh hoa don.
 * Tien dien tinh theo bac gia luy tien (BacGiaDien).
 * Gia nuoc tinh dong gia co dinh (co the mo rong thanh bac gia tuong tu neu can).
 */
@Service
public class ChiSoService {

    @Autowired private ChiSoDienNuocRepository chiSoRepository;
    @Autowired private PhongRepository phongRepository;
    @Autowired private BacGiaDienRepository bacGiaDienRepository;
    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private NguoiDungRepository nguoiDungRepository;

    // Gia nuoc co dinh: dong/m3 - co the doi thanh cau hinh trong DB neu can
    private static final BigDecimal GIA_NUOC_M3 = new BigDecimal("15000");

    /**
     * Ghi chi so dien nuoc thang moi. Chi so cu duoc lay tu chi so moi cua thang truoc do
     * (neu la lan dau ghi cho phong thi chi so cu = 0).
     */
    @Transactional
    public ChiSoDienNuoc ghiChiSo(GhiChiSoRequest req, Long nguoiGhiId) {
        Phong phong = phongRepository.findById(req.getPhongId())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong"));

        if (chiSoRepository.findByPhongIdAndThangAndNam(phong.getId(), req.getThang(), req.getNam()).isPresent()) {
            throw new IllegalStateException("Da ghi chi so cho phong nay trong thang/nam da chon");
        }

        double dienCu = 0, nuocCu = 0;
        var chiSoTruoc = chiSoRepository.findTopByPhongIdOrderByNamDescThangDesc(phong.getId());
        if (chiSoTruoc.isPresent()) {
            dienCu = chiSoTruoc.get().getChiSoDienMoi();
            nuocCu = chiSoTruoc.get().getChiSoNuocMoi();
        }

        if (req.getChiSoDienMoi() < dienCu || req.getChiSoNuocMoi() < nuocCu) {
            throw new IllegalArgumentException("Chi so moi khong duoc nho hon chi so cu");
        }

        NguoiDung nguoiGhi = nguoiDungRepository.findById(nguoiGhiId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi ghi"));

        ChiSoDienNuoc cs = new ChiSoDienNuoc();
        cs.setPhong(phong);
        cs.setThang(req.getThang());
        cs.setNam(req.getNam());
        cs.setChiSoDienCu(dienCu);
        cs.setChiSoDienMoi(req.getChiSoDienMoi());
        cs.setChiSoNuocCu(nuocCu);
        cs.setChiSoNuocMoi(req.getChiSoNuocMoi());
        cs.setNguoiGhi(nguoiGhi);
        cs = chiSoRepository.save(cs);

        // Sau khi ghi chi so, tu dong tao hoa don cho phong
        taoHoaDon(cs);

        return cs;
    }

    /** Tinh tien dien theo bac gia luy tien */
    public BigDecimal tinhTienDien(double soKwTieuThu) {
        List<BacGiaDien> bacGia = bacGiaDienRepository.findAllByOrderByBacThuAsc();
        if (bacGia.isEmpty()) {
            // Gia mac dinh neu chua cau hinh bac gia: 2000d/kWh
            return BigDecimal.valueOf(soKwTieuThu).multiply(new BigDecimal("2000")).setScale(0, RoundingMode.HALF_UP);
        }

        BigDecimal tongTien = BigDecimal.ZERO;
        double conLai = soKwTieuThu;

        for (BacGiaDien bac : bacGia) {
            if (conLai <= 0) break;
            double gioiHanBac = (bac.getDenSoKw() == null)
                    ? conLai
                    : Math.min(conLai, bac.getDenSoKw() - bac.getTuSoKw());
            double soKwTinhBac = Math.min(conLai, gioiHanBac);
            tongTien = tongTien.add(BigDecimal.valueOf(soKwTinhBac).multiply(bac.getDonGia()));
            conLai -= soKwTinhBac;
        }
        return tongTien.setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal tinhTienNuoc(double soM3TieuThu) {
        return BigDecimal.valueOf(soM3TieuThu).multiply(GIA_NUOC_M3).setScale(0, RoundingMode.HALF_UP);
    }

    /** Tao hoa don tu chi so da ghi: tien phong (nguyen gia phong, khong chia dau nguoi theo mac dinh) + dien + nuoc */
    @Transactional
    public HoaDon taoHoaDon(ChiSoDienNuoc cs) {
        Phong phong = cs.getPhong();

        BigDecimal tienDien = tinhTienDien(cs.getSoDienTieuThu());
        BigDecimal tienNuoc = tinhTienNuoc(cs.getSoNuocTieuThu());
        BigDecimal tienPhong = phong.getGiaPhongThang();
        BigDecimal tongTien = tienPhong.add(tienDien).add(tienNuoc);

        HoaDon hd = new HoaDon();
        hd.setPhong(phong);
        hd.setChiSo(cs);
        hd.setThang(cs.getThang());
        hd.setNam(cs.getNam());
        hd.setTienPhong(tienPhong);
        hd.setTienDien(tienDien);
        hd.setTienNuoc(tienNuoc);
        hd.setTongTien(tongTien);
        hd.setHanThanhToan(LocalDate.of(cs.getNam(), cs.getThang(), 1).plusMonths(1).plusDays(9)); // han ngay 10 thang sau
        hd.setTrangThai(TrangThaiHoaDon.CHUA_THANH_TOAN);

        return hoaDonRepository.save(hd);
    }
}