package com.dreamjourney.utilsx.wheelview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.OverScroller;

import androidx.annotation.NonNull;

import com.dreamjourney.utilsx.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WheelView extends View implements GestureDetector.OnGestureListener {

    // Default Variable Here
    public static final float DEFAULT_SPACING_FACTOR = 1.5f;
    public static final float DEFAULT_LINE_RATIO = 0.7f;
    public static final String DEFAULT_SYMBOL = "";
    public static final int CENTER_TEXT_SIZE_SP = 40;
    public static final int NORMAL_TEXT_SIZE_SP = 27;
    public static final int SMALL_UNIT_TEXT_SIZE_SP = 16;
    public static final int TINY_UNIT_TEXT_SIZE_SP = 14;

    // Paint & Drawing Related Variables Here
    private Paint linePaint;
    private TextPaint textPaint;
    private int normalLineColor;
    private int centerLineColor;
    private int normalTextColor;
    private int centerTextColor;
    private int affixColor;
    private int cursorColor;
    private final Path centerLinePath = new Path();

    // Dimension & Size Related Variables Here
    private int viewHeight;
    private float centerTextSize;
    private float normalTextSize;
    private float topPadding;
    private float bottomPadding;
    private float lineSpacingFactor = DEFAULT_SPACING_FACTOR;
    private float lineHeightRatio = DEFAULT_LINE_RATIO;
    private float markSpacing;
    private float normalLineWidth;
    private float extraTextWidth;
    private float cursorSize;

    // Data & Item Related Variables Here
    private List<String> itemList;
    private String suffixText;
    private String prefixText;
    private int selectedIndex = -1;
    private int previousSelectedIndex = -1;
    private int totalLineCount;
    private int visibleItemRange;

    // Scroll Control & Touch Handling Variables Here
    private OverScroller scroller;
    private float maxScrollLimit;
    private RectF contentArea;
    private boolean isFlinging = false;
    private GestureDetector touchDetector;

    // Selection & Limit Control Variables Here
    private boolean isShowLine = true;
    private int minSelectableItem = Integer.MIN_VALUE;
    private int maxSelectableItem = Integer.MAX_VALUE;

    // Rupee & Symbol Text Size Variables Here
    private int smallUnitTextSize;

    // Listener & Callback Here
    private OnWheelItemSelectedListener listener;

    public WheelView(Context context) {
        super(context);
        init(null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    protected void init(AttributeSet attrs) {
        float density = getResources().getDisplayMetrics().density;
        float centerLineWidth = 0;
        normalLineWidth = 0;
        prefixText = DEFAULT_SYMBOL;

        centerTextColor = 0xFFFFFFFF;
        normalTextColor = 0x80666666;
        normalLineColor = 0xFFEEEEEE;
        affixColor = normalTextColor;
        centerLineColor = centerTextColor;
        cursorColor = centerTextColor;
        cursorSize = 0;
        centerTextSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, CENTER_TEXT_SIZE_SP,
                getResources().getDisplayMetrics()
        );

        normalTextSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, NORMAL_TEXT_SIZE_SP,
                getResources().getDisplayMetrics()
        );

        smallUnitTextSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, SMALL_UNIT_TEXT_SIZE_SP,
                getResources().getDisplayMetrics()
        );

        Rect textBoundsRect = new Rect();

        bottomPadding = density * 6;


        try (TypedArray ta = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.WheelView, 0, 0)
        ) {
            centerTextColor = ta.getColor(R.styleable.WheelView_wvCenterTextColor, centerTextColor);
            normalTextColor = ta.getColor(R.styleable.WheelView_wvNormalTextColor, normalTextColor);
            normalLineColor = ta.getColor(R.styleable.WheelView_wvNormalLineColor, normalLineColor);
            centerLineColor = ta.getColor(R.styleable.WheelView_wvCenterLineColor, centerTextColor);
            lineSpacingFactor = ta.getFloat(R.styleable.WheelView_wvLineSpaceFactor, lineSpacingFactor);
            lineHeightRatio = ta.getFloat(R.styleable.WheelView_wvLineHeightRatio, lineHeightRatio);
            centerTextSize = ta.getDimension(R.styleable.WheelView_wvCenterTextSize, centerTextSize);
            normalTextSize = ta.getDimension(R.styleable.WheelView_wvNormalTextSize, normalTextSize);
            cursorColor = ta.getColor(R.styleable.WheelView_wvCursorColor, centerTextColor);
            cursorSize = ta.getDimension(R.styleable.WheelView_wvCursorSize, cursorSize);
            affixColor = ta.getColor(R.styleable.WheelView_wvAffixColorColor, centerTextColor);
            isShowLine = ta.getBoolean(R.styleable.WheelView_wvShowLine, isShowLine);
            suffixText = ta.getString(R.styleable.WheelView_wvSuffixText);
            if (suffixText == null) suffixText = "";
            prefixText = ta.getString(R.styleable.WheelView_wvPrefixText);
            if (prefixText == null) prefixText = "";

        }

        if (prefixText.isBlank() || prefixText.isEmpty()) {
            prefixText = DEFAULT_SYMBOL;
        }
        int fadeLineColor = centerTextColor & 0xAAFFFFFF;
        lineSpacingFactor = Math.max(1, lineSpacingFactor);
        lineHeightRatio = Math.min(1, lineHeightRatio);
        if (isShowLine) topPadding = cursorSize + density * 2;
        else topPadding = cursorSize / 2;

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(centerTextColor);

        linePaint.setColor(normalLineColor);
        linePaint.setStrokeWidth(centerLineWidth);

        textPaint.setTextSize(centerTextSize);
        calcIntervalDis();

        scroller = new OverScroller(getContext());
        contentArea = new RectF();

        touchDetector = new GestureDetector(getContext(), this);
        selectIndex(0);
    }

    private void calcIntervalDis() {

        if (textPaint == null) return;
        Rect temp = new Rect();
        float maxWidth = 0f;

        List<String> list;
        if (itemList == null || itemList.isEmpty()) {
            list = Collections.singletonList("888888");
        } else list = itemList;

        for (String s : list) {
            textPaint.getTextBounds(s, 0, s.length(), temp);
            maxWidth = Math.max(maxWidth, temp.width());
        }

        // suffix or prefix
        String affix = !TextUtils.isEmpty(suffixText) ? suffixText : prefixText;
        textPaint.setTextSize(normalTextSize);
        textPaint.getTextBounds(affix, 0, affix.length(), temp);
        extraTextWidth = temp.width();

        // final interval
        markSpacing = (maxWidth + extraTextWidth) * lineSpacingFactor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureSize = MeasureSpec.getSize(widthMeasureSpec);
        int result = getSuggestedMinimumWidth();
        switch (measureMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = measureSize;
                break;
            default:
                break;
        }
        return result;
    }

    private int measureHeight(int heightMeasure) {
        int measureMode = MeasureSpec.getMode(heightMeasure);
        int measureSize = MeasureSpec.getSize(heightMeasure);
        int result = (int) (bottomPadding + topPadding * 2 + centerTextSize);
        switch (measureMode) {
            case MeasureSpec.EXACTLY:
                result = Math.max(result, measureSize);
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, measureSize);
                break;
            default:
                break;
        }
        return result;
    }

    public void fling(int velocityX, int velocityY) {
        scroller.fling(getScrollX(), getScrollY(),
                velocityX, velocityY,
                (int) (-maxScrollLimit + minSelectableItem * markSpacing),
                (int) (contentArea.width() - maxScrollLimit
                        - (totalLineCount - 1 - maxSelectableItem) * markSpacing
                ),
                0, 0,
                (int) maxScrollLimit, 0);
        postInvalidateOnAnimation();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        if (w != oldW || h != oldH) {
            viewHeight = h;
            maxScrollLimit = w / 2.f;
            contentArea.set(0, 0, (totalLineCount - 1) * markSpacing, h);
            visibleItemRange = (int) Math.ceil(maxScrollLimit / markSpacing);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        centerLinePath.reset();
        float sizeDiv2 = cursorSize / 2f;
        float sizeDiv3 = cursorSize / 3f;
        centerLinePath.moveTo(maxScrollLimit - sizeDiv2 + getScrollX(), 0);
        centerLinePath.rLineTo(0, sizeDiv3);
        centerLinePath.rLineTo(sizeDiv2, sizeDiv2);
        centerLinePath.rLineTo(sizeDiv2, -sizeDiv2);
        centerLinePath.rLineTo(0, -sizeDiv3);
        centerLinePath.close();

        linePaint.setColor(cursorColor);
        canvas.drawPath(centerLinePath, linePaint);

        int start = selectedIndex - visibleItemRange;
        int end = selectedIndex + visibleItemRange + 1;

        start = Math.max(start, -visibleItemRange * 2);
        end = Math.min(end, totalLineCount + visibleItemRange * 2);

        // extends both ends
        if (selectedIndex == maxSelectableItem) {
            end += visibleItemRange;
        } else if (selectedIndex == minSelectableItem) {
            start -= visibleItemRange;
        }

        float x = start * markSpacing;

        float markHeight = viewHeight - bottomPadding - centerTextSize - topPadding;

        // small scale Y offset
        float smallMarkShrinkY = markHeight * (1 - lineHeightRatio) / 2f;
        smallMarkShrinkY = Math.min((markHeight - normalLineWidth) / 2f, smallMarkShrinkY);

        for (int i = start; i < end; i++) {

            // draw mark text
            if (totalLineCount > 0 && i >= 0 && i < totalLineCount) {
                CharSequence text = itemList.get(i);
                boolean isSelected = (selectedIndex == i);
                float baseY = viewHeight - bottomPadding;
                float off = extraTextWidth / 2f;

                // draw line
                if (isShowLine) {
                    linePaint.setColor(isSelected ? centerLineColor : normalLineColor);
                    linePaint.setStrokeWidth(isSelected ? 5f : normalLineWidth);
                    canvas.drawLine(x, topPadding, x, topPadding + markHeight, linePaint);
                }

                if (isSelected) {
                    textPaint.setColor(centerTextColor);
                    float tWidth = textPaint.measureText(text, 0, text.length());

                    if (!TextUtils.isEmpty(suffixText)) {
                        textPaint.setTextSize(centerTextSize);
                        canvas.drawText(text, 0, text.length(), x - off, baseY, textPaint);

                        textPaint.setTextSize(normalTextSize);
                        textPaint.setColor(affixColor);
                        canvas.drawText(suffixText, x + off + tWidth / 2f, baseY, textPaint);
                    } else if (!TextUtils.isEmpty(prefixText)) {
                        textPaint.setTextSize(centerTextSize);
                        canvas.drawText(text, 0, text.length(), x + off, baseY, textPaint);

                        textPaint.setTextSize(smallUnitTextSize);
                        textPaint.setColor(affixColor);
                        canvas.drawText(prefixText, x - off - tWidth / 2f, baseY, textPaint);
                    } else {
                        textPaint.setTextSize(centerTextSize);
                        canvas.drawText(text, 0, text.length(), x, baseY, textPaint);
                    }
                } else {
                    textPaint.setColor(normalTextColor);
                    textPaint.setTextSize(normalTextSize);
                    canvas.drawText(text, 0, text.length(), x, baseY, textPaint);
                }
            }

            x += markSpacing;
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (itemList == null || itemList.isEmpty() || !isEnabled()) {
            return false;
        }
        boolean ret = touchDetector.onTouchEvent(event);
        if (!isFlinging && MotionEvent.ACTION_UP == event.getAction()) {
            autoSettle();
            ret = true;
        }
        return ret || super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            refreshCenter();
            invalidate();
        } else {
            if (isFlinging) {
                isFlinging = false;
                autoSettle();
            }
        }
    }

    public void setAdditionCenterMark(String additionCenterMark) {
        suffixText = additionCenterMark;
        calcIntervalDis();
        invalidate();
    }

    private void autoSettle() {
        int sx = getScrollX();
        float dx = selectedIndex * markSpacing - sx - maxScrollLimit;
        scroller.startScroll(sx, 0, (int) dx, 0);
        postInvalidate();
        if (previousSelectedIndex != selectedIndex) {
            previousSelectedIndex = selectedIndex;
            if (null != listener) {
                listener.onWheelItemSelected(this, selectedIndex);
            }
        }
    }

    private int safeCenter(int center) {
        if (center < minSelectableItem) {
            center = minSelectableItem;
        } else if (center > maxSelectableItem) {
            center = maxSelectableItem;
        }
        return center;
    }

    private void refreshCenter(int offsetX) {
        int offset = (int) (offsetX + maxScrollLimit);
        int tempIndex = Math.round(offset / markSpacing);
        tempIndex = safeCenter(tempIndex);
        if (selectedIndex == tempIndex) {
            return;
        }
        selectedIndex = tempIndex;
        if (null != listener) {
            listener.onWheelItemChanged(this, selectedIndex);
        }
    }

    private void refreshCenter() {
        refreshCenter(getScrollX());
    }

    public void selectIndex(int index) {
        selectedIndex = index;
        post(new Runnable() {
            @Override
            public void run() {
                scrollTo((int) (selectedIndex * markSpacing - maxScrollLimit), 0);
                invalidate();
                refreshCenter();
            }
        });
    }

    public void smoothSelectIndex(int index) {
        if (!scroller.isFinished()) {
            scroller.abortAnimation();
        }
        int deltaIndex = index - selectedIndex;
        scroller.startScroll(getScrollX(), 0, (int) (deltaIndex * markSpacing), 0);
        invalidate();
    }

    public int getMinSelectableIndex() {
        return minSelectableItem;
    }

    public void setMinSelectableIndex(int minSelectableIndex) {
        if (minSelectableIndex > maxSelectableItem) {
            minSelectableIndex = maxSelectableItem;
        }
        minSelectableItem = minSelectableIndex;
        int afterCenter = safeCenter(selectedIndex);
        if (afterCenter != selectedIndex) {
            selectIndex(afterCenter);
        }
    }

    public int getMaxSelectableIndex() {
        return maxSelectableItem;
    }

    public void setMaxSelectableIndex(int maxSelectableIndex) {
        if (maxSelectableIndex < minSelectableItem) {
            maxSelectableIndex = minSelectableItem;
        }
        maxSelectableItem = maxSelectableIndex;
        int afterCenter = safeCenter(selectedIndex);
        if (afterCenter != selectedIndex) {
            selectIndex(afterCenter);
        }
    }

    public List<String> getItems() {
        return itemList;
    }

    public void setItems(List<String> items) {
        if (itemList == null) {
            itemList = new ArrayList<>();
        } else {
            itemList.clear();
        }
        itemList.addAll(items);
        totalLineCount = null == itemList ? 0 : itemList.size();
        if (totalLineCount > 0) {
            minSelectableItem = Math.max(minSelectableItem, 0);
            maxSelectableItem = Math.min(maxSelectableItem, totalLineCount - 1);
        }
        contentArea.set(0, 0, (totalLineCount - 1) * markSpacing, getMeasuredHeight());
        selectedIndex = Math.min(selectedIndex, totalLineCount);
        calcIntervalDis();
        invalidate();
    }

    public int getSelectedPosition() {
        return selectedIndex;
    }

    public void setOnWheelItemSelectedListener(OnWheelItemSelectedListener onWheelItemSelectedListener) {
        listener = onWheelItemSelectedListener;
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        if (!scroller.isFinished()) {
            scroller.forceFinished(false);
        }
        isFlinging = false;
        if (null != getParent()) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return true;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        playSoundEffect(SoundEffectConstants.CLICK);
        refreshCenter((int) (getScrollX() + e.getX() - maxScrollLimit));
        autoSettle();
        return true;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onScroll(
            MotionEvent e1, @NonNull MotionEvent e2,
            float distanceX, float distanceY
    ) {
        float dis = distanceX;
        float scrollX = getScrollX();
        if (scrollX < minSelectableItem * markSpacing - 2 * maxScrollLimit) {
            dis = 0;
        } else if (scrollX < minSelectableItem * markSpacing - maxScrollLimit) {
            dis = distanceX / 4.f;
        } else if (
                scrollX > contentArea.width() -
                        (totalLineCount - maxSelectableItem - 1) * markSpacing
        ) {
            dis = 0;
        } else if (scrollX >
                contentArea.width() - (totalLineCount - maxSelectableItem - 1)
                        * markSpacing
                        - maxScrollLimit
        ) {
            dis = distanceX / 4.f;
        }
        scrollBy((int) dis, 0);
        refreshCenter();
        return true;
    }

    @Override
    public boolean onFling(
            MotionEvent e1, @NonNull MotionEvent e2,
            float velocityX, float velocityY
    ) {
        float scrollX = getScrollX();
        if (scrollX <
                -maxScrollLimit + minSelectableItem * markSpacing
                || scrollX > contentArea.width() - maxScrollLimit -
                (totalLineCount - 1 - maxSelectableItem) * markSpacing
        ) return false;
        else {
            isFlinging = true;
            fling((int) -velocityX, 0);
            return true;
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.index = getSelectedPosition();
        ss.min = minSelectableItem;
        ss.max = maxSelectableItem;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        minSelectableItem = ss.min;
        maxSelectableItem = ss.max;
        selectIndex(ss.index);
        requestLayout();
    }

    public interface OnWheelItemSelectedListener {
        void onWheelItemChanged(WheelView wheelView, int position);

        void onWheelItemSelected(WheelView wheelView, int position);
    }

    static class SavedState extends BaseSavedState {

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            @NonNull
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @NonNull
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        int index;
        int min;
        int max;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            index = in.readInt();
            min = in.readInt();
            max = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(index);
            out.writeInt(min);
            out.writeInt(max);
        }

        @NonNull
        @Override
        public String toString() {
            return "WheelView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " index=" + index + " min=" + min + " max=" + max + "}";
        }

    }

}
