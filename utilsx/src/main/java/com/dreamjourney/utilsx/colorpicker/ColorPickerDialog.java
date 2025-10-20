package com.dreamjourney.utilsx.colorpicker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.dreamjourney.utilsx.R;

public class ColorPickerDialog extends Dialog {
    private int btnTextColor = Color.MAGENTA;


    private boolean showButton = true;
    private final int color;
    private final boolean alpha;
    private final Config.ColorListener listener;
    private Config config;


    public ColorPickerDialog(
            @NonNull Context context, @ColorInt int initColor,
            boolean enableAlpha, Config.ColorListener listener
    ) {
        super(context);
        this.color = initColor;
        this.alpha = enableAlpha;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate layout
        @SuppressLint("InflateParams")
        View root = LayoutInflater.from(getContext())
                .inflate(R.layout.color_picker_dialog, null);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.empty_rect);
        }

        // setup config
        config = new Config(
                root, alpha,
                true,
                btnTextColor,
                showButton,
                color,
                listener
        );

        // set view
        setContentView(root);
    }

    public Config getConfig() {
        return config;
    }

    public void setButtonTextColor(@ColorInt int color) {
        this.btnTextColor = color;
    }

    public void setShowButton(boolean showButton) {
        this.showButton = showButton;
    }


}
