package com.loopers.interfaces.api.ranking;

public enum RankingPeriod {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly");

    private final String value;

    RankingPeriod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RankingPeriod fromValue(String value) {
        for (RankingPeriod period : values()) {
            if (period.value.equals(value)) {
                return period;
            }
        }
        throw new IllegalArgumentException("Invalid ranking period: " + value);
    }
}