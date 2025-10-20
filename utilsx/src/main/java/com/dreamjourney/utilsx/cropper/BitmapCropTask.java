package com.dreamjourney.utilsx.cropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class BitmapCropTask {

    private final WeakReference<CropImageView> cropImageViewReference;
    private final Bitmap bitmap;
    private final Uri uri;
    private final Context context;
    private final float[] cropPoints;
    private final int degreesRotated;
    private final int orgWidth;
    private final int orgHeight;
    private final boolean fixAspectRatio;
    private final int aspectRatioX;
    private final int aspectRatioY;
    private final int reqWidth;
    private final int reqHeight;
    private final boolean flipHorizontally;
    private final boolean flipVertically;
    private final CropImageView.RequestSizeOptions reqSizeOptions;
    private final Uri saveUri;
    private final Bitmap.CompressFormat saveCompressFormat;
    private final int saveCompressQuality;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public BitmapCropTask(
            CropImageView cropImageView, Bitmap bitmap,
            float[] cropPoints, int degreesRotated, boolean fixAspectRatio,
            int aspectRatioX, int aspectRatioY, int reqWidth, int reqHeight,
            boolean flipHorizontally, boolean flipVertically,
            CropImageView.RequestSizeOptions options, Uri saveUri,
            Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality
    ) {

        this.cropImageViewReference = new WeakReference<>(cropImageView);
        this.context = cropImageView.getContext();
        this.bitmap = bitmap;
        this.cropPoints = cropPoints;
        this.uri = null;
        this.degreesRotated = degreesRotated;
        this.fixAspectRatio = fixAspectRatio;
        this.aspectRatioX = aspectRatioX;
        this.aspectRatioY = aspectRatioY;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
        this.flipHorizontally = flipHorizontally;
        this.flipVertically = flipVertically;
        this.reqSizeOptions = options;
        this.saveUri = saveUri;
        this.saveCompressFormat = saveCompressFormat;
        this.saveCompressQuality = saveCompressQuality;
        this.orgWidth = 0;
        this.orgHeight = 0;
    }

    public BitmapCropTask(
            CropImageView cropImageView, Uri uri, float[] cropPoints,
            int degreesRotated, int orgWidth, int orgHeight, boolean fixAspectRatio,
            int aspectRatioX, int aspectRatioY, int reqWidth, int reqHeight,
            boolean flipHorizontally, boolean flipVertically,
            CropImageView.RequestSizeOptions options, Uri saveUri,
            Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality
    ) {

        this.cropImageViewReference = new WeakReference<>(cropImageView);
        this.context = cropImageView.getContext();
        this.uri = uri;
        this.cropPoints = cropPoints;
        this.degreesRotated = degreesRotated;
        this.fixAspectRatio = fixAspectRatio;
        this.aspectRatioX = aspectRatioX;
        this.aspectRatioY = aspectRatioY;
        this.orgWidth = orgWidth;
        this.orgHeight = orgHeight;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
        this.flipHorizontally = flipHorizontally;
        this.flipVertically = flipVertically;
        this.reqSizeOptions = options;
        this.saveUri = saveUri;
        this.saveCompressFormat = saveCompressFormat;
        this.saveCompressQuality = saveCompressQuality;
        this.bitmap = null;
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
            BitmapUtils.BitmapSampled bitmapSampled;

            if (uri != null) {
                bitmapSampled = BitmapUtils.cropBitmap(
                        context,
                        uri,
                        cropPoints,
                        degreesRotated,
                        orgWidth,
                        orgHeight,
                        fixAspectRatio,
                        aspectRatioX,
                        aspectRatioY,
                        reqWidth,
                        reqHeight,
                        flipHorizontally,
                        flipVertically
                );
            } else if (bitmap != null) {
                bitmapSampled = BitmapUtils.cropBitmapObjectHandleOOM(
                        bitmap,
                        cropPoints,
                        degreesRotated,
                        fixAspectRatio,
                        aspectRatioX,
                        aspectRatioY,
                        flipHorizontally,
                        flipVertically
                );
            } else {
                return new Result((Bitmap) null, 1);
            }

            Bitmap resized = BitmapUtils.resizeBitmap(bitmapSampled.bitmap, reqWidth, reqHeight, reqSizeOptions);

            if (saveUri == null) {
                return new Result(resized, bitmapSampled.sampleSize);
            } else {
                BitmapUtils.writeBitmapToUri(context, resized, saveUri, saveCompressFormat, saveCompressQuality);
                resized.recycle();
                return new Result(saveUri, bitmapSampled.sampleSize);
            }
        } catch (Exception e) {
            return new Result(e, saveUri != null);
        }
    }

    private void onPostExecute(Result result) {
        if (result != null) {
            boolean completeCalled = false;
            CropImageView cropImageView = cropImageViewReference.get();
            if (cropImageView != null) {
                completeCalled = true;
                cropImageView.onImageCroppingAsyncComplete(result);
            }
            if (!completeCalled && result.bitmap != null) {
                result.bitmap.recycle();
            }
        }
    }

    public static final class Result {
        public final Bitmap bitmap;
        public final Uri uri;
        final Exception error;
        final boolean isSave;
        final int sampleSize;

        Result(Bitmap bitmap, int sampleSize) {
            this.bitmap = bitmap;
            this.uri = null;
            this.error = null;
            this.isSave = false;
            this.sampleSize = sampleSize;
        }

        Result(Uri uri, int sampleSize) {
            this.bitmap = null;
            this.uri = uri;
            this.error = null;
            this.isSave = true;
            this.sampleSize = sampleSize;
        }

        Result(Exception error, boolean isSave) {
            this.bitmap = null;
            this.uri = null;
            this.error = error;
            this.isSave = isSave;
            this.sampleSize = 1;
        }
    }

}
