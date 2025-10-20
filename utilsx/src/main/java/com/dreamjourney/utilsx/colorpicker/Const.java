package com.dreamjourney.utilsx.colorpicker;

import android.graphics.Color;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;


import com.dreamjourney.utilsx.R;

import java.util.Locale;

public class Const {
    // Color Piker
    public static float[] hsvColor = new float[3];
    private static int alpha = 255;

    public static int getColor() {
        final int argb = Color.HSVToColor(hsvColor);
        return alpha << 24 | (argb & 0x00ffffff);
    }

    public static float getSquareColor() {
        return hsvColor[0];
    }

    public static void setHValue(float hue) {
        hsvColor[0] = hue;
    }

    public static void setSValue(float sat) {
        hsvColor[1] = sat;
    }

    public static void setBvValue(float val) {
        hsvColor[2] = val;
    }

    public static float getBvValue() {
        return hsvColor[2];
    }

    public static float getSValue() {
        return hsvColor[1];
    }

    public static float getAlphaValue() {
        return alpha;
    }

    public static void setAlphaValue(int a) {
        alpha = a;
    }

    public static void fadeIn(@NonNull View view) {
        Animation fadeIn =
                AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
        fadeIn.setFillAfter(true);
        view.startAnimation(fadeIn);
    }

    public static void fadeOut(@NonNull View view) {
        Animation fadeOut =
                AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
        fadeOut.setFillAfter(true);
        view.startAnimation(fadeOut);
    }

    @NonNull
    public static String getHexCode(@ColorInt int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format(Locale.getDefault(), "%02X%02X%02X%02X", a, r, g, b);
    }

    @NonNull
    public static int[] getColorARGB(@ColorInt int color) {
        int[] argb = new int[4];
        argb[0] = Color.alpha(color);
        argb[1] = Color.red(color);
        argb[2] = Color.green(color);
        argb[3] = Color.blue(color);
        return argb;
    }

}
