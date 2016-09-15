package com.evgeniysharafan.utils;

import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.ArrayRes;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Resources utils
 */
@SuppressWarnings("unused")
public final class Res {

    private static final Resources resources;

    static {
        resources = Utils.getApp().getResources();
    }

    private Res() {
    }

    public static Resources get() {
        return resources;
    }

    public static DisplayMetrics getDisplayMetrics() {
        return resources.getDisplayMetrics();
    }

    public static Configuration getConfiguration() {
        return resources.getConfiguration();
    }

    public static void updateConfiguration(Configuration config) {
        resources.updateConfiguration(config, getDisplayMetrics());
    }

    @NonNull
    public static String getString(@StringRes int resId) {
        return resources.getString(resId);
    }

    @NonNull
    public static String getString(@StringRes int resId, Object... formatArgs) {
        return resources.getString(resId, formatArgs);
    }

    @NonNull
    public static CharSequence getText(@StringRes int resId) {
        return resources.getText(resId);
    }

    public static CharSequence getText(@StringRes int resId, CharSequence def) {
        return resources.getText(resId, def);
    }

    public static int getInt(@IntegerRes int resId) {
        return resources.getInteger(resId);
    }

    public static boolean getBool(@BoolRes int resId) {
        return resources.getBoolean(resId);
    }

    @Nullable
    public static Drawable getDrawable(@DrawableRes int resId) {
        return AppCompatResources.getDrawable(Utils.getApp(), resId);
    }

    @ColorInt
    public static int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(Utils.getApp(), resId);
    }

    public static ColorStateList getColorStateList(@ColorRes int resId) {
        return AppCompatResources.getColorStateList(Utils.getApp(), resId);
    }

    @NonNull
    public static String getQuantityString(@PluralsRes int resId, int quantity) {
        return resources.getQuantityString(resId, quantity);
    }

    @NonNull
    public static String getQuantityString(@PluralsRes int resId, int quantity, Object... formatArgs) {
        return resources.getQuantityString(resId, quantity, formatArgs);
    }

    @NonNull
    public static CharSequence getQuantityText(@PluralsRes int resId, int quantity) {
        return resources.getQuantityText(resId, quantity);
    }

    public static float getDimen(@DimenRes int resId) {
        return resources.getDimension(resId);
    }

    public static int getDimensionPixelSize(@DimenRes int resId) {
        return resources.getDimensionPixelSize(resId);
    }

    @NonNull
    public static String[] getStringArray(@ArrayRes int resId) {
        return resources.getStringArray(resId);
    }

    @NonNull
    public static CharSequence[] getTextArray(@ArrayRes int resId) {
        return resources.getTextArray(resId);
    }

    @NonNull
    public static int[] getIntArray(@ArrayRes int resId) {
        return resources.getIntArray(resId);
    }

    public static AssetManager getAssets() {
        return resources.getAssets();
    }

    public static int convertToPixels(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getDisplayMetrics());
    }

    public static int convertToDips(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getStatusBarHeight() {
        int result = 0;

        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getDimensionPixelSize(resourceId);
        }

        return result;
    }

    public static void tint(ImageView view, @ColorInt int color) {
        if (view != null) {
            view.setImageDrawable(tint(view.getDrawable(), color));
        }
    }

    public static Drawable tint(Drawable image, @ColorInt int color) {
        if (image == null) {
            return null;
        }

        // we need this unwrap for old devices (e.g Android 4.1), without this tint works only once.
        Drawable tintedImage = DrawableCompat.wrap(DrawableCompat.unwrap(image));
        DrawableCompat.setTint(tintedImage, color);

        return tintedImage;
    }

    public static String readAssetAsString(String name) throws IOException {
        InputStream is = getAssets().open(name);
        Writer writer = new StringWriter();

        char[] buffer = new char[2048];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            IO.closeQuietly(is);
        }

        return writer.toString();
    }

}
