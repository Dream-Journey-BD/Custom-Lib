package com.dreamjourney.imagetilezoom.model;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Tile {

    public Rect sRect;
    public int sampleSize;
    public Bitmap bitmap;
    public boolean loading;
    public boolean visible;

    // Volatile fields instantiated once then updated before use to reduce GC.
    public Rect vRect;
    public Rect fileSRect;

}