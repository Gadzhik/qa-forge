package ru.qacourse.support;

import java.util.List;

/** Итог прогона тестов студента. */
public record TestRunResult(long found, long passed, long failed, long aborted, List<String> failures) {
    public boolean allPassed() {
        return found > 0 && failed == 0 && aborted == 0;
    }

    public boolean anyFailed() {
        return failed > 0;
    }

    public String describe() {
        StringBuilder sb = new StringBuilder("найдено тестов: " + found + ", прошло: " + passed + ", упало: " + failed);
        if (aborted > 0) sb.append(", пропущено: ").append(aborted);
        failures.forEach(f -> sb.append(System.lineSeparator()).append("  ✗ ").append(f));
        return sb.toString();
    }
}
