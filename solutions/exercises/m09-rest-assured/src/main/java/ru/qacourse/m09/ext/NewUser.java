package ru.qacourse.m09.ext;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для параметра теста: «дай мне нового зарегистрированного пользователя».
 *   @Test void test(@NewUser ApiUser user) { ... }
 * Расширение подключается автоматически благодаря @ExtendWith на самой аннотации.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(NewUserResolver.class)
public @interface NewUser {
}
