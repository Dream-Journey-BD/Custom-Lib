package com.dreamjourney.imagetilezoom.listener;

import android.graphics.PointF;

/**
 * Default implementation of {@link OnStateChangedListener}. This does nothing in any method.
 */
public class DefaultOnStateChangedListener implements OnStateChangedListener {

    @Override
    public void onCenterChanged(PointF newCenter, int origin) {
    }

    @Override
    public void onScaleChanged(float newScale, int origin) {
    }

}