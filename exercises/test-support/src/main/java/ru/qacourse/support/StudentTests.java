package ru.qacourse.support;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import ru.qacourse.shop.launcher.ShopAppLauncher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

/**
 * Запускает тестовый класс студента программно (как это делает IDE) против
 * отдельного экземпляра QA Burger с нужными багами.
 */
public final class StudentTests {
    private StudentTests() {
    }

    /** Прогнать тесты класса против нового экземпляра приложения с указанными багами (пустая строка = без багов). */
    public static TestRunResult runAgainstShop(Class<?> testClass, String bugs) {
        return runAgainstShop(testClass, bugs, Map.of());
    }

    public static TestRunResult runAgainstShop(Class<?> testClass, String bugs, Map<String, Object> extraProps) {
        Map<String, Object> props = new HashMap<>(extraProps);
        props.put("shop.bugs", bugs);
        props.putIfAbsent("shop.kitchen.cooking-delay", "PT1S");
        props.putIfAbsent("shop.kitchen.ready-delay", "PT1S");
        String previous = System.getProperty("shop.url");
        String previousDb = System.getProperty("shop.db.url");
        try (ShopAppLauncher.Running app = ShopAppLauncher.start(props)) {
            System.setProperty("shop.url", app.baseUrl());
            System.setProperty("shop.db.url", app.jdbcUrl());
            return run(testClass);
        } finally {
            restore("shop.url", previous);
            restore("shop.db.url", previousDb);
        }
    }

    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key); else System.setProperty(key, value);
    }

    /** Просто прогнать тестовый класс и собрать результат. */
    public static TestRunResult run(Class<?> testClass) {
        LauncherDiscoveryRequest request = request().selectors(selectClass(testClass)).build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener summary = new SummaryGeneratingListener();
        List<String> failures = new ArrayList<>();
        TestExecutionListener collector = new TestExecutionListener() {
            @Override
            public void executionFinished(TestIdentifier id, TestExecutionResult result) {
                if (id.isTest() && result.getStatus() == TestExecutionResult.Status.FAILED) {
                    String reason = result.getThrowable().map(t -> firstLine(t.getMessage(), t)).orElse("");
                    failures.add(id.getDisplayName() + " — " + reason);
                }
            }
        };
        launcher.execute(request, summary, collector);
        TestExecutionSummary s = summary.getSummary();
        return new TestRunResult(s.getTestsFoundCount(), s.getTestsSucceededCount(), s.getTestsFailedCount(),
                s.getTestsAbortedCount(), failures);
    }

    private static String firstLine(String message, Throwable t) {
        if (message == null) return t.getClass().getSimpleName();
        String line = message.strip().lines().findFirst().orElse("");
        return line.length() > 200 ? line.substring(0, 200) + "…" : line;
    }
}
