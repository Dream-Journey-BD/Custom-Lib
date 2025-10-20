
package com.dreamjourney.utilsx.colorpicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dreamjourney.utilsx.R;


public abstract class FlagView extends RelativeLayout {
    private View view;
    private TextView textView;

    public FlagView(Context context, int layout) {
        super(context);
        initializeLayout(layout);
    }

    private void initializeLayout(int layout) {
        View inflated = LayoutInflater.from(getContext()).inflate(layout, this);
        inflated.setLayoutParams(
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        inflated.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        inflated.layout(0, 0, inflated.getWidth(), inflated.getMeasuredHeight());

        textView = findViewById(R.id.colorCode);
        view = findViewById(R.id.preView);

        setVisibility(GONE);

    }

    public void setDetails(int Color) {
        view.setBackgroundColor(Color);
        textView.setText(String.format("#%06X", (0xFFFFFF & Color)));
    }

}
