package com.evgeniysharafan.utils;

import android.text.format.DateUtils;

import java.util.Random;

/**
 * Not thread safe.
 */
@SuppressWarnings("unused")
public final class RandomUtils {

    private static final Random random;
    private static final char[] chars;
    private static final char[] charsDigits;
    private static final StringBuilder stringBuilder;
    private static final String loremIpsum;

    static {
        random = new Random();
        chars = " 1234567890 abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ !@#$%^&*() -= _+ \\| ,./ <>? ;' :\" [] {} ".toCharArray();
        charsDigits = "1234567890".toCharArray();
        stringBuilder = new StringBuilder();
        loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
    }

    private RandomUtils() {
    }

    public static int getInt() {
        return random.nextInt();
    }

    /**
     * Returns a pseudo-RANDOM uniformly distributed {@code int} in the
     * half-open range [0, n). If n == 0 returns 0.
     */
    public static int getInt(int n) {
        return n > 0 ? random.nextInt(n) : 0;
    }

    /**
     * Very ineffective, don't use it if you need to get result fast.
     * Includes min and max.
     */
    public static int getInt(int min, int max) {
        int result = getInt(max + 1);
        while (result < min) {
            result = getInt(max + 1);
        }

        return result;
    }

    public static double getDouble() {
        return random.nextDouble();
    }

    public static byte getByte() {
        byte[] buf = new byte[1];
        random.nextBytes(buf);
        return buf[0];
    }

    public static byte[] getBytes(int length) {
        byte[] buf = new byte[length];
        random.nextBytes(buf);
        return buf;
    }

    public static boolean getBoolean() {
        return random.nextBoolean();
    }

    public static String getString(int maxLength) {
        int length = getInt(maxLength + 1);

        stringBuilder.delete(0, stringBuilder.length());
        for (int i = 0; i < length; ++i) {
            stringBuilder.append(chars[getInt(chars.length)]);
        }

        return stringBuilder.toString();
    }

    public static String getLoremString(int maxLength, boolean allowEmptyStrings) {
        int length = getInt(maxLength > loremIpsum.length() ? loremIpsum.length() : maxLength + 1);
        if (!allowEmptyStrings && length == 0) {
            length++;
        }

        return loremIpsum.substring(0, length);
    }

    public static String getPhoneNumber() {
        boolean isShortFormat = getBoolean();
        int length = isShortFormat ? 9 : 12;

        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append(isShortFormat ? "0" : "+");

        for (int i = 0; i < length; ++i) {
            stringBuilder.append(charsDigits[getInt(charsDigits.length)]);
        }

        return stringBuilder.toString();
    }

    public static long getDateInMillis(int maxOffsetInDays) {
        int minusDays = getInt(maxOffsetInDays + 1);
        long minusSeconds = getInt((int) (DateUtils.DAY_IN_MILLIS / DateUtils.SECOND_IN_MILLIS));

        return System.currentTimeMillis() - (minusDays > 0 ? minusDays * DateUtils.DAY_IN_MILLIS
                : DateUtils.DAY_IN_MILLIS) + (minusSeconds * DateUtils.SECOND_IN_MILLIS);
    }

}
