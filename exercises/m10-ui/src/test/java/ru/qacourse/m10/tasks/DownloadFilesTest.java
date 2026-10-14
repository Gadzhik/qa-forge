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

/**
 * Задание 10.12: скачивание файлов в Selenide и чтение PDF / XLSX.
 * Страница /playground/download.
 * 1) sample.txt ([data-testid=download-txt]) — содержит «Этот файл скачан автотестом».
 * 2) menu.pdf ([data-testid=download-pdf]) — текст содержит «ИТОГО: 708.80».
 * 3) orders.xlsx ([data-testid=download-xlsx]) — в первом листе заголовок A1 = «id», в строке 2 total (колонка F) = 708.8.
 */
public class DownloadFilesTest {
    @Test
    void download() {
        fail("TODO: напишите тест");
    }
}
