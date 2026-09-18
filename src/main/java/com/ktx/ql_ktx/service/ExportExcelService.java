package com.ktx.ql_ktx.service;

import com.ktx.ql_ktx.entity.HoaDon;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Xuat danh sach hoa don / cong no ra file Excel (.xlsx) de Ban quan ly tai ve.
 */
@Service
public class ExportExcelService {

    public byte[] xuatDanhSachHoaDon(List<HoaDon> danhSach) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Hoa don - Cong no");

            String[] tieuDe = {"Toa nha", "Phong", "Thang", "Nam", "Tien phong", "Tien dien", "Tien nuoc", "Tong tien", "Han thanh toan", "Trang thai"};
            Row header = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);

            for (int i = 0; i < tieuDe.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(tieuDe[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (HoaDon hd : danhSach) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(hd.getPhong().getToaNha().getTenToaNha());
                row.createCell(1).setCellValue(hd.getPhong().getSoPhong());
                row.createCell(2).setCellValue(hd.getThang());
                row.createCell(3).setCellValue(hd.getNam());
                row.createCell(4).setCellValue(hd.getTienPhong().doubleValue());
                row.createCell(5).setCellValue(hd.getTienDien().doubleValue());
                row.createCell(6).setCellValue(hd.getTienNuoc().doubleValue());
                row.createCell(7).setCellValue(hd.getTongTien().doubleValue());
                row.createCell(8).setCellValue(hd.getHanThanhToan().toString());
                row.createCell(9).setCellValue(hd.getTrangThai().name());
            }

            for (int i = 0; i < tieuDe.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}