package ru.qacourse.m05;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Задание 5.10: основы многопоточности.
 */
public class Task10Concurrency {

    /** Потокобезопасный счётчик просмотров. Сейчас он теряет обновления при работе из нескольких потоков. */
    public static class ViewCounter {
        private int views = 0;

        public void increment() {
            views++;
        }

        public int get() {
            return views;
        }
    }

    /**
     * Выполняет все задачи параллельно в пуле из threads потоков и возвращает результаты
     * В ТОМ ЖЕ ПОРЯДКЕ, что и задачи. Пул обязательно закрыть.
     */
    public static <T> List<T> runAll(List<Callable<T>> tasks, int threads) throws Exception {
        throw new UnsupportedOperationException("TODO: напишите решение");
    }
}
