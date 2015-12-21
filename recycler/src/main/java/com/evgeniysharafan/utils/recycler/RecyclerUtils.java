package com.evgeniysharafan.utils.recycler;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.widget.EdgeEffect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class RecyclerUtils {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setEdgeEffectL(final RecyclerView recyclerView, final int color) {
        try {
            final Class<?> clazz = RecyclerView.class;
            for (final String name : new String[]{"ensureTopGlow", "ensureBottomGlow"}) {
                Method method = clazz.getDeclaredMethod(name);
                method.setAccessible(true);
                method.invoke(recyclerView);
            }
            for (final String name : new String[]{"mTopGlow", "mBottomGlow"}) {
                final Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                final Object edge = field.get(recyclerView); // android.support.v4.widget.EdgeEffectCompat
                final Field fEdgeEffect = edge.getClass().getDeclaredField("mEdgeEffect");
                fEdgeEffect.setAccessible(true);
                ((EdgeEffect) fEdgeEffect.get(edge)).setColor(color);
            }
        } catch (final Exception ignored) {
        }
    }

}
