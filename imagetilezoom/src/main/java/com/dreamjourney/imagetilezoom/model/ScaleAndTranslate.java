package com.dreamjourney.imagetilezoom.model;

import android.graphics.PointF;

public class ScaleAndTranslate {
    public float scale;
    public final PointF vTranslate;

    public ScaleAndTranslate(float scale, PointF vTranslate) {
        this.scale = scale;
        this.vTranslate = vTranslate;
    }
}