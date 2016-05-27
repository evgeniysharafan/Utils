package com.evgeniysharafan.utils;

import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
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

    public static String getString(int resId) {
        return resources.getString(resId);
    }

    public static String getString(int resId, Object... formatArgs) {
        return resources.getString(resId, formatArgs);
    }

    public static CharSequence getText(int resId) {
        return resources.getText(resId);
    }

    public static CharSequence getText(int resId, CharSequence def) {
        return resources.getText(resId, def);
    }

    public static int getInt(int id) {
        return resources.getInteger(id);
    }

    public static boolean getBool(int id) {
        return resources.getBoolean(id);
    }

    public static Drawable getDrawable(int id) {
        //noinspection deprecation
        return resources.getDrawable(id);
    }

    public static int getColor(int id) {
        //noinspection deprecation
        return resources.getColor(id);
    }

    public static ColorStateList getColorStateList(int id) {
        //noinspection deprecation
        return resources.getColorStateList(id);
    }

    public static String getQuantityString(int id, int quantity) {
        return resources.getQuantityString(id, quantity);
    }

    public static String getQuantityString(int id, int quantity, Object... formatArgs) {
        return resources.getQuantityString(id, quantity, formatArgs);
    }

    public static CharSequence getQuantityText(int id, int quantity) {
        return resources.getQuantityText(id, quantity);
    }

    public static float getDimen(int id) {
        return resources.getDimension(id);
    }

    public static int getDimensionPixelSize(int id) {
        return resources.getDimensionPixelSize(id);
    }

    public static String[] getStringArray(int id) {
        return resources.getStringArray(id);
    }

    public static CharSequence[] getTextArray(int id) {
        return resources.getTextArray(id);
    }

    public static int[] getIntArray(int id) {
        return resources.getIntArray(id);
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

    public static void tint(ImageView view, int color) {
        if (view != null) {
            view.setImageDrawable(tint(view.getDrawable(), color));
        }
    }

    public static Drawable tint(Drawable image, int color) {
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
            try {
                is.close();
            } catch (IOException e) {
                // noop
            }
        }

        return writer.toString();
    }

}
