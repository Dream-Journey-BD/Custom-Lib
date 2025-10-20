package com.dreamjourney.utilsx.colorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.dreamjourney.utilsx.R;

public class ColorPickerView extends RelativeLayout {

    private final Config config;

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Default Value Here
        int initColor = Color.RED;
        int buttonColor = Color.MAGENTA;
        boolean enableAlpha = false;
        boolean showButton = false;

        LayoutInflater.from(context).inflate(
                R.layout.color_picker_dialog, this, true
        );


        try (TypedArray ta = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.ColorPickerAttr,
                0, 0
        )) {
            initColor = ta.getColor(R.styleable.ColorPickerAttr_setInitialColor, Color.RED);
            buttonColor = ta.getColor(R.styleable.ColorPickerAttr_setButtonTextColor, Color.MAGENTA);
            enableAlpha = ta.getBoolean(R.styleable.ColorPickerAttr_enableAlpha, false);
            showButton = ta.getBoolean(R.styleable.ColorPickerAttr_showButton, false);
        }


        config = new Config(
                this,
                enableAlpha,
                false,
                buttonColor,
                showButton,
                initColor,
                null
        );

    }

    public void setColorListener(Config.ColorListener listener) {
        config.listener = listener;
    }
}