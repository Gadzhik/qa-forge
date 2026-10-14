package ru.qacourse.m06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class CustomerCheck {

    private static Method method(String name, Class<?>... params) {
        try {
            return Customer.class.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Test
    @DisplayName("Есть геттеры и сеттеры (@Data или @Getter/@Setter)")
    void accessors() throws Exception {
        assertThat(method("getName")).as("getName()").isNotNull();
        assertThat(method("setName", String.class)).as("setName(String)").isNotNull();
        assertThat(method("isVip")).as("isVip() — для boolean геттер начинается с is").isNotNull();
        Customer c = Customer.class.getConstructor().newInstance();
        method("setEmail", String.class).invoke(c, "anna@test.local");
        assertThat(method("getEmail").invoke(c)).isEqualTo("anna@test.local");
    }

    @Test
    @DisplayName("Есть конструктор без параметров и со всеми полями")
    void constructors() {
        assertThatCode(() -> Customer.class.getConstructor()).as("@NoArgsConstructor").doesNotThrowAnyException();
        assertThatCode(() -> Customer.class.getConstructor(String.class, String.class, String.class, boolean.class))
                .as("@AllArgsConstructor").doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Есть билдер: Customer.builder().name(...).build()")
    void builder() throws Exception {
        Method builder = method("builder");
        assertThat(builder).as("статический метод builder() — @Builder").isNotNull();
        Object b = builder.invoke(null);
        b.getClass().getMethod("name", String.class).invoke(b, "Анна");
        b.getClass().getMethod("vip", boolean.class).invoke(b, true);
        Object customer = b.getClass().getMethod("build").invoke(b);
        assertThat(method("getName").invoke(customer)).isEqualTo("Анна");
        assertThat(method("isVip").invoke(customer)).isEqualTo(true);
    }

    @Test
    @DisplayName("equals/hashCode по полям и понятный toString")
    void equalsToString() throws Exception {
        var ctor = Customer.class.getConstructor(String.class, String.class, String.class, boolean.class);
        Object a = ctor.newInstance("Анна", "a@t.ru", "+7", false);
        Object b = ctor.newInstance("Анна", "a@t.ru", "+7", false);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a.toString()).contains("Анна").contains("a@t.ru");
    }
}
