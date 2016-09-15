package com.evgeniysharafan.utils;

import android.content.Context;
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
import android.support.annotation.FractionRes;
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

    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context == null ? getDisplayMetrics() : context.getResources().getDisplayMetrics();
    }

    public static Configuration getConfiguration() {
        return resources.getConfiguration();
    }

    public static Configuration getConfiguration(Context context) {
        return context == null ? getConfiguration() : context.getResources().getConfiguration();
    }

    public static void updateConfiguration(Configuration config) {
        resources.updateConfiguration(config, getDisplayMetrics());
    }

    public static void updateConfiguration(Context context, Configuration config) {
        if (context == null) {
            updateConfiguration(config);
        } else {
            context.getResources().updateConfiguration(config, getDisplayMetrics(context));
        }
    }

    @NonNull
    public static String getString(@StringRes int resId) {
        return resources.getString(resId);
    }

    @NonNull
    public static String getString(Context context, @StringRes int resId) {
        return context == null ? getString(resId) : context.getResources().getString(resId);
    }

    @NonNull
    public static String getString(@StringRes int resId, Object... formatArgs) {
        return resources.getString(resId, formatArgs);
    }

    @NonNull
    public static String getString(Context context, @StringRes int resId, Object... formatArgs) {
        return context == null ? getString(resId, formatArgs) : context.getResources().getString(resId, formatArgs);
    }

    @NonNull
    public static CharSequence getText(@StringRes int resId) {
        return resources.getText(resId);
    }

    @NonNull
    public static CharSequence getText(Context context, @StringRes int resId) {
        return context == null ? getText(resId) : context.getResources().getText(resId);
    }

    public static CharSequence getText(@StringRes int resId, CharSequence def) {
        return resources.getText(resId, def);
    }

    public static CharSequence getText(Context context, @StringRes int resId, CharSequence def) {
        return context == null ? getText(resId, def) : context.getResources().getText(resId, def);
    }

    public static int getInt(@IntegerRes int resId) {
        return resources.getInteger(resId);
    }

    public static int getInt(Context context, @IntegerRes int resId) {
        return context == null ? getInt(resId) : context.getResources().getInteger(resId);
    }

    public static boolean getBool(@BoolRes int resId) {
        return resources.getBoolean(resId);
    }

    public static boolean getBool(Context context, @BoolRes int resId) {
        return context == null ? getBool(resId) : context.getResources().getBoolean(resId);
    }

    @Nullable
    public static Drawable getDrawable(@DrawableRes int resId) {
        return AppCompatResources.getDrawable(Utils.getApp(), resId);
    }

    @Nullable
    public static Drawable getDrawable(Context context, @DrawableRes int resId) {
        return context == null ? getDrawable(resId) : AppCompatResources.getDrawable(context, resId);
    }

    @ColorInt
    public static int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(Utils.getApp(), resId);
    }

    @ColorInt
    public static int getColor(Context context, @ColorRes int resId) {
        return context == null ? getColor(resId) : ContextCompat.getColor(context, resId);
    }

    public static ColorStateList getColorStateList(@ColorRes int resId) {
        return AppCompatResources.getColorStateList(Utils.getApp(), resId);
    }

    public static ColorStateList getColorStateList(Context context, @ColorRes int resId) {
        return context == null ? getColorStateList(resId) : AppCompatResources.getColorStateList(context, resId);
    }

    @NonNull
    public static String getQuantityString(@PluralsRes int resId, int quantity) {
        return resources.getQuantityString(resId, quantity);
    }

    @NonNull
    public static String getQuantityString(Context context, @PluralsRes int resId, int quantity) {
        return context == null ? getQuantityString(resId, quantity) : context.getResources().getQuantityString(resId, quantity);
    }

    @NonNull
    public static String getQuantityString(@PluralsRes int resId, int quantity, Object... formatArgs) {
        return resources.getQuantityString(resId, quantity, formatArgs);
    }

    @NonNull
    public static String getQuantityString(Context context, @PluralsRes int resId, int quantity, Object... formatArgs) {
        return context == null ? getQuantityString(resId, quantity, formatArgs) :
                context.getResources().getQuantityString(resId, quantity, formatArgs);
    }

    @NonNull
    public static CharSequence getQuantityText(@PluralsRes int resId, int quantity) {
        return resources.getQuantityText(resId, quantity);
    }

    @NonNull
    public static CharSequence getQuantityText(Context context, @PluralsRes int resId, int quantity) {
        return context == null ? getQuantityText(resId, quantity) : context.getResources().getQuantityText(resId, quantity);
    }

    public static float getDimen(@DimenRes int resId) {
        return resources.getDimension(resId);
    }

    public static float getDimen(Context context, @DimenRes int resId) {
        return context == null ? getDimen(resId) : context.getResources().getDimension(resId);
    }

    public static int getDimensionPixelSize(@DimenRes int resId) {
        return resources.getDimensionPixelSize(resId);
    }

    public static int getDimensionPixelSize(Context context, @DimenRes int resId) {
        return context == null ? getDimensionPixelSize(resId) : context.getResources().getDimensionPixelSize(resId);
    }

    @NonNull
    public static String[] getStringArray(@ArrayRes int resId) {
        return resources.getStringArray(resId);
    }

    @NonNull
    public static String[] getStringArray(Context context, @ArrayRes int resId) {
        return context == null ? getStringArray(resId) : context.getResources().getStringArray(resId);
    }

    @NonNull
    public static CharSequence[] getTextArray(@ArrayRes int resId) {
        return resources.getTextArray(resId);
    }

    @NonNull
    public static CharSequence[] getTextArray(Context context, @ArrayRes int resId) {
        return context == null ? getTextArray(resId) : context.getResources().getTextArray(resId);
    }

    @NonNull
    public static int[] getIntArray(@ArrayRes int resId) {
        return resources.getIntArray(resId);
    }

    @NonNull
    public static int[] getIntArray(Context context, @ArrayRes int resId) {
        return context == null ? getIntArray(resId) : context.getResources().getIntArray(resId);
    }

    public static AssetManager getAssets() {
        return resources.getAssets();
    }

    public static AssetManager getAssets(Context context) {
        return context == null ? getAssets() : context.getResources().getAssets();
    }

    public static float getFraction(@FractionRes int resId, int base, int pbase) {
        return resources.getFraction(resId, base, pbase);
    }

    public static float getFraction(Context context, @FractionRes int resId, int base, int pbase) {
        return context == null ? getFraction(resId, base, pbase) : context.getResources().getFraction(resId, base, pbase);
    }

    public static int convertToPixels(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getDisplayMetrics());
    }

    public static int convertToPixels(Context context, float dp) {
        return context == null ? convertToPixels(dp) :
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getDisplayMetrics(context));
    }

    public static int convertToDips(int px) {
        return (int) (px / getDisplayMetrics().density);
    }

    public static int convertToDips(Context context, int px) {
        return context == null ? convertToDips(px) : (int) (px / getDisplayMetrics(context).density);
    }

    public static int getStatusBarHeight() {
        int result = 0;

        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getDimensionPixelSize(resourceId);
        }

        return result;
    }

    public static int getStatusBarHeight(Context context) {
        if (context == null) {
            return getStatusBarHeight();
        }

        int result = 0;

        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
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
