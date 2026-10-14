package ru.qacourse.m10.tasks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.qacourse.m10.Browser;
import ru.qacourse.support.ShopConfig;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;

/**
 * Задание 10.7: Actions и JavascriptExecutor.
 * 1) /playground/drag-drop: перетащить [data-testid=draggable] на [data-testid=droppable] → статус «на тарелке».
 * 2) /playground/hover: навести мышь на [data-testid=hover-target] → пункты меню [data-testid=hover-item] видимы;
 *    двойной клик по [data-testid=double-click-btn] → результат «двойной клик»;
 *    правый клик по [data-testid=right-click-btn] → «правый клик».
 * 3) /playground/scroll: прокрутить страницу вниз JavaScript'ом несколько раз, пока не появится [data-testid=feed-end]
 *    (или пока элементов не станет 100); проверить, что элементов [data-testid=feed-item] 100.
 */
public class ActionsJsTest {
    @Test
    void actions() {
        fail("TODO: напишите тест");
    }
}
