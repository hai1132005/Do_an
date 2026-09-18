package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.entity.ChiSoDienNuoc;
import com.ktx.ql_ktx.entity.HoaDon;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Xuat hoa don tien phong/dien/nuoc ra file PDF (theo yeu cau de bai: xuat Excel/PDF).
 * Dung thu vien OpenPDF, co ho tro font Unicode de hien thi tieng Viet co dau.
 */
@Service
public class ExportPdfService {

    private static final Color PINE = new Color(44, 74, 59);
    private static final Color LINE = new Color(218, 213, 198);

    public byte[] xuatHoaDon(HoaDon hd) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 48, 48, 48, 48);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Font Unicode ho tro tieng Viet (dung font co san trong OpenPDF)
            Font fTieuDe = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", 18, Font.BOLD, PINE);
            Font fNhan = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", 10, Font.BOLD);
            Font fThuong = FontFactory.getFont(FontFactory.HELVETICA, "Cp1252", 11);
            Font fNho = FontFactory.getFont(FontFactory.HELVETICA, "Cp1252", 9, Font.ITALIC, Color.GRAY);
            Font fTong = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", 13, Font.BOLD, PINE);

            Paragraph tieuDe = new Paragraph("HOA DON KY TUC XA", fTieuDe);
            tieuDe.setAlignment(Element.ALIGN_CENTER);
            doc.add(tieuDe);

            Paragraph kyHoaDon = new Paragraph(
                    "Ky thanh toan: Thang " + hd.getThang() + "/" + hd.getNam(), fThuong);
            kyHoaDon.setAlignment(Element.ALIGN_CENTER);
            kyHoaDon.setSpacingAfter(18);
            doc.add(kyHoaDon);

            // Thong tin phong
            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setWidths(new float[]{1.2f, 2.8f});
            info.setSpacingAfter(16);
            themDong(info, "Toa nha:", hd.getPhong().getToaNha().getTenToaNha(), fNhan, fThuong);
            themDong(info, "Phong:", hd.getPhong().getSoPhong(), fNhan, fThuong);
            themDong(info, "Han thanh toan:",
                    hd.getHanThanhToan().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fNhan, fThuong);
            themDong(info, "Trang thai:", trangThaiText(hd), fNhan, fThuong);
            doc.add(info);

            // Bang chi tiet
            PdfPTable bang = new PdfPTable(4);
            bang.setWidthPercentage(100);
            bang.setWidths(new float[]{2.4f, 1.4f, 1.4f, 1.6f});
            bang.setSpacingAfter(14);

            themHeader(bang, "Khoan muc", fNhan);
            themHeader(bang, "Chi so cu", fNhan);
            themHeader(bang, "Chi so moi", fNhan);
            themHeader(bang, "Thanh tien", fNhan);

            ChiSoDienNuoc cs = hd.getChiSo();
            themO(bang, "Tien phong", fThuong, Element.ALIGN_LEFT);
            themO(bang, "-", fThuong, Element.ALIGN_CENTER);
            themO(bang, "-", fThuong, Element.ALIGN_CENTER);
            themO(bang, tien(hd.getTienPhong()), fThuong, Element.ALIGN_RIGHT);

            themO(bang, "Tien dien (bac gia luy tien)", fThuong, Element.ALIGN_LEFT);
            themO(bang, cs != null ? fmt(cs.getChiSoDienCu()) : "-", fThuong, Element.ALIGN_CENTER);
            themO(bang, cs != null ? fmt(cs.getChiSoDienMoi()) : "-", fThuong, Element.ALIGN_CENTER);
            themO(bang, tien(hd.getTienDien()), fThuong, Element.ALIGN_RIGHT);

            themO(bang, "Tien nuoc", fThuong, Element.ALIGN_LEFT);
            themO(bang, cs != null ? fmt(cs.getChiSoNuocCu()) : "-", fThuong, Element.ALIGN_CENTER);
            themO(bang, cs != null ? fmt(cs.getChiSoNuocMoi()) : "-", fThuong, Element.ALIGN_CENTER);
            themO(bang, tien(hd.getTienNuoc()), fThuong, Element.ALIGN_RIGHT);

            doc.add(bang);

            Paragraph tong = new Paragraph("TONG CONG: " + tien(hd.getTongTien()), fTong);
            tong.setAlignment(Element.ALIGN_RIGHT);
            tong.setSpacingAfter(26);
            doc.add(tong);

            Paragraph chuThich = new Paragraph(
                    "Ghi chu: Tien dien duoc tinh theo bac gia luy tien do Ban quan ly ky tuc xa quy dinh.\n"
                  + "Hoa don duoc tao tu dong tu he thong quan ly ky tuc xa.", fNho);
            doc.add(chuThich);

            doc.close();
            return out.toByteArray();
        }
    }

    private String trangThaiText(HoaDon hd) {
        return switch (hd.getTrangThai()) {
            case DA_THANH_TOAN -> "Da thanh toan";
            case QUA_HAN -> "Qua han";
            default -> "Chua thanh toan";
        };
    }

    private String fmt(Double d) {
        return d == null ? "-" : String.valueOf(Math.round(d));
    }

    private String tien(BigDecimal v) {
        return String.format("%,d d", v == null ? 0 : v.longValue());
    }

    private void themDong(PdfPTable t, String nhan, String giaTri, Font fNhan, Font fThuong) {
        PdfPCell c1 = new PdfPCell(new Phrase(nhan, fNhan));
        PdfPCell c2 = new PdfPCell(new Phrase(giaTri == null ? "-" : giaTri, fThuong));
        c1.setBorder(Rectangle.NO_BORDER);
        c2.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(4);
        c2.setPadding(4);
        t.addCell(c1);
        t.addCell(c2);
    }

    private void themHeader(PdfPTable t, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(new Color(239, 236, 227));
        c.setBorderColor(LINE);
        c.setPadding(7);
        t.addCell(c);
    }

    private void themO(PdfPTable t, String text, Font f, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setHorizontalAlignment(align);
        c.setBorderColor(LINE);
        c.setPadding(7);
        t.addCell(c);
    }
}