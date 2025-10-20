package com.dreamjourney.utilsx.signaturepad.utils;


import androidx.annotation.NonNull;

public class SvgPoint {

    final Integer x, y;

    public SvgPoint(@NonNull TimedPoint point) {
        x = Math.round(point.x);
        y = Math.round(point.y);
    }

    public SvgPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toAbsoluteCoordinates() {
        return x + "," + y;
    }

    public String toRelativeCoordinates(@NonNull final SvgPoint point) {
        return (new SvgPoint(x - point.x, y - point.y)).toString();
    }

    @NonNull
    @Override
    public String toString() {
        return toAbsoluteCoordinates();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SvgPoint svgPoint = (SvgPoint) o;

        if (!x.equals(svgPoint.x)) return false;
        return y.equals(svgPoint.y);

    }

    @Override
    public int hashCode() {
        int result = x.hashCode();
        result = 31 * result + y.hashCode();
        return result;
    }

}
