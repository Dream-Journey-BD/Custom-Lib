package com.dreamjourney.imagetilezoom.tsak;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.dreamjourney.imagetilezoom.ImageTileZoomView;
import com.dreamjourney.imagetilezoom.decoder.DecoderFactory;
import com.dreamjourney.imagetilezoom.decoder.ImageDecoder;

import java.lang.ref.WeakReference;

/**
 * Runnable used to load bitmap without blocking the UI thread.
 */
public class BitmapLoadTask implements Runnable {
    private final WeakReference<DecoderFactory<? extends ImageDecoder>> decoderFactoryRef;
    private static final String TAG = "BitmapLoadTask_LOG";
    private final WeakReference<ImageTileZoomView> viewRef;
    private final WeakReference<Context> contextRef;
    private final Uri source;
    private final boolean preview;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public BitmapLoadTask(
            ImageTileZoomView view, Context context,
            DecoderFactory<? extends ImageDecoder> decoderFactory,
            Uri source, boolean preview
    ) {
        this.viewRef = new WeakReference<>(view);
        this.contextRef = new WeakReference<>(context);
        this.decoderFactoryRef = new WeakReference<>(decoderFactory);
        this.source = source;
        this.preview = preview;
    }

    @Override
    public void run() {
        // Same Like (doInBackground)
        Bitmap bitmap = null;
        Integer orientation = null;
        Exception exception = null;

        try {
            Context context = contextRef.get();
            DecoderFactory<? extends ImageDecoder> decoderFactory = decoderFactoryRef.get();
            ImageTileZoomView view = viewRef.get();

            if (context != null && decoderFactory != null && view != null) {
                view.debug("BitmapLoadTask.run (Background)");
                bitmap = decoderFactory.make().decode(context, source);
                orientation = view.getExifOrientation(context, source.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load bitmap", e);
            exception = e;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Failed to load bitmap - OutOfMemoryError", e);
            exception = new RuntimeException(e);
        }

        // ফলাফল মেইন থ্রেডে (UI Thread) পাঠানোর জন্য Handler ব্যবহার করা হচ্ছে
        final Bitmap finalBitmap = bitmap;
        final Integer finalOrientation = orientation;
        final Exception finalException = exception;

        mainHandler.post(() -> {
            // এই অংশটি মেইন থ্রেডে চলবে (onPostExecute এর মতো)
            ImageTileZoomView view = viewRef.get();
            if (view != null) {
                if (finalBitmap != null && finalOrientation != null) {
                    if (preview) {
                        view.onPreviewLoaded(finalBitmap);
                    } else {
                        view.onImageLoaded(finalBitmap, finalOrientation, false);
                    }
                } else if (finalException != null && view.onImageEventListener != null) {
                    if (preview) {
                        view.onImageEventListener.onPreviewLoadError(finalException);
                    } else {
                        view.onImageEventListener.onImageLoadError(finalException);
                    }
                }
            }
        });
    }
}