package ru.qacourse.shop.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.qacourse.shop.order.DocumentService;
import ru.qacourse.shop.order.Order;
import ru.qacourse.shop.order.OrderItem;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * «Песочница» для UI-автотестов: формы, алерты, iframe, drag&drop, файлы, ожидания, Basic Auth.
 * Все страницы: /playground
 */
@Controller
@RequestMapping("/playground")
public class PlaygroundController {
    private final DocumentService documents;

    public PlaygroundController(DocumentService documents) {
        this.documents = documents;
    }

    @GetMapping
    public String index() {
        return "playground/index";
    }

    @GetMapping("/{page:forms|alerts|iframe|iframe-inner|drag-drop|dynamic|table|hover|windows|scroll|modal|upload}")
    public String page(@PathVariable String page) {
        return "playground/" + page;
    }

    @PostMapping("/forms")
    public String submitForm(@RequestParam java.util.Map<String, String> params, Model model) {
        model.addAttribute("submitted", params);
        return "playground/forms";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        model.addAttribute("fileName", file.getOriginalFilename());
        model.addAttribute("fileSize", file.getSize());
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        model.addAttribute("preview", text.length() > 200 ? text.substring(0, 200) : text);
        return "playground/upload";
    }

    @GetMapping("/download")
    public String downloadPage() {
        return "playground/download";
    }

    @GetMapping("/files/sample.txt")
    public ResponseEntity<byte[]> sampleTxt() {
        return file("sample.txt", MediaType.TEXT_PLAIN, "Привет из QA Burger!\nЭтот файл скачан автотестом.\n".getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/files/menu.pdf")
    public ResponseEntity<byte[]> samplePdf() {
        return file("menu.pdf", MediaType.APPLICATION_PDF, documents.receiptPdf(sampleOrder()));
    }

    @GetMapping("/files/orders.xlsx")
    public ResponseEntity<byte[]> sampleXlsx() {
        return file("orders.xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                documents.ordersXlsx(List.of(sampleOrder())));
    }

    /** Страница за HTTP Basic Auth: логин admin, пароль admin. */
    @GetMapping("/basic-auth")
    public String basicAuth(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String header,
                            HttpServletResponse res) {
        String expected = "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));
        if (!expected.equals(header)) {
            res.setStatus(401);
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"QA Burger Playground\"");
            return "playground/unauthorized";
        }
        return "playground/basic-auth";
    }

    private static Order sampleOrder() {
        List<OrderItem> items = List.of(
                new OrderItem(1L, "Классический бургер", new BigDecimal("289.90"), 2),
                new OrderItem(7L, "Картофель фри", new BigDecimal("129.00"), 1));
        BigDecimal subtotal = new BigDecimal("708.80");
        return new Order(0L, items, subtotal, BigDecimal.ZERO.setScale(2), subtotal, null) {
            @Override
            public Long getId() {
                return 0L;
            }

            @Override
            public ru.qacourse.shop.order.OrderStatus getStatus() {
                return ru.qacourse.shop.order.OrderStatus.NEW;
            }
        };
    }

    private static ResponseEntity<byte[]> file(String name, MediaType type, byte[] data) {
        return ResponseEntity.ok().contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                .body(data);
    }
}
