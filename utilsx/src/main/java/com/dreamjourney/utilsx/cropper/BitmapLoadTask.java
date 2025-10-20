package com.dreamjourney.utilsx.cropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class BitmapLoadTask {

    private final WeakReference<CropImageView> cropImageViewReference;
    private final Uri uri;
    private final Context context;
    private final int width;
    private final int height;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public BitmapLoadTask(CropImageView cropImageView, Uri uri) {
        this.uri = uri;
        this.cropImageViewReference = new WeakReference<>(cropImageView);
        this.context = cropImageView.getContext();

        DisplayMetrics metrics = cropImageView.getResources().getDisplayMetrics();
        double densityAdj = metrics.density > 1 ? 1 / metrics.density : 1;
        this.width = (int) (metrics.widthPixels * densityAdj);
        this.height = (int) (metrics.heightPixels * densityAdj);
    }

    public Uri getUri() {
        return uri;
    }

    public void execute() {
        executor.execute(() -> {
            Result result = doInBackground();
            mainHandler.post(() -> onPostExecute(result));
        });
    }

    @NonNull
    private Result doInBackground() {
        try {
            BitmapUtils.BitmapSampled decodeResult
                    = BitmapUtils.decodeSampledBitmap(
                    context, uri, width, height
            );

            BitmapUtils.RotateBitmapResult rotateResult
                    = BitmapUtils.rotateBitmapByExif(
                    decodeResult.bitmap, context, uri
            );

            return new Result(
                    uri, rotateResult.bitmap,
                    decodeResult.sampleSize, rotateResult.degrees
            );

        } catch (Exception e) {
            return new Result(uri, e);
        }
    }

    private void onPostExecute(Result result) {
        if (result != null) {
            boolean completeCalled = false;
            CropImageView cropImageView = cropImageViewReference.get();
            if (cropImageView != null) {
                completeCalled = true;
                cropImageView.onSetImageUriAsyncComplete(result);
            }
            if (!completeCalled && result.bitmap != null) {
                result.bitmap.recycle();
            }
        }
    }

    public static final class Result {
        public final Uri uri;
        public final Bitmap bitmap;
        public final int loadSampleSize;
        public final int degreesRotated;
        public final Exception error;

        Result(Uri uri, Bitmap bitmap, int loadSampleSize, int degreesRotated) {
            this.uri = uri;
            this.bitmap = bitmap;
            this.loadSampleSize = loadSampleSize;
            this.degreesRotated = degreesRotated;
            this.error = null;
        }

        Result(Uri uri, Exception error) {
            this.uri = uri;
            this.bitmap = null;
            this.loadSampleSize = 0;
            this.degreesRotated = 0;
            this.error = error;
        }
    }

}
