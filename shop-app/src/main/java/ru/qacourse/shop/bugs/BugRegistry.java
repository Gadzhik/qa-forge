package ru.qacourse.shop.bugs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/** Хранит, какие баги сейчас включены. */
@Component
public class BugRegistry {

    private final Set<Bug> enabled = new CopyOnWriteArraySet<>();

    public BugRegistry(@Value("${shop.bugs:}") String bugs) {
        if (bugs.isBlank()) {
            return;
        }
        if (bugs.trim().equalsIgnoreCase("all")) {
            enabled.addAll(EnumSet.allOf(Bug.class));
            return;
        }
        Arrays.stream(bugs.split(","))
                .filter(s -> !s.isBlank())
                .map(Bug::fromCode)
                .forEach(enabled::add);
    }

    public boolean isOn(Bug bug) {
        return enabled.contains(bug);
    }

    public Set<Bug> enabled() {
        return Set.copyOf(enabled);
    }

    public void set(Collection<Bug> bugs) {
        enabled.clear();
        enabled.addAll(bugs);
    }

    public void toggle(Bug bug, boolean on) {
        if (on) enabled.add(bug); else enabled.remove(bug);
    }
}
