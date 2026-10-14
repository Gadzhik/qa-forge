package ru.qacourse.shop.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.qacourse.shop.bugs.Bug;
import ru.qacourse.shop.bugs.BugRegistry;
import ru.qacourse.shop.events.EventLog;
import ru.qacourse.shop.events.OrderEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Служебные ручки тренажёра. В настоящем продукте таких открытых ручек быть не должно —
 * здесь они нужны, чтобы тесты могли готовить окружение.
 */
@RestController
@Tag(name = "Служебное (тренажёр)")
public class AdminController {

    public record BugInfo(String code, String area, String title, String report, String where, boolean enabled) {
    }

    private final BugRegistry bugs;
    private final DataSeeder seeder;
    private final EventLog eventLog;

    public AdminController(BugRegistry bugs, DataSeeder seeder, EventLog eventLog) {
        this.bugs = bugs;
        this.seeder = seeder;
        this.eventLog = eventLog;
    }

    @GetMapping("/api/admin/bugs")
    @Operation(summary = "Каталог багов и какие включены")
    public List<BugInfo> bugs() {
        return Arrays.stream(Bug.values())
                .map(b -> new BugInfo(b.code(), b.area(), b.title(), b.report(), b.where(), bugs.isOn(b)))
                .toList();
    }

    @PutMapping("/api/admin/bugs")
    @Operation(summary = "Задать список включённых багов, например [\"BUG-101\"]")
    public List<BugInfo> setBugs(@RequestBody List<String> codes) {
        bugs.set(codes.stream().map(Bug::fromCode).toList());
        return bugs();
    }

    @PostMapping("/api/admin/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Сбросить данные к начальному состоянию")
    public void reset() {
        seeder.reset();
    }

    @GetMapping("/api/debug/logs")
    @Operation(summary = "Последние строки лога приложения")
    public List<String> logs(@RequestParam(defaultValue = "100") int lines) {
        return LogBuffer.tail(Math.max(1, Math.min(lines, 1000)));
    }

    @GetMapping("/api/debug/events")
    @Operation(summary = "Последние события о заказах (то же, что уходит в Kafka)")
    public List<OrderEvent> events(@RequestParam(required = false) Long orderId) {
        return eventLog.all().stream().filter(e -> orderId == null || e.orderId() == orderId).toList();
    }
}
