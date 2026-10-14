package ru.qacourse.shop.order;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.qacourse.shop.bugs.Bug;
import ru.qacourse.shop.bugs.BugRegistry;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

/** Генерация PDF-чека и Excel-выгрузки — для уроков про чтение PDF/XLSX в тестах. */
@Service
public class DocumentService {
    private final BugRegistry bugs;

    public DocumentService(BugRegistry bugs) {
        this.bugs = bugs;
    }

    public byte[] receiptPdf(Order order) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();
            Font font = cyrillicFont(12);
            Font title = cyrillicFont(18);
            doc.add(new Paragraph("QA Burger — чек по заказу №" + order.getId(), title));
            doc.add(new Paragraph("Статус: " + order.getStatus(), font));
            doc.add(new Paragraph(" ", font));
            PdfPTable table = new PdfPTable(4);
            for (String h : List.of("Товар", "Цена", "Кол-во", "Сумма")) table.addCell(new Paragraph(h, font));
            for (OrderItem i : order.getItems()) {
                table.addCell(new Paragraph(i.getName(), font));
                table.addCell(new Paragraph(i.getPrice().toPlainString(), font));
                table.addCell(new Paragraph(String.valueOf(i.getQuantity()), font));
                table.addCell(new Paragraph(i.getPrice().multiply(java.math.BigDecimal.valueOf(i.getQuantity())).toPlainString(), font));
            }
            doc.add(table);
            doc.add(new Paragraph(" ", font));
            if (order.getPromoCode() != null) {
                doc.add(new Paragraph("Промокод: " + order.getPromoCode() + ", скидка: " + order.getDiscount().toPlainString(), font));
            }
            String total = (bugs.isOn(Bug.BUG_801) ? order.getSubtotal() : order.getTotal()).toPlainString();
            doc.add(new Paragraph("ИТОГО: " + total + " RUB", title));
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось сформировать PDF", e);
        }
    }

    public byte[] ordersXlsx(List<Order> orders) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Orders");
            Row header = sheet.createRow(0);
            String[] cols = {"id", "status", "items", "subtotal", "discount", "total", "promo", "createdAt"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            int count = bugs.isOn(Bug.BUG_802) ? Math.max(0, orders.size() - 1) : orders.size();
            for (int r = 0; r < count; r++) {
                Order o = orders.get(r);
                Row row = sheet.createRow(r + 1);
                row.createCell(0).setCellValue(o.getId());
                row.createCell(1).setCellValue(o.getStatus().name());
                row.createCell(2).setCellValue(o.getItems().stream().mapToInt(OrderItem::getQuantity).sum());
                row.createCell(3).setCellValue(o.getSubtotal().doubleValue());
                row.createCell(4).setCellValue(o.getDiscount().doubleValue());
                row.createCell(5).setCellValue(o.getTotal().doubleValue());
                row.createCell(6).setCellValue(o.getPromoCode() == null ? "" : o.getPromoCode());
                row.createCell(7).setCellValue(o.getCreatedAt().toString());
            }
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось сформировать XLSX", e);
        }
    }

    /** Встроенный шрифт PDF не умеет кириллицу — берём DejaVuSans из ресурсов. */
    private Font cyrillicFont(float size) throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf")) {
            byte[] bytes = in.readAllBytes();
            BaseFont bf = BaseFont.createFont("DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
            return new Font(bf, size);
        }
    }
}
