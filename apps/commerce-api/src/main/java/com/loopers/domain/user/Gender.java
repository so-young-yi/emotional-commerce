package com.loopers.domain.user;

public enum Gender {
    M("남성"),
    F("여성");

    private final String description;

    Gender(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Gender from(String value) {
        for (Gender gender : Gender.values()) {
            if (gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + value);
    }

}
