package com.evgeniysharafan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public final class PrefUtils {

    private static final SharedPreferences defaultPreferences;

    static {
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(Utils.getApp());
    }

    private PrefUtils() {
    }

    public static SharedPreferences get() {
        return defaultPreferences;
    }

    public static SharedPreferences get(String name) {
        return Utils.getApp().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static String getString(String key, String defValue) {
        return getString(defaultPreferences, key, defValue);
    }

    public static String getString(SharedPreferences preferences, String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public static Set<String> getStringSet(String key, Set<String> defValues) {
        return getStringSet(defaultPreferences, key, defValues);
    }

    public static Set<String> getStringSet(SharedPreferences preferences, String key, Set<String> defValues) {
        return preferences.getStringSet(key, defValues);
    }

    public static boolean getBool(String key, boolean defValue) {
        return getBool(defaultPreferences, key, defValue);
    }

    public static boolean getBool(SharedPreferences preferences, String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public static int getInt(String key, int defValue) {
        return getInt(defaultPreferences, key, defValue);
    }

    public static int getInt(SharedPreferences preferences, String key, int defValue) {
        return preferences.getInt(key, defValue);
    }

    public static long getLong(String key, long defValue) {
        return getLong(defaultPreferences, key, defValue);
    }

    public static long getLong(SharedPreferences preferences, String key, long defValue) {
        return preferences.getLong(key, defValue);
    }

    public static float getFloat(String key, float defValue) {
        return getFloat(defaultPreferences, key, defValue);
    }

    public static float getFloat(SharedPreferences preferences, String key, float defValue) {
        return preferences.getFloat(key, defValue);
    }

    public static void put(String key, String value) {
        put(defaultPreferences, key, value);
    }

    public static void put(SharedPreferences preferences, String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    public static void put(String key, Set<String> values) {
        put(defaultPreferences, key, values);
    }

    public static void put(SharedPreferences preferences, String key, Set<String> values) {
        preferences.edit().putStringSet(key, values).apply();
    }

    public static void put(String key, boolean value) {
        put(defaultPreferences, key, value);
    }

    public static void put(SharedPreferences preferences, String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public static void put(String key, int value) {
        put(defaultPreferences, key, value);
    }

    public static void put(SharedPreferences preferences, String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public static void put(String key, long value) {
        put(defaultPreferences, key, value);
    }

    public static void put(SharedPreferences preferences, String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    public static void put(String key, float value) {
        put(defaultPreferences, key, value);
    }

    public static void put(SharedPreferences preferences, String key, float value) {
        preferences.edit().putFloat(key, value).apply();
    }

    public static boolean contains(String key) {
        return contains(defaultPreferences, key);
    }

    public static boolean contains(SharedPreferences preferences, String key) {
        return preferences.contains(key);
    }

    public static void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        registerOnSharedPreferenceChangeListener(defaultPreferences, listener);
    }

    public static void registerOnSharedPreferenceChangeListener(SharedPreferences preferences,
                                                                OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        unregisterOnSharedPreferenceChangeListener(defaultPreferences, listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(SharedPreferences preferences,
                                                                  OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static void remove(String key) {
        remove(defaultPreferences, key);
    }

    public static void remove(SharedPreferences preferences, String key) {
        preferences.edit().remove(key).apply();
    }

    // Need to test
    public static Map<String, ?> getAll() {
        return getAll(defaultPreferences);
    }

    // Need to test
    public static Map<String, ?> getAll(SharedPreferences preferences) {
        return preferences.getAll();
    }

    // Need to test
    public static void dump() {
        dump(defaultPreferences);
    }

    // Need to test
    public static void dump(SharedPreferences preferences) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dumping shared preferences...\n");

        Map<String, ?> all = getAll();
        if (all != null) {
            for (Map.Entry<String, ?> entry : all.entrySet()) {
                Object val = entry.getValue();
                if (val == null) {
                    sb.append(String.format("%s = <null>%n", entry.getKey()));
                } else {
                    sb.append(String.format("%s = %s (%s)%n", entry.getKey(), String.valueOf(val),
                            val.getClass().getSimpleName()));
                }
            }
        }

        sb.append("Dump complete\n");
        L.d(sb.toString());
    }

}
