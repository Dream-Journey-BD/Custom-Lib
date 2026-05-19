package com.dreamjourney.imagetilezoom.tsak;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.dreamjourney.imagetilezoom.ImageTileZoomView;
import com.dreamjourney.imagetilezoom.decoder.DecoderFactory;
import com.dreamjourney.imagetilezoom.decoder.ImageRegionDecoder;

import java.lang.ref.WeakReference;

/**
 * Task used to get image details and initialize the decoder without AsyncTask.
 */
public class TilesInitTask implements Runnable {
    private static final String TAG = "TilesInitTask_LOG";

    private final WeakReference<ImageTileZoomView> viewRef;
    private final WeakReference<Context> contextRef;
    private final WeakReference<DecoderFactory<? extends ImageRegionDecoder>> decoderFactoryRef;
    private final Uri source;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public TilesInitTask(ImageTileZoomView view, Context context, DecoderFactory<? extends ImageRegionDecoder> decoderFactory, Uri source) {
        this.viewRef = new WeakReference<>(view);
        this.contextRef = new WeakReference<>(context);
        this.decoderFactoryRef = new WeakReference<>(decoderFactory);
        this.source = source;
    }

    @Override
    public void run() {
        // Background execution starts here
        ImageRegionDecoder decoder = null;
        int[] results = null; // Stores: [width, height, orientation]
        Exception exception = null;

        try {
            Context context = contextRef.get();
            DecoderFactory<? extends ImageRegionDecoder> decoderFactory = decoderFactoryRef.get();
            ImageTileZoomView view = viewRef.get();

            if (context != null && decoderFactory != null && view != null) {
                view.debug("TilesInitTask.run (Background)");

                // Initialize the decoder
                decoder = decoderFactory.make();
                Point dimensions = decoder.init(context, source);
                int sWidth = dimensions.x;
                int sHeight = dimensions.y;

                // Get rotation from EXIF data
                int exifOrientation = view.getExifOrientation(context, source.toString());

                // Handle region/cropping if specified
                if (view.sRegion != null) {
                    view.sRegion.left = Math.max(0, view.sRegion.left);
                    view.sRegion.top = Math.max(0, view.sRegion.top);
                    view.sRegion.right = Math.min(sWidth, view.sRegion.right);
                    view.sRegion.bottom = Math.min(sHeight, view.sRegion.bottom);
                    sWidth = view.sRegion.width();
                    sHeight = view.sRegion.height();
                }

                results = new int[]{sWidth, sHeight, exifOrientation};
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialise bitmap decoder", e);
            exception = e;
        }

        // Post results back to the UI thread
        final ImageRegionDecoder finalDecoder = decoder;
        final int[] finalResults = results;
        final Exception finalException = exception;

        mainHandler.post(() -> {
            ImageTileZoomView view = viewRef.get();
            if (view != null) {
                if (finalDecoder != null && finalResults != null) {
                    // Notify the view that initialization is complete
                    view.onTilesInited(finalDecoder, finalResults[0], finalResults[1], finalResults[2]);
                } else if (finalException != null && view.onImageEventListener != null) {
                    // Notify listener about the error
                    view.onImageEventListener.onImageLoadError(finalException);
                }
            }
        });
    }
}