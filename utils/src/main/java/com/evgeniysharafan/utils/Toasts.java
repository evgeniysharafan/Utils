package com.evgeniysharafan.utils;

import android.view.View;
import android.widget.Toast;

/**
 * Toast utils
 */
@SuppressWarnings("unused")
public final class Toasts {

    private static final String FUTURE_MESSAGE = "This functionality will be implemented in the near future";

    private Toasts() {
    }

    public static Toast showShort(int resId) {
        return showShort(Res.getText(resId));
    }

    public static Toast showShort(CharSequence message) {
        return show(message, Toast.LENGTH_SHORT);
    }

    public static Toast showLong(int resId) {
        return showLong(Res.getText(resId));
    }

    public static Toast showLong(CharSequence message) {
        return show(message, Toast.LENGTH_LONG);
    }

    public static Toast showFuture() {
        return show(FUTURE_MESSAGE, Toast.LENGTH_LONG);
    }

    public static void showShortFromBackground(int resId) {
        showShortFromBackground(Res.getText(resId));
    }

    public static void showShortFromBackground(CharSequence message) {
        showFromBackground(message, Toast.LENGTH_SHORT);
    }

    public static void showLongFromBackground(int resId) {
        showLongFromBackground(Res.getText(resId));
    }

    public static void showLongFromBackground(CharSequence message) {
        showFromBackground(message, Toast.LENGTH_LONG);
    }

    public static boolean needShow(Toast toast) {
        return toast == null || toast.getView().getWindowVisibility() != View.VISIBLE;
    }

    private static void showFromBackground(final CharSequence text, final int duration) {
        Utils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                show(text, duration);
            }
        });
    }

    private static Toast show(CharSequence text, int duration) {
        Toast toast = Toast.makeText(Utils.getApp(), text, duration);
        toast.show();
        return toast;
    }

}
