package ru.qacourse.m05;

/**
 * Своё исключение для неправильного количества (менять не нужно).
 * Наследуется от RuntimeException — значит, оно «непроверяемое».
 */
public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException(String message) {
        super(message);
    }

    public InvalidQuantityException(String message, Throwable cause) {
        super(message, cause);
    }
}
