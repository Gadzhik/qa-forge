package ru.qacourse.m07.checks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.qacourse.m07.sut.Mutant;
import org.junit.jupiter.api.Tag;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/** Проверка задания. Не изменяйте этот файл. */
class TaggedTestCheck {

    private static Set<String> tags(String method) throws Exception {
        return Arrays.stream(ru.qacourse.m07.tasks.TaggedTest.class.getDeclaredMethod(method).getAnnotationsByType(Tag.class))
                .map(Tag::value).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("newCartIsEmpty помечен smoke")
    void smoke() throws Exception {
        assertThat(tags("newCartIsEmpty")).containsExactly("smoke");
    }

    @Test
    @DisplayName("totalWithPromo помечен regression и promo")
    void regression() throws Exception {
        assertThat(tags("totalWithPromo")).containsExactlyInAnyOrder("regression", "promo");
    }
}
