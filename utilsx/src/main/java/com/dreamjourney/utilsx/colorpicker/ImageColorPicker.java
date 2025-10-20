package com.dreamjourney.utilsx.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dreamjourney.utilsx.R;


public class ImageColorPicker extends FrameLayout {

    public interface ColorListener {
        void OnColorChange(int color);
    }

    private boolean showFlag = false;
    private static final String TAG = "Test_LOG";
    public ColorListener listener;
    public ImageView mainImageView;
    public ImageView selectorImageView;
    private FlagView flagView;
    private BitmapDrawable imageDrawable;
    private Bitmap imageBitmap;
    private Drawable iconDrawable;

    public ImageColorPicker(@NonNull Context context) {
        super(context);
        InitLIB(null);
    }

    public ImageColorPicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        InitLIB(attrs);
    }

    public ImageColorPicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        InitLIB(attrs);
    }

    public ImageColorPicker(
            @NonNull Context context,
            @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);
        InitLIB(attrs);
    }

    private void InitLIB(AttributeSet attrs) {


        try (TypedArray ta = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.ImageColorPickerAttr,
                0, 0
        )) {

            if (ta.hasValue(R.styleable.ImageColorPickerAttr_setImageSrc))
                imageDrawable = (BitmapDrawable) ta.getDrawable(
                        R.styleable.ImageColorPickerAttr_setImageSrc
                );

            if (ta.hasValue(R.styleable.ImageColorPickerAttr_showFlag))
                showFlag = ta.getBoolean(R.styleable.ImageColorPickerAttr_showFlag, false);

            if (ta.hasValue(R.styleable.ImageColorPickerAttr_setSelectorIcon))
                iconDrawable = ta.getDrawable(R.styleable.ImageColorPickerAttr_setSelectorIcon);

        }


        // Main Image
        mainImageView = new ImageView(getContext());

        LayoutParams imageParam = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        imageParam.gravity = Gravity.CENTER;
        addView(mainImageView, imageParam);

        // Flag Image
        flagView = new FlagView(getContext(), R.layout.custom_flag) {
        };


        LayoutParams flagParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        flagParams.gravity = Gravity.CENTER;
        addView(flagView, flagParams);

        // Selector Image
        selectorImageView = new ImageView(getContext());

        if (iconDrawable == null) {
            selectorImageView.setImageResource(R.drawable.square_cursor);
        } else selectorImageView.setImageDrawable(iconDrawable);

        LayoutParams selectorParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        selectorParams.gravity = Gravity.CENTER;
        addView(selectorImageView, selectorParams);

        if (imageDrawable != null) setImage(imageDrawable);
        // Get Image Data
        TouchControl();

    }

    public void setImage(Drawable image) {
        if (image == null) return;
        mainImageView.setImageDrawable(image);
        // get bitmap From Image
        imageDrawable = (BitmapDrawable) mainImageView.getDrawable();
        if (imageDrawable != null) imageBitmap = imageDrawable.getBitmap();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void TouchControl() {

        mainImageView.setOnTouchListener((v, event) -> {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_DOWN) {
                if (showFlag) flagView.setVisibility(VISIBLE);
                Const.fadeIn(flagView);
                getColorFromBitmap(event);
                return true;
            } else if (action == MotionEvent.ACTION_MOVE) {
                getColorFromBitmap(event);
                return true;

            } else if (action == MotionEvent.ACTION_UP) {
                if (showFlag) flagView.setVisibility(GONE);
                Const.fadeOut(flagView);
                return true;
            }
            return false;
        });

    }

    private void getColorFromBitmap(@NonNull MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        Matrix invertMatrix = new Matrix();
        mainImageView.getImageMatrix().invert(invertMatrix);

        float[] mappedPoints = new float[]{x, y};
        invertMatrix.mapPoints(mappedPoints);

        if (imageDrawable != null && imageBitmap != null) {

            Rect rect = imageDrawable.getBounds();
            int px = (int) (mappedPoints[0] / rect.width() * imageBitmap.getWidth());
            int py = (int) (mappedPoints[1] / rect.height() * imageBitmap.getHeight());

            int bitmapH = imageBitmap.getHeight();
            int bitmapW = imageBitmap.getWidth();

            // Update Flag
            float flagX = x - ((float) flagView.getWidth());
            float flagY = y - ((float) flagView.getHeight() * 2);

            // Move Selector
            float selectorX = x - ((float) selectorImageView.getWidth() / 2);
            float selectorY = y - ((float) selectorImageView.getHeight() / 2);

            // Move Selector
            if (py >= 0 && py < bitmapH) {
                if (flagY < 0) flagY = y + ((float) flagView.getHeight() * 2);
                if (showFlag) flagView.setY(flagY);
                selectorImageView.setY(selectorY);
            }

            if (px >= 0 && px < bitmapW) {
                if (flagX < 0) flagX = (float) selectorImageView.getWidth() / 3;
                if (selectorX < 0) selectorX = 0;
                if (showFlag) flagView.setX(flagX);
                selectorImageView.setX(selectorX);
            }

            // get Color from Pixel
            if (px < 0) px = 0;
            if (py < 0) py = 0;
            if (px >= bitmapW) px = bitmapW - 1;
            if (py >= bitmapH) py = bitmapH - 1;

            int color = imageBitmap.getPixel(px, py);
            if (showFlag) flagView.setDetails(color);
            if (listener != null) listener.OnColorChange(color);

        }

    }

    public void setListener(ColorListener listener) {
        this.listener = listener;
    }

}


