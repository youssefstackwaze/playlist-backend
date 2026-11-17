package com.example.music.shuffle;

import com.example.music.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry of every {@link ShuffleStrategy} bean. Adding a new strategy only
 * requires declaring a new {@code @Component} - no change here (open/closed).
 */
@Component
public class ShuffleStrategyRegistry {

    public static final String DEFAULT = "random";

    private final Map<String, ShuffleStrategy> strategies;

    public ShuffleStrategyRegistry(List<ShuffleStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(s -> s.name().toLowerCase(Locale.ROOT), Function.identity()));
    }

    public ShuffleStrategy get(String name) {
        String key = (name == null || name.isBlank()) ? DEFAULT : name.toLowerCase(Locale.ROOT);
        ShuffleStrategy strategy = strategies.get(key);
        if (strategy == null) {
            throw new BadRequestException("Unknown shuffle strategy '" + name + "'. Available: " + strategies.keySet());
        }
        return strategy;
    }

    public Collection<ShuffleStrategy> all() {
        return strategies.values();
    }
}
