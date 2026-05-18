package com.dreamjourney.imagetilezoom.model;

import android.graphics.PointF;

import com.dreamjourney.imagetilezoom.ImageTileZoomView;
import com.dreamjourney.imagetilezoom.listener.OnAnimationEventListener;

public class Anim {
    public float scaleStart; // Scale at start of anim
    public float scaleEnd; // Scale at end of anim (target)
    public PointF sCenterStart; // Source center point at start
    public PointF sCenterEnd; // Source center point at end, adjusted for pan limits
    public PointF sCenterEndRequested; // Source center point that was requested, without adjustment
    public PointF vFocusStart; // View point that was double tapped
    public PointF vFocusEnd; // Where the view focal point should be moved to during the anim
    public long duration = 500; // How long the anim takes
    public boolean interruptible = true; // Whether the anim can be interrupted by a touch
    public int easing = ImageTileZoomView.EASE_IN_OUT_QUAD; // Easing style
    public int origin = ImageTileZoomView.ORIGIN_ANIM; // Animation origin (API, double tap or fling)
    public long time = System.currentTimeMillis(); // Start time
    public OnAnimationEventListener listener; // Event listener
}