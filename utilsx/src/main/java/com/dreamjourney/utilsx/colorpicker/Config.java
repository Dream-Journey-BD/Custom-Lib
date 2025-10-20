package com.dreamjourney.utilsx.colorpicker;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.dreamjourney.utilsx.R;

public class Config {

    private static final String TAG = "Config_LOG";

    public abstract static class ColorListener {
        public void OnColorChange(int color) {

        }

        public void OnOKButtonClick(int color) {

        }

        public void OnCancelButtonClick() {

        }

    }

    private final int buttonColor;
    private final boolean isDialog;
    private final boolean isAlpha;
    private final boolean showButton;
    private final View rootView;
    private final View newColor;
    private final View colorLineView;
    private final ImageView lineColorSelector;
    private final SquareView squareView;
    private final ImageView squareColorSelector;
    private final ImageView alphaCursor;
    private final ImageView alphaView;
    private final View alphaOverlay;
    private final ViewGroup mainLayout;
    public ColorListener listener;
    public final TextView okButton;
    public final TextView cancelButton;
    public final CardView mainCard;
    public final LinearLayout buttonLayout;

    public Config(@NonNull View rootView, boolean enableAlpha,
                  boolean isDialog, @ColorInt int buttonColor,
                  boolean showButton, int initColor, ColorListener listener) {
        this.rootView = rootView;
        this.isAlpha = enableAlpha;
        this.isDialog = isDialog;
        this.showButton = showButton;
        this.listener = listener;
        this.buttonColor = buttonColor;

        colorLineView = rootView.findViewById(R.id.colorView);
        squareView = rootView.findViewById(R.id.squareView);
        lineColorSelector = rootView.findViewById(R.id.colorSelector);
        newColor = rootView.findViewById(R.id.newColor);
        squareColorSelector = rootView.findViewById(R.id.squareColorSelector);
        mainLayout = rootView.findViewById(R.id.mainRL);
        alphaOverlay = rootView.findViewById(R.id.alphaOverlay);
        alphaCursor = rootView.findViewById(R.id.alphaSelector);
        alphaView = rootView.findViewById(R.id.alphaView);
        okButton = rootView.findViewById(R.id.okButton);
        cancelButton = rootView.findViewById(R.id.cancel_button);
        mainCard = rootView.findViewById(R.id.carView);
        buttonLayout = rootView.findViewById(R.id.buttonLay);

        initLayout(initColor);

    }

    private void initLayout(int initColor) {
        //Store Init Color
        if (!isAlpha) initColor = initColor | 0xff000000;
        Color.colorToHSV(initColor, Const.hsvColor);
        Const.setAlphaValue(Color.alpha(initColor));

        // Set Start Color
        squareView.setSquareColor(Const.getSquareColor());
        //oldColor.setBackgroundColor(initColor);
        newColor.setBackgroundColor(initColor);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        moveColorCursor();
                        moveSquareCursor();
                        if (isAlpha) {
                            moveAlphaCursor();
                            updateAlphaColor();
                        } else {
                            alphaView.setVisibility(View.GONE);
                            alphaOverlay.setVisibility(View.GONE);
                            alphaCursor.setVisibility(View.GONE);
                        }
                    }
                }
        );

        cancelButton.setTextColor(buttonColor);
        okButton.setTextColor(buttonColor);

        if (isDialog) {
            mainCard.setRadius(20f);
            mainCard.setElevation(20f);
        } else mainCard.setCardBackgroundColor(Color.TRANSPARENT);

        if (showButton) buttonLayout.setVisibility(View.VISIBLE);

        okButton.setOnClickListener(v -> {
            if (listener != null) listener.OnOKButtonClick(Const.getColor());
        });
        cancelButton.setOnClickListener(v -> {
            if (listener != null) listener.OnCancelButtonClick();
        });

        TouchControl();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void TouchControl() {

        colorLineView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE
                    || event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_UP) {

                float y = event.getY();
                if (y < 0.f) y = 0.f;

                if (y > colorLineView.getMeasuredHeight()) {
                    // to avoid jumping the cursor from bottom to top.
                    y = colorLineView.getMeasuredHeight() - 0.001f;
                }
                float hue = 360.f - 360.f / colorLineView.getMeasuredHeight() * y;
                if (hue == 360.f) hue = 0.f;
                Const.setHValue(hue);

                // update view
                squareView.setSquareColor(Const.getSquareColor());
                moveColorCursor();
                newColor.setBackgroundColor(Const.getColor());
                if (listener != null) {
                    listener.OnColorChange(Const.getColor());
                }
                updateAlphaColor();
                return true;
            }
            return false;
        });

        squareView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE
                    || event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_UP) {

                // touch event are in dp units.
                float x = event.getX();
                float y = event.getY();

                if (x < 0.f) x = 0.f;
                if (x > squareView.getMeasuredWidth()) x = squareView.getMeasuredWidth();
                if (y < 0.f) y = 0.f;
                if (y > squareView.getMeasuredHeight()) y = squareView.getMeasuredHeight();

                Const.setSValue(1.f / squareView.getMeasuredWidth() * x);
                Const.setBvValue(1.f - (1.f / squareView.getMeasuredHeight() * y));

                // update view
                moveSquareCursor();

                newColor.setBackgroundColor(Const.getColor());
                if (listener != null) {
                    listener.OnColorChange(Const.getColor());
                }
                updateAlphaColor();
                return true;
            }
            return false;
        });

        if (isAlpha) alphaView.setOnTouchListener((v, event) -> {
            if ((event.getAction() == MotionEvent.ACTION_MOVE)
                    || (event.getAction() == MotionEvent.ACTION_DOWN)
                    || (event.getAction() == MotionEvent.ACTION_UP)) {

                float x = event.getX();
                int mWidth = alphaView.getMeasuredWidth();

                x = Math.max(0, Math.min(x, mWidth));

                int a = Math.round((255.f / mWidth) * x);

                // update Value and view
                Const.setAlphaValue(a);
                moveAlphaCursor();
                int col = Const.getColor();
                int c = a << 24 | col & 0x00ffffff;
                newColor.setBackgroundColor(c);
                if (listener != null) {
                    listener.OnColorChange(Const.getColor());
                }

                return true;
            }
            return false;
        });

    }

    private void moveColorCursor() {

        float y = colorLineView.getMeasuredHeight() -
                (Const.getSquareColor() * colorLineView.getMeasuredHeight() / 360.f);

        if (y == colorLineView.getMeasuredHeight()) y = 0.f;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) lineColorSelector.getLayoutParams();

        layoutParams.leftMargin = (int) (
                colorLineView.getLeft() -
                        Math.floor((double) lineColorSelector.getMeasuredWidth() / 2)
                        - mainLayout.getPaddingLeft()
        );

        layoutParams.topMargin = (int) (
                colorLineView.getTop() + y -
                        Math.floor((double) lineColorSelector.getMeasuredHeight() / 2) -
                        mainLayout.getPaddingTop()
        );

        lineColorSelector.setLayoutParams(layoutParams);

    }

    private void moveSquareCursor() {

        float x = Const.getSValue() * squareView.getMeasuredWidth();
        float y = (1.f - Const.getBvValue()) * squareView.getMeasuredHeight();


        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                squareColorSelector.getLayoutParams();

        layoutParams.leftMargin = (int) (
                squareView.getLeft() + x -
                        Math.floor((double) squareColorSelector.getMeasuredWidth() / 2) -
                        mainLayout.getPaddingLeft()
        );

        layoutParams.topMargin = (int) (
                squareView.getTop() + y -
                        Math.floor((double) squareColorSelector.getMeasuredHeight() / 2) -
                        mainLayout.getPaddingTop()
        );

        squareColorSelector.setLayoutParams(layoutParams);

    }

    private void moveAlphaCursor() {

        final int mWidth = alphaView.getMeasuredWidth();
        float xValue = (Const.getAlphaValue() * mWidth) / 255.f;

        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) alphaCursor.getLayoutParams();

        params.topMargin = alphaView.getTop() - alphaView.getHeight() / 2;

        params.leftMargin = (int) (
                (alphaView.getLeft() + xValue) -
                        (float) alphaCursor.getMeasuredWidth() / 2
        );

        alphaCursor.setLayoutParams(params);

    }

    private void updateAlphaColor() {

        final GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, new int[]{
                Color.HSVToColor(Const.hsvColor), 0x0
        });
        alphaOverlay.setBackground(gd);

    }

}
