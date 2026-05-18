package com.dreamjourney.imagetilezoom.tsak;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.dreamjourney.imagetilezoom.ImageTileZoomView;
import com.dreamjourney.imagetilezoom.decoder.DecoderFactory;
import com.dreamjourney.imagetilezoom.decoder.ImageDecoder;

import java.lang.ref.WeakReference;

/**
 * Async task used to load bitmap without blocking the UI thread.
 */
public class BitmapLoadTask extends AsyncTask<Void, Void, Integer> {
    private final WeakReference<DecoderFactory<? extends ImageDecoder>> decoderFactoryRef;
    private static final String TAG = "BitmapLoadTask_LOG";
    private final WeakReference<ImageTileZoomView> viewRef;
    private final WeakReference<Context> contextRef;
    private final Uri source;
    private final boolean preview;
    private Bitmap bitmap;
    private Exception exception;

    public BitmapLoadTask(ImageTileZoomView view, Context context, DecoderFactory<? extends ImageDecoder> decoderFactory, Uri source, boolean preview) {
        this.viewRef = new WeakReference<>(view);
        this.contextRef = new WeakReference<>(context);
        this.decoderFactoryRef = new WeakReference<>(decoderFactory);
        this.source = source;
        this.preview = preview;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try {
            String sourceUri = source.toString();
            Context context = contextRef.get();
            DecoderFactory<? extends ImageDecoder> decoderFactory = decoderFactoryRef.get();
            ImageTileZoomView view = viewRef.get();
            if (context != null && decoderFactory != null && view != null) {
                view.debug("BitmapLoadTask.doInBackground");
                bitmap = decoderFactory.make().decode(context, source);
                return view.getExifOrientation(context, sourceUri);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load bitmap", e);
            this.exception = e;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Failed to load bitmap - OutOfMemoryError", e);
            this.exception = new RuntimeException(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Integer orientation) {
        ImageTileZoomView imageTileZoomView = viewRef.get();
        if (imageTileZoomView != null) {
            if (bitmap != null && orientation != null) {
                if (preview) {
                    imageTileZoomView.onPreviewLoaded(bitmap);
                } else {
                    imageTileZoomView.onImageLoaded(bitmap, orientation, false);
                }
            } else if (exception != null && imageTileZoomView.onImageEventListener != null) {
                if (preview) {
                    imageTileZoomView.onImageEventListener.onPreviewLoadError(exception);
                } else {
                    imageTileZoomView.onImageEventListener.onImageLoadError(exception);
                }
            }
        }
    }
}
