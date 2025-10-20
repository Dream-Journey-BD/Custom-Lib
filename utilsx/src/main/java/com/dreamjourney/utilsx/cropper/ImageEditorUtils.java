package com.dreamjourney.utilsx.cropper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;

public class ImageEditorUtils {


    @NonNull
    public static Bitmap addTextToBitmap(
            @NonNull Bitmap bitmap, @NonNull String text,
            float x, float y, int color, float textSize
    ) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);

        canvas.drawText(text, x, y, paint);

        return mutableBitmap;
    }


}
