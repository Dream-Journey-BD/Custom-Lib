package com.dreamjourney.imagetilezoom.tsak;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.dreamjourney.imagetilezoom.ImageTileZoomView;
import com.dreamjourney.imagetilezoom.decoder.ImageRegionDecoder;
import com.dreamjourney.imagetilezoom.model.Tile;

import java.lang.ref.WeakReference;

/**
 * Task used to load image tiles in background without AsyncTask.
 */
public class TileLoadTask implements Runnable {
    private static final String TAG = "TileLoadTask_LOG";

    private final WeakReference<ImageRegionDecoder> decoderRef;
    private final WeakReference<ImageTileZoomView> viewRef;
    private final WeakReference<Tile> tileRef;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public TileLoadTask(ImageTileZoomView view, ImageRegionDecoder decoder, Tile tile) {
        this.viewRef = new WeakReference<>(view);
        this.decoderRef = new WeakReference<>(decoder);
        this.tileRef = new WeakReference<>(tile);
        // Mark tile as loading before starting background work
        tile.loading = true;
    }

    @Override
    public void run() {
        Bitmap bitmap = null;
        Exception exception = null;

        try {
            ImageTileZoomView view = viewRef.get();
            ImageRegionDecoder decoder = decoderRef.get();
            Tile tile = tileRef.get();

            if (decoder != null && tile != null && view != null && decoder.isReady() && tile.visible) {
                view.debug("TileLoadTask.run (Background), tile.sRect=%s, tile.sampleSize=%d", tile.sRect, tile.sampleSize);

                // Acquire read lock to ensure decoder is not recycled while in use
                view.decoderLock.readLock().lock();
                try {
                    if (decoder.isReady()) {
                        // Calculate the actual source rectangle based on rotation/orientation
                        view.fileSRect(tile.sRect, tile.fileSRect);
                        if (view.sRegion != null) {
                            tile.fileSRect.offset(view.sRegion.left, view.sRegion.top);
                        }
                        // Decode the specific region of the image
                        bitmap = decoder.decodeRegion(tile.fileSRect, tile.sampleSize);
                    } else {
                        tile.loading = false;
                    }
                } finally {
                    // Always release the lock
                    view.decoderLock.readLock().unlock();
                }
            } else if (tile != null) {
                tile.loading = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode tile", e);
            exception = e;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Failed to decode tile - OutOfMemoryError", e);
            exception = new RuntimeException(e);
        }

        // Send results back to the UI Thread
        final Bitmap finalBitmap = bitmap;
        final Exception finalException = exception;

        mainHandler.post(() -> {
            ImageTileZoomView imageTileZoomView = viewRef.get();
            Tile tile = tileRef.get();

            if (imageTileZoomView != null && tile != null) {
                if (finalBitmap != null) {
                    tile.bitmap = finalBitmap;
                    tile.loading = false;
                    imageTileZoomView.onTileLoaded();
                } else {
                    tile.loading = false;
                    if (finalException != null && imageTileZoomView.onImageEventListener != null) {
                        imageTileZoomView.onImageEventListener.onTileLoadError(finalException);
                    }
                }
            }
        });
    }
}