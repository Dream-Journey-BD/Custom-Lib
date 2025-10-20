package com.dreamjourney.utilsx.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dreamjourney.utilsx.R;
import com.google.android.material.appbar.MaterialToolbar;

public class CustomToolBar extends MaterialToolbar {
    private static final String TAG = "CustomTB_LOG";

    public CustomToolBar(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public CustomToolBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, androidx.appcompat.R.attr.toolbarStyle);
        init(context, attrs);
    }

    public CustomToolBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {

        try (TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.CustomToolBar,
                0, 0
        )) {
            // title size
            int titleSize = a.getDimensionPixelSize(
                    R.styleable.CustomToolBar_titleTextSize, 0
            );
            if (titleSize > 0) setTitleTextSize(titleSize);

            // title font style
            int fontStyle = a.getInt(
                    R.styleable.CustomToolBar_titleFontStyle, 0
            );
            setTitleFontStyle(fontStyle);

            // overflow icon drawable
            Drawable overflowIcon = a.getDrawable(
                    R.styleable.CustomToolBar_overflowMenuIcon
            );
            if (overflowIcon != null) setOverflowIcon(overflowIcon);

            // overflow icon tint
            int overflowTint = a.getColor(
                    R.styleable.CustomToolBar_overflowMenuIconTint, 0
            );
            if (overflowTint != 0) setOverflowIconTint(overflowTint);

        }
    }

    // ---------------- Title ----------------
    public void setTitleTextSize(int px) {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof android.widget.TextView) {
                ((android.widget.TextView)
                        getChildAt(i)).setTextSize(
                        TypedValue.COMPLEX_UNIT_PX, px
                );
                break;
            }
        }
    }

    public void setTitleFontStyle(int typeface) {
        Typeface font = Typeface.defaultFromStyle(typeface);
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof android.widget.TextView) {
                ((android.widget.TextView) getChildAt(i)).setTypeface(font);
                break;
            }

        }
    }

    // ---------------- Overflow Icon ----------------
    public void setOverflowIconTint(int color) {
        Drawable icon = getOverflowIcon();
        if (icon != null) icon.setTint(color);
    }

}
