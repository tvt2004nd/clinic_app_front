package com.dermacare.clinic.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.TabAlignment;
import com.itextpdf.layout.element.TabStop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class PdfGenerator {

    public static void exportMedicalRecordPdf(Context context, Map<String, String> data) {
        try {
            String fileName = "BenhAn_" + data.getOrDefault("recordCode", String.valueOf(System.currentTimeMillis())) + ".pdf";
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            // Cấu hình Font tiếng Việt (Cần ttf trong assets/fonts/)
            PdfFont font = null;
            try {
                // Thử tải font từ assets nếu có
                byte[] fontBytes = readAssetFile(context, "fonts/arial.ttf");
                if (fontBytes != null) {
                    font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    document.setFont(font);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // --- TRÊN CÙNG (SỞ Y TẾ / BỆNH VIỆN) ---
            Table topTable = new Table(UnitValue.createPointArray(new float[]{200, 200, 150}));
            topTable.setWidth(UnitValue.createPercentValue(100));
            
            topTable.addCell(new Cell().add(new Paragraph("Sở Y tế: TP. Hồ Chí Minh\nPhòng khám: DermaCare Clinic").setFontSize(10)).setBorder(null));
            topTable.addCell(new Cell().add(new Paragraph("BỆNH ÁN NGOẠI TRÚ\nCHUYÊN KHOA DA LIỄU")
                    .setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14)).setBorder(null));
            topTable.addCell(new Cell().add(new Paragraph("Mã BN: " + data.getOrDefault("recordCode", "N/A") + "\nSố hồ sơ: " + (System.currentTimeMillis() / 100000))
                    .setFontSize(9).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
            document.add(topTable);

            // --- I. HÀNH CHÍNH ---
            document.add(new Paragraph("\nI. HÀNH CHÍNH:").setBold().setFontSize(12));
            
            Table adminTable = new Table(UnitValue.createPointArray(new float[]{280, 140, 100}));
            adminTable.setWidth(UnitValue.createPercentValue(100));
            adminTable.addCell(new Cell().add(new Paragraph("1. Họ và tên: " + data.getOrDefault("hoTen", "").toUpperCase())).setBorder(null));
            adminTable.addCell(new Cell().add(new Paragraph("2. Ngày sinh: " + data.getOrDefault("ngaySinh", ""))).setBorder(null));
            adminTable.addCell(new Cell().add(new Paragraph("3. Giới tính: " + data.getOrDefault("gioiTinh", ""))).setBorder(null));
            document.add(adminTable);

            document.add(new Paragraph("4. Địa chỉ: " + data.getOrDefault("diaChi", "........................................................................")).setFontSize(10));
            document.add(new Paragraph("5. Số thẻ BHYT: " + data.getOrDefault("bhyt", "....................................................")).setFontSize(10));

            // --- II. HỎI BỆNH & TIỀN SỬ ---
            document.add(new Paragraph("\nII. TIỀN SỬ & LÝ DO KHÁM:").setBold().setFontSize(12));
            document.add(new Paragraph("+ Lý do khám: " + data.getOrDefault("lyDo", "Theo dõi bệnh lý da liễu")).setFontSize(10));
            document.add(new Paragraph("+ Tiền sử dị ứng: " + data.getOrDefault("diUng", "Không")).setFontSize(10));
            document.add(new Paragraph("+ Tiền sử gia đình: " + data.getOrDefault("tienSuGiaDinh", "Bình thường")).setFontSize(10));

            // --- III. KHÁM LÂM SÀNG ---
            document.add(new Paragraph("\nIII. KHÁM LÂM SÀNG:").setBold().setFontSize(12));
            
            Table medicalTable = new Table(UnitValue.createPointArray(new float[]{350, 150}));
            medicalTable.setWidth(UnitValue.createPercentValue(100));
            medicalTable.addCell(new Cell().add(new Paragraph("1. Mô tả tổn thương da:\n- Vị trí: .....................................................\n- Tính chất: ...............................................")).setBorder(null));
            
            Table vitalTable = new Table(UnitValue.createPointArray(new float[]{80, 40}));
            vitalTable.addCell(new Cell().add(new Paragraph("Mạch:").setFontSize(9)));
            vitalTable.addCell(new Cell().add(new Paragraph("....... l/p").setFontSize(9)));
            vitalTable.addCell(new Cell().add(new Paragraph("Huyết áp:").setFontSize(9)));
            vitalTable.addCell(new Cell().add(new Paragraph("....... mmHg").setFontSize(9)));
            medicalTable.addCell(new Cell().add(vitalTable));
            document.add(medicalTable);

            // --- IV. CHẨN ĐOÁN & ĐIỀU TRỊ ---
            document.add(new Paragraph("\nIV. CHẨN ĐOÁN & ĐIỀU TRỊ:").setBold().setFontSize(12));
            document.add(new Paragraph("1. Chẩn đoán: " + data.getOrDefault("chanDoan", "") + " (" + data.getOrDefault("disease", "") + ")").setFontSize(10));
            document.add(new Paragraph("2. Phác đồ điều trị / Thuốc: \n" + data.getOrDefault("dieuTri", "........................................................................................................................................")).setFontSize(10));
            document.add(new Paragraph("3. Hẹn tái khám: " + data.getOrDefault("followUp", "Theo chỉ định")).setFontSize(10).setItalic());

            // --- KÝ TÊN ---
            String currentDate = new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());
            String currentMonth = new SimpleDateFormat("MM", Locale.getDefault()).format(new Date());
            String currentYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());

            document.add(new Paragraph("\nNgày " + currentDate + " tháng " + currentMonth + " năm " + currentYear)
                    .setTextAlignment(TextAlignment.RIGHT).setItalic().setFontSize(10));
            
            Table footerTable = new Table(UnitValue.createPointArray(new float[]{250, 250}));
            footerTable.setWidth(UnitValue.createPercentValue(100));
            footerTable.addCell(new Cell().add(new Paragraph("Xác nhận bệnh nhân\n(Ký và ghi rõ họ tên)")
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(10)).setBorder(null));
            footerTable.addCell(new Cell().add(new Paragraph("Bác sỹ điều trị\n\n\n" + data.getOrDefault("tenBacSi", "........................."))
                    .setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(11)).setBorder(null));
            
            document.add(footerTable);

            document.close();
            Toast.makeText(context, "Đã lưu PDF tại thư mục Documents", Toast.LENGTH_SHORT).show();
            
            openPdf(context, file);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static byte[] readAssetFile(Context context, String fileName) {
        try (InputStream is = context.getAssets().open(fileName);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            return os.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private static void openPdf(Context context, File file) {
        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Không có ứng dụng xem PDF. Hãy tải PDF Viewer.", Toast.LENGTH_LONG).show();
        }
    }
}
