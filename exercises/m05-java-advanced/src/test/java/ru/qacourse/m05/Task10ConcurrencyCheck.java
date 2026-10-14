package ru.qacourse.m05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class Task10ConcurrencyCheck {

    @Test
    @DisplayName("ViewCounter не теряет обновления: 8 потоков × 10 000")
    void counter() throws Exception {
        var counter = new Task10Concurrency.ViewCounter();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int t = 0; t < 8; t++) {
            pool.submit(() -> { for (int i = 0; i < 10_000; i++) counter.increment(); });
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
        assertThat(counter.get()).isEqualTo(80_000);
    }

    @Test
    @DisplayName("runAll возвращает результаты в порядке задач")
    void runAll() throws Exception {
        List<Callable<String>> tasks = List.of(
                () -> { Thread.sleep(300); return "a"; },
                () -> "b",
                () -> { Thread.sleep(100); return "c"; });
        assertThat(Task10Concurrency.runAll(tasks, 3)).containsExactly("a", "b", "c");
    }

    @Test
    @DisplayName("runAll действительно выполняет параллельно")
    void parallel() throws Exception {
        List<Callable<Integer>> tasks = List.of(
                () -> { Thread.sleep(500); return 1; },
                () -> { Thread.sleep(500); return 2; },
                () -> { Thread.sleep(500); return 3; },
                () -> { Thread.sleep(500); return 4; });
        long start = System.currentTimeMillis();
        Task10Concurrency.runAll(tasks, 4);
        assertThat(System.currentTimeMillis() - start).isLessThan(1500);
    }
}
