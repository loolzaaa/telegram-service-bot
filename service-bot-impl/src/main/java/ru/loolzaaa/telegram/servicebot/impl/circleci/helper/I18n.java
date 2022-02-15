package ru.loolzaaa.telegram.servicebot.impl.circleci.helper;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static java.text.MessageFormat.format;

public class I18n {

    private static final String BASE_NAME = "messages";

    private static final Map<String, ResourceBundle> bundleMap = Map.of(
            "en", ResourceBundle.getBundle(BASE_NAME, new Locale("en")),
            "ru", ResourceBundle.getBundle(BASE_NAME, new Locale("ru"))
    );

    private static ResourceBundle currentBundle = bundleMap.get("en");

    public static String get(String key, String... args) {
        return format(currentBundle.getString(key), (Object[]) args);
    }

    public static void setCurrentBundle(String localeKey) {
        currentBundle = bundleMap.getOrDefault(localeKey, bundleMap.get("en"));
    }
}
