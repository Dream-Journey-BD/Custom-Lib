package com.dreamjourney.utilsx.signaturepad.utils;

import androidx.annotation.NonNull;

public class SvgPathBuilder {

    public static final Character SVG_RELATIVE_CUBIC_BEZIER_CURVE = 'c';
    public static final Character SVG_MOVE = 'M';
    private final StringBuilder mStringBuilder;
    private final Integer mStrokeWidth;
    private final SvgPoint mStartPoint;
    private SvgPoint mLastPoint;

    public SvgPathBuilder(final SvgPoint startPoint, final Integer strokeWidth) {
        mStrokeWidth = strokeWidth;
        mStartPoint = startPoint;
        mLastPoint = startPoint;
        mStringBuilder = new StringBuilder();
        mStringBuilder.append(SVG_RELATIVE_CUBIC_BEZIER_CURVE);
    }

    public final Integer getStrokeWidth() {
        return mStrokeWidth;
    }

    public final SvgPoint getLastPoint() {
        return mLastPoint;
    }

    public void append(
            SvgPoint controlPoint1, SvgPoint controlPoint2, SvgPoint endPoint
    ) {
        mStringBuilder.append(makeRelativeCubicBezierCurve(controlPoint1, controlPoint2, endPoint));
        mLastPoint = endPoint;
    }

    @NonNull
    @Override
    public String toString() {
        return "<path " +
                "stroke-width=\"" +
                mStrokeWidth +
                "\" " +
                "d=\"" +
                SVG_MOVE +
                mStartPoint +
                mStringBuilder +
                "\"/>";
    }

    @NonNull
    private String makeRelativeCubicBezierCurve(
            @NonNull SvgPoint controlPoint1,
            @NonNull SvgPoint controlPoint2, @NonNull SvgPoint endPoint
    ) {
        String sControlPoint1 = controlPoint1.toRelativeCoordinates(mLastPoint);
        String sControlPoint2 = controlPoint2.toRelativeCoordinates(mLastPoint);
        String sEndPoint = endPoint.toRelativeCoordinates(mLastPoint);

        // discard zero curve
        String svg = sControlPoint1 +
                " " +
                sControlPoint2 +
                " " +
                sEndPoint +
                " "
                // discard zero curve
                ;
        if ("c0 0 0 0 0 0".equals(svg)) {
            return "";
        } else {
            return svg;
        }
    }


}
