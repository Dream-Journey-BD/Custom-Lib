package com.dreamjourney.imagetilezoom.listener;


import android.graphics.PointF;

/**
 * An event listener, allowing activities to be notified of pan and zoom events. Initialisation
 * and calls made by your code do not trigger events; touch events and animations do. Methods in
 * this listener will be called on the UI thread and may be called very frequently - your
 * implementation should return quickly.
 */
@SuppressWarnings("EmptyMethod")
public interface OnStateChangedListener {

    /**
     * The scale has changed. Use with { ImageTileZoomView #getMaxScale} and { ImageTileZoomView #getMinScale()} to determine
     * whether the image is fully zoomed in or out.
     *
     * @param newScale The new scale.
     * @param origin   Where the event originated from - one of { ImageTileZoomView #ORIGIN_ANIM}, { ImageTileZoomView #ORIGIN_TOUCH}.
     */
    void onScaleChanged(float newScale, int origin);

    /**
     * The source center has been changed. This can be a result of panning or zooming.
     *
     * @param newCenter The new source center point.
     * @param origin    Where the event originated from - one of {ImageTileZoomView #ORIGIN_ANIM}, {ImageTileZoomView #ORIGIN_TOUCH}.
     */
    void onCenterChanged(PointF newCenter, int origin);

}