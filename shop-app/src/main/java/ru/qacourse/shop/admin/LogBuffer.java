package ru.qacourse.shop.admin;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/** Logback-аппендер, который держит последние 1000 строк лога в памяти (для /api/debug/logs). */
public class LogBuffer extends AppenderBase<ILoggingEvent> {
    private static final Deque<String> LINES = new ConcurrentLinkedDeque<>();

    @Override
    protected void append(ILoggingEvent e) {
        String line = Instant.ofEpochMilli(e.getTimeStamp()) + " " + e.getLevel() + " [" + e.getThreadName() + "] "
                + e.getLoggerName().replaceAll("^.*\\.", "") + " - " + e.getFormattedMessage();
        if (e.getThrowableProxy() != null) {
            line += System.lineSeparator() + ThrowableProxyUtil.asString(e.getThrowableProxy());
        }
        LINES.addLast(line);
        while (LINES.size() > 1000) LINES.pollFirst();
    }

    public static List<String> tail(int n) {
        List<String> all = new ArrayList<>(LINES);
        return all.subList(Math.max(0, all.size() - n), all.size());
    }
}
