package com.dreamjourney.imagetilezoom.tsak;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.dreamjourney.imagetilezoom.ImageTileZoomView;
import com.dreamjourney.imagetilezoom.decoder.ImageRegionDecoder;
import com.dreamjourney.imagetilezoom.model.Tile;

import java.lang.ref.WeakReference;

/**
 * Async task used to load images without blocking the UI thread.
 */
public class TileLoadTask extends AsyncTask<Void, Void, Bitmap> {
    private static final String TAG = "TileLoadTask_LOG";
    private final WeakReference<ImageRegionDecoder> decoderRef;
    private final WeakReference<ImageTileZoomView> viewRef;
    private final WeakReference<Tile> tileRef;
    private Exception exception;

    public TileLoadTask(ImageTileZoomView view, ImageRegionDecoder decoder, Tile tile) {
        this.viewRef = new WeakReference<>(view);
        this.decoderRef = new WeakReference<>(decoder);
        this.tileRef = new WeakReference<>(tile);
        tile.loading = true;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            ImageTileZoomView view = viewRef.get();
            ImageRegionDecoder decoder = decoderRef.get();
            Tile tile = tileRef.get();
            if (decoder != null && tile != null && view != null && decoder.isReady() && tile.visible) {
                view.debug("TileLoadTask.doInBackground, tile.sRect=%s, tile.sampleSize=%d", tile.sRect, tile.sampleSize);
                view.decoderLock.readLock().lock();
                try {
                    if (decoder.isReady()) {
                        // Update tile's file sRect according to rotation
                        view.fileSRect(tile.sRect, tile.fileSRect);
                        if (view.sRegion != null) {
                            tile.fileSRect.offset(view.sRegion.left, view.sRegion.top);
                        }
                        return decoder.decodeRegion(tile.fileSRect, tile.sampleSize);
                    } else {
                        tile.loading = false;
                    }
                } finally {
                    view.decoderLock.readLock().unlock();
                }
            } else if (tile != null) {
                tile.loading = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode tile", e);
            this.exception = e;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Failed to decode tile - OutOfMemoryError", e);
            this.exception = new RuntimeException(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        final ImageTileZoomView imageTileZoomView = viewRef.get();
        final Tile tile = tileRef.get();
        if (imageTileZoomView != null && tile != null) {
            if (bitmap != null) {
                tile.bitmap = bitmap;
                tile.loading = false;
                imageTileZoomView.onTileLoaded();
            } else if (exception != null && imageTileZoomView.onImageEventListener != null) {
                imageTileZoomView.onImageEventListener.onTileLoadError(exception);
            }
        }
    }
}