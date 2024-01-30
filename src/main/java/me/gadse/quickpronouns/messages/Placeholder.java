package me.gadse.quickpronouns.messages;

import java.util.regex.Pattern;

public enum Placeholder {
    TARGET,
    PRONOUNS_DISPLAY;

    private final Pattern placeholderPattern = Pattern.compile("%" + name().toLowerCase() + "%");

    public String apply(String source, String replacement) {
        return placeholderPattern.matcher(source).replaceAll(replacement);
    }
}
