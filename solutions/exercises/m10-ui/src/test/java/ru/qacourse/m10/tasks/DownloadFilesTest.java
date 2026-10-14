package ru.qacourse.m10.tasks;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.qacourse.support.ShopConfig;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Задание 10.12: скачивание файлов в Selenide и чтение PDF / XLSX.
 * Страница /playground/download.
 * 1) sample.txt ([data-testid=download-txt]) — содержит «Этот файл скачан автотестом».
 * 2) menu.pdf ([data-testid=download-pdf]) — текст содержит «ИТОГО: 708.80».
 * 3) orders.xlsx ([data-testid=download-xlsx]) — в первом листе заголовок A1 = «id», в строке 2 total (колонка F) = 708.8.
 */
public class DownloadFilesTest {
    @BeforeEach
    void setUp() {
        Configuration.baseUrl = ShopConfig.baseUrl();
        open("/playground/download");
    }

    @AfterEach
    void tearDown() {
        closeWebDriver();
    }

    @Test
    void text() throws Exception {
        File file = $("[data-testid=download-txt]").download();
        assertThat(Files.readString(file.toPath(), StandardCharsets.UTF_8)).contains("Этот файл скачан автотестом");
    }

    @Test
    void pdf() throws Exception {
        File file = $("[data-testid=download-pdf]").download();
        try (PDDocument doc = Loader.loadPDF(file)) {
            assertThat(new PDFTextStripper().getText(doc)).contains("ИТОГО: 708.80");
        }
    }

    @Test
    void xlsx() throws Exception {
        File file = $("[data-testid=download-xlsx]").download();
        try (XSSFWorkbook wb = new XSSFWorkbook(file)) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("id");
            assertThat(sheet.getRow(1).getCell(5).getNumericCellValue()).isEqualTo(708.8);
        }
    }
}
