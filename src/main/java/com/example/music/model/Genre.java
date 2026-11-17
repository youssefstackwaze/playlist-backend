package com.example.music.model;

import java.util.EnumSet;
import java.util.Set;

public enum Genre {
    ROCK, POP, JAZZ, BLUES, CLASSICAL, ELECTRONIC, HIP_HOP, RNB, METAL, FOLK;

    /** Genres considered musically close to this one (used by recommendations). */
    public Set<Genre> compatibleGenres() {
        return switch (this) {
            case ROCK -> EnumSet.of(METAL, BLUES, FOLK, POP);
            case POP -> EnumSet.of(RNB, ELECTRONIC, ROCK);
            case JAZZ -> EnumSet.of(BLUES, CLASSICAL, RNB);
            case BLUES -> EnumSet.of(JAZZ, ROCK, RNB);
            case CLASSICAL -> EnumSet.of(JAZZ, FOLK);
            case ELECTRONIC -> EnumSet.of(POP, HIP_HOP);
            case HIP_HOP -> EnumSet.of(RNB, ELECTRONIC, POP);
            case RNB -> EnumSet.of(HIP_HOP, POP, JAZZ, BLUES);
            case METAL -> EnumSet.of(ROCK);
            case FOLK -> EnumSet.of(ROCK, CLASSICAL);
        };
    }

    public boolean isCompatibleWith(Genre other) {
        return this == other || compatibleGenres().contains(other);
    }
}
