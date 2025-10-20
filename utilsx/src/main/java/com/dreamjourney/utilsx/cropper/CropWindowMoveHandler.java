package com.dreamjourney.utilsx.cropper;

import android.graphics.PointF;
import android.graphics.RectF;

import androidx.annotation.NonNull;


final class CropWindowMoveHandler {
    private final float mMinCropWidth;
    private final float mMinCropHeight;
    private final float mMaxCropWidth;
    private final float mMaxCropHeight;
    private final Type mType;
    private final PointF mTouchOffset = new PointF();

    public CropWindowMoveHandler(
            Type type, @NonNull CropWindowHandler cropWindowHandler,
            float touchX, float touchY
    ) {
        mType = type;
        mMinCropWidth = cropWindowHandler.getMinCropWidth();
        mMinCropHeight = cropWindowHandler.getMinCropHeight();
        mMaxCropWidth = cropWindowHandler.getMaxCropWidth();
        mMaxCropHeight = cropWindowHandler.getMaxCropHeight();
        calculateTouchOffset(cropWindowHandler.getRect(), touchX, touchY);
    }

    public void move(
            RectF rect, float x, float y, RectF bounds, int viewWidth,
            int viewHeight, float snapMargin, boolean fixedAspectRatio, float aspectRatio
    ) {

        float adjX = x + mTouchOffset.x;
        float adjY = y + mTouchOffset.y;

        if (mType == Type.CENTER) {
            moveCenter(rect, adjX, adjY, bounds, viewWidth, viewHeight, snapMargin);
        } else {
            if (fixedAspectRatio) moveSizeWithFixedAspectRatio(
                    rect, adjX, adjY, bounds, viewWidth,
                    viewHeight, snapMargin, aspectRatio
            );
            else moveSizeWithFreeAspectRatio(
                    rect, adjX, adjY, bounds,
                    viewWidth, viewHeight, snapMargin
            );
        }
    }

    private void calculateTouchOffset(
            RectF rect, float touchX, float touchY
    ) {

        float touchOffsetX = 0;
        float touchOffsetY = 0;

        // Calculate the offset from the appropriate handle.
        switch (mType) {
            case TOP_LEFT:
                touchOffsetX = rect.left - touchX;
                touchOffsetY = rect.top - touchY;
                break;
            case TOP_RIGHT:
                touchOffsetX = rect.right - touchX;
                touchOffsetY = rect.top - touchY;
                break;
            case BOTTOM_LEFT:
                touchOffsetX = rect.left - touchX;
                touchOffsetY = rect.bottom - touchY;
                break;
            case BOTTOM_RIGHT:
                touchOffsetX = rect.right - touchX;
                touchOffsetY = rect.bottom - touchY;
                break;
            case LEFT:
                touchOffsetX = rect.left - touchX;
                touchOffsetY = 0;
                break;
            case TOP:
                touchOffsetX = 0;
                touchOffsetY = rect.top - touchY;
                break;
            case RIGHT:
                touchOffsetX = rect.right - touchX;
                touchOffsetY = 0;
                break;
            case BOTTOM:
                touchOffsetX = 0;
                touchOffsetY = rect.bottom - touchY;
                break;
            case CENTER:
                touchOffsetX = rect.centerX() - touchX;
                touchOffsetY = rect.centerY() - touchY;
                break;
            default:
                break;
        }

        mTouchOffset.x = touchOffsetX;
        mTouchOffset.y = touchOffsetY;
    }

    private void moveCenter(
            @NonNull RectF rect, float x, float y, RectF bounds,
            int viewWidth, int viewHeight, float snapRadius
    ) {
        float dx = x - rect.centerX();
        float dy = y - rect.centerY();
        if (rect.left + dx < 0
                || rect.right + dx > viewWidth
                || rect.left + dx < bounds.left
                || rect.right + dx > bounds.right) {
            dx /= 1.05f;
            mTouchOffset.x -= dx / 2;
        }
        if (rect.top + dy < 0
                || rect.bottom + dy > viewHeight
                || rect.top + dy < bounds.top
                || rect.bottom + dy > bounds.bottom) {
            dy /= 1.05f;
            mTouchOffset.y -= dy / 2;
        }
        rect.offset(dx, dy);
        snapEdgesToBounds(rect, bounds, snapRadius);
    }

    private void moveSizeWithFreeAspectRatio(
            RectF rect, float x, float y, RectF bounds,
            int viewWidth, int viewHeight, float snapMargin
    ) {
        switch (mType) {
            case TOP_LEFT:
                adjustTop(rect, y, bounds, snapMargin, 0, false, false);
                adjustLeft(rect, x, bounds, snapMargin, 0, false, false);
                break;
            case TOP_RIGHT:
                adjustTop(rect, y, bounds, snapMargin, 0, false, false);
                adjustRight(rect, x, bounds, viewWidth, snapMargin, 0, false, false);
                break;
            case BOTTOM_LEFT:
                adjustBottom(rect, y, bounds, viewHeight, snapMargin, 0, false, false);
                adjustLeft(rect, x, bounds, snapMargin, 0, false, false);
                break;
            case BOTTOM_RIGHT:
                adjustBottom(rect, y, bounds, viewHeight, snapMargin, 0, false, false);
                adjustRight(rect, x, bounds, viewWidth, snapMargin, 0, false, false);
                break;
            case LEFT:
                adjustLeft(rect, x, bounds, snapMargin, 0, false, false);
                break;
            case TOP:
                adjustTop(rect, y, bounds, snapMargin, 0, false, false);
                break;
            case RIGHT:
                adjustRight(rect, x, bounds, viewWidth, snapMargin, 0, false, false);
                break;
            case BOTTOM:
                adjustBottom(rect, y, bounds, viewHeight, snapMargin, 0, false, false);
                break;
            default:
                break;
        }
    }

    private void moveSizeWithFixedAspectRatio(
            RectF rect, float x, float y, RectF bounds, int viewWidth,
            int viewHeight, float snapMargin, float aspectRatio
    ) {
        switch (mType) {
            case TOP_LEFT:
                if (calculateAspectRatio(x, y, rect.right, rect.bottom) < aspectRatio) {
                    adjustTop(rect, y, bounds, snapMargin, aspectRatio, true, false);
                    adjustLeftByAspectRatio(rect, aspectRatio);
                } else {
                    adjustLeft(rect, x, bounds, snapMargin, aspectRatio, true, false);
                    adjustTopByAspectRatio(rect, aspectRatio);
                }
                break;
            case TOP_RIGHT:
                if (calculateAspectRatio(rect.left, y, x, rect.bottom) < aspectRatio) {
                    adjustTop(rect, y, bounds, snapMargin, aspectRatio, false, true);
                    adjustRightByAspectRatio(rect, aspectRatio);
                } else {
                    adjustRight(rect, x, bounds, viewWidth, snapMargin, aspectRatio, true, false);
                    adjustTopByAspectRatio(rect, aspectRatio);
                }
                break;
            case BOTTOM_LEFT:
                if (calculateAspectRatio(x, rect.top, rect.right, y) < aspectRatio) {
                    adjustBottom(rect, y, bounds, viewHeight, snapMargin, aspectRatio, true, false);
                    adjustLeftByAspectRatio(rect, aspectRatio);
                } else {
                    adjustLeft(rect, x, bounds, snapMargin, aspectRatio, false, true);
                    adjustBottomByAspectRatio(rect, aspectRatio);
                }
                break;
            case BOTTOM_RIGHT:
                if (calculateAspectRatio(rect.left, rect.top, x, y) < aspectRatio) {
                    adjustBottom(rect, y, bounds, viewHeight, snapMargin, aspectRatio, false, true);
                    adjustRightByAspectRatio(rect, aspectRatio);
                } else {
                    adjustRight(rect, x, bounds, viewWidth, snapMargin, aspectRatio, false, true);
                    adjustBottomByAspectRatio(rect, aspectRatio);
                }
                break;
            case LEFT:
                adjustLeft(rect, x, bounds, snapMargin, aspectRatio, true, true);
                adjustTopBottomByAspectRatio(rect, bounds, aspectRatio);
                break;
            case TOP:
                adjustTop(rect, y, bounds, snapMargin, aspectRatio, true, true);
                adjustLeftRightByAspectRatio(rect, bounds, aspectRatio);
                break;
            case RIGHT:
                adjustRight(rect, x, bounds, viewWidth, snapMargin, aspectRatio, true, true);
                adjustTopBottomByAspectRatio(rect, bounds, aspectRatio);
                break;
            case BOTTOM:
                adjustBottom(rect, y, bounds, viewHeight, snapMargin, aspectRatio, true, true);
                adjustLeftRightByAspectRatio(rect, bounds, aspectRatio);
                break;
            default:
                break;
        }
    }

    private void snapEdgesToBounds(
            @NonNull RectF edges, @NonNull RectF bounds, float margin
    ) {
        if (edges.left < bounds.left + margin) {
            edges.offset(bounds.left - edges.left, 0);
        }
        if (edges.top < bounds.top + margin) {
            edges.offset(0, bounds.top - edges.top);
        }
        if (edges.right > bounds.right - margin) {
            edges.offset(bounds.right - edges.right, 0);
        }
        if (edges.bottom > bounds.bottom - margin) {
            edges.offset(0, bounds.bottom - edges.bottom);
        }
    }

    private void adjustLeft(
            RectF rect, float left, RectF bounds, float snapMargin,
            float aspectRatio, boolean topMoves, boolean bottomMoves
    ) {

        float newLeft = left;

        if (newLeft < 0) {
            newLeft /= 1.05f;
            mTouchOffset.x -= newLeft / 1.1f;
        }

        if (newLeft < bounds.left) {
            mTouchOffset.x -= (newLeft - bounds.left) / 2f;
        }

        if (newLeft - bounds.left < snapMargin) {
            newLeft = bounds.left;
        }

        // Checks if the window is too small horizontally
        if (rect.right - newLeft < mMinCropWidth) {
            newLeft = rect.right - mMinCropWidth;
        }

        // Checks if the window is too large horizontally
        if (rect.right - newLeft > mMaxCropWidth) {
            newLeft = rect.right - mMaxCropWidth;
        }

        if (newLeft - bounds.left < snapMargin) {
            newLeft = bounds.left;
        }

        // check vertical bounds if aspect ratio is in play
        if (aspectRatio > 0) {
            float newHeight = (rect.right - newLeft) / aspectRatio;

            // Checks if the window is too small vertically
            if (newHeight < mMinCropHeight) {
                newLeft = Math.max(bounds.left, rect.right - mMinCropHeight * aspectRatio);
                newHeight = (rect.right - newLeft) / aspectRatio;
            }

            // Checks if the window is too large vertically
            if (newHeight > mMaxCropHeight) {
                newLeft = Math.max(bounds.left, rect.right - mMaxCropHeight * aspectRatio);
                newHeight = (rect.right - newLeft) / aspectRatio;
            }

            // if top AND bottom edge moves by aspect ratio check that it is within full height bounds
            if (topMoves && bottomMoves) {
                newLeft =
                        Math.max(newLeft, Math.max(bounds.left, rect.right - bounds.height() * aspectRatio));
            } else {
                // if top edge moves by aspect ratio check that it is within bounds
                if (topMoves && rect.bottom - newHeight < bounds.top) {
                    newLeft = Math.max(bounds.left, rect.right - (rect.bottom - bounds.top) * aspectRatio);
                    newHeight = (rect.right - newLeft) / aspectRatio;
                }

                // if bottom edge moves by aspect ratio check that it is within bounds
                if (bottomMoves && rect.top + newHeight > bounds.bottom) {
                    newLeft =
                            Math.max(
                                    newLeft,
                                    Math.max(bounds.left, rect.right - (bounds.bottom - rect.top) * aspectRatio));
                }
            }
        }

        rect.left = newLeft;
    }

    private void adjustRight(
            RectF rect, float right, RectF bounds, int viewWidth, float snapMargin,
            float aspectRatio, boolean topMoves, boolean bottomMoves
    ) {

        float newRight = right;

        if (newRight > viewWidth) {
            newRight = viewWidth + (newRight - viewWidth) / 1.05f;
            mTouchOffset.x -= (newRight - viewWidth) / 1.1f;
        }

        if (newRight > bounds.right) {
            mTouchOffset.x -= (newRight - bounds.right) / 2f;
        }

        // If close to the edge
        if (bounds.right - newRight < snapMargin) {
            newRight = bounds.right;
        }

        // Checks if the window is too small horizontally
        if (newRight - rect.left < mMinCropWidth) {
            newRight = rect.left + mMinCropWidth;
        }

        // Checks if the window is too large horizontally
        if (newRight - rect.left > mMaxCropWidth) {
            newRight = rect.left + mMaxCropWidth;
        }

        // If close to the edge
        if (bounds.right - newRight < snapMargin) {
            newRight = bounds.right;
        }

        // check vertical bounds if aspect ratio is in play
        if (aspectRatio > 0) {
            float newHeight = (newRight - rect.left) / aspectRatio;

            // Checks if the window is too small vertically
            if (newHeight < mMinCropHeight) {
                newRight = Math.min(bounds.right, rect.left + mMinCropHeight * aspectRatio);
                newHeight = (newRight - rect.left) / aspectRatio;
            }

            // Checks if the window is too large vertically
            if (newHeight > mMaxCropHeight) {
                newRight = Math.min(bounds.right, rect.left + mMaxCropHeight * aspectRatio);
                newHeight = (newRight - rect.left) / aspectRatio;
            }

            // if top AND bottom edge moves by aspect ratio check that it is within full height bounds
            if (topMoves && bottomMoves) {
                newRight =
                        Math.min(newRight, Math.min(bounds.right, rect.left + bounds.height() * aspectRatio));
            } else {
                // if top edge moves by aspect ratio check that it is within bounds
                if (topMoves && rect.bottom - newHeight < bounds.top) {
                    newRight = Math.min(bounds.right, rect.left + (rect.bottom - bounds.top) * aspectRatio);
                    newHeight = (newRight - rect.left) / aspectRatio;
                }

                // if bottom edge moves by aspect ratio check that it is within bounds
                if (bottomMoves && rect.top + newHeight > bounds.bottom) {
                    newRight =
                            Math.min(
                                    newRight,
                                    Math.min(bounds.right, rect.left + (bounds.bottom - rect.top) * aspectRatio));
                }
            }
        }

        rect.right = newRight;
    }

    private void adjustTop(
            RectF rect, float top, RectF bounds, float snapMargin,
            float aspectRatio, boolean leftMoves, boolean rightMoves
    ) {

        float newTop = top;

        if (newTop < 0) {
            newTop /= 1.05f;
            mTouchOffset.y -= newTop / 1.1f;
        }

        if (newTop < bounds.top) {
            mTouchOffset.y -= (newTop - bounds.top) / 2f;
        }

        if (newTop - bounds.top < snapMargin) {
            newTop = bounds.top;
        }

        // Checks if the window is too small vertically
        if (rect.bottom - newTop < mMinCropHeight) {
            newTop = rect.bottom - mMinCropHeight;
        }

        // Checks if the window is too large vertically
        if (rect.bottom - newTop > mMaxCropHeight) {
            newTop = rect.bottom - mMaxCropHeight;
        }

        if (newTop - bounds.top < snapMargin) {
            newTop = bounds.top;
        }

        // check horizontal bounds if aspect ratio is in play
        if (aspectRatio > 0) {
            float newWidth = (rect.bottom - newTop) * aspectRatio;

            // Checks if the crop window is too small horizontally due to aspect ratio adjustment
            if (newWidth < mMinCropWidth) {
                newTop = Math.max(bounds.top, rect.bottom - (mMinCropWidth / aspectRatio));
                newWidth = (rect.bottom - newTop) * aspectRatio;
            }

            // Checks if the crop window is too large horizontally due to aspect ratio adjustment
            if (newWidth > mMaxCropWidth) {
                newTop = Math.max(bounds.top, rect.bottom - (mMaxCropWidth / aspectRatio));
                newWidth = (rect.bottom - newTop) * aspectRatio;
            }

            // if left AND right edge moves by aspect ratio check that it is within full width bounds
            if (leftMoves && rightMoves) {
                newTop = Math.max(newTop, Math.max(bounds.top, rect.bottom - bounds.width() / aspectRatio));
            } else {
                // if left edge moves by aspect ratio check that it is within bounds
                if (leftMoves && rect.right - newWidth < bounds.left) {
                    newTop = Math.max(bounds.top, rect.bottom - (rect.right - bounds.left) / aspectRatio);
                    newWidth = (rect.bottom - newTop) * aspectRatio;
                }

                // if right edge moves by aspect ratio check that it is within bounds
                if (rightMoves && rect.left + newWidth > bounds.right) {
                    newTop =
                            Math.max(
                                    newTop,
                                    Math.max(bounds.top, rect.bottom - (bounds.right - rect.left) / aspectRatio));
                }
            }
        }

        rect.top = newTop;
    }

    private void adjustBottom(
            RectF rect, float bottom, RectF bounds,
            int viewHeight, float snapMargin, float aspectRatio,
            boolean leftMoves, boolean rightMoves
    ) {

        float newBottom = bottom;

        if (newBottom > viewHeight) {
            newBottom = viewHeight + (newBottom - viewHeight) / 1.05f;
            mTouchOffset.y -= (newBottom - viewHeight) / 1.1f;
        }

        if (newBottom > bounds.bottom) {
            mTouchOffset.y -= (newBottom - bounds.bottom) / 2f;
        }

        if (bounds.bottom - newBottom < snapMargin) {
            newBottom = bounds.bottom;
        }

        // Checks if the window is too small vertically
        if (newBottom - rect.top < mMinCropHeight) {
            newBottom = rect.top + mMinCropHeight;
        }

        // Checks if the window is too small vertically
        if (newBottom - rect.top > mMaxCropHeight) {
            newBottom = rect.top + mMaxCropHeight;
        }

        if (bounds.bottom - newBottom < snapMargin) {
            newBottom = bounds.bottom;
        }

        // check horizontal bounds if aspect ratio is in play
        if (aspectRatio > 0) {
            float newWidth = (newBottom - rect.top) * aspectRatio;

            // Checks if the window is too small horizontally
            if (newWidth < mMinCropWidth) {
                newBottom = Math.min(bounds.bottom, rect.top + mMinCropWidth / aspectRatio);
                newWidth = (newBottom - rect.top) * aspectRatio;
            }

            // Checks if the window is too large horizontally
            if (newWidth > mMaxCropWidth) {
                newBottom = Math.min(bounds.bottom, rect.top + mMaxCropWidth / aspectRatio);
                newWidth = (newBottom - rect.top) * aspectRatio;
            }

            // if left AND right edge moves by aspect ratio check that it is within full width bounds
            if (leftMoves && rightMoves) {
                newBottom = Math.min(newBottom, Math.min(
                        bounds.bottom, rect.top + bounds.width() / aspectRatio)
                );
            } else {
                // if left edge moves by aspect ratio check that it is within bounds
                if (leftMoves && rect.right - newWidth < bounds.left) {
                    newBottom = Math.min(bounds.bottom,
                            rect.top + (rect.right - bounds.left) / aspectRatio
                    );
                    newWidth = (newBottom - rect.top) * aspectRatio;
                }

                // if right edge moves by aspect ratio check that it is within bounds
                if (rightMoves && rect.left + newWidth > bounds.right) {
                    newBottom = Math.min(newBottom,
                            Math.min(bounds.bottom, rect.top
                                    + (bounds.right - rect.left) / aspectRatio)
                    );
                }
            }
        }

        rect.bottom = newBottom;
    }

    private void adjustLeftByAspectRatio(@NonNull RectF rect, float aspectRatio) {
        rect.left = rect.right - rect.height() * aspectRatio;
    }

    private void adjustTopByAspectRatio(@NonNull RectF rect, float aspectRatio) {
        rect.top = rect.bottom - rect.width() / aspectRatio;
    }

    private void adjustRightByAspectRatio(@NonNull RectF rect, float aspectRatio) {
        rect.right = rect.left + rect.height() * aspectRatio;
    }

    private void adjustBottomByAspectRatio(@NonNull RectF rect, float aspectRatio) {
        rect.bottom = rect.top + rect.width() / aspectRatio;
    }

    private void adjustLeftRightByAspectRatio(
            @NonNull RectF rect, @NonNull RectF bounds, float aspectRatio
    ) {
        rect.inset((rect.width() - rect.height() * aspectRatio) / 2, 0);
        if (rect.left < bounds.left) {
            rect.offset(bounds.left - rect.left, 0);
        }
        if (rect.right > bounds.right) {
            rect.offset(bounds.right - rect.right, 0);
        }
    }

    private void adjustTopBottomByAspectRatio(
            @NonNull RectF rect, @NonNull RectF bounds, float aspectRatio
    ) {
        rect.inset(0, (rect.height() - rect.width() / aspectRatio) / 2);
        if (rect.top < bounds.top) {
            rect.offset(0, bounds.top - rect.top);
        }
        if (rect.bottom > bounds.bottom) {
            rect.offset(0, bounds.bottom - rect.bottom);
        }
    }

    private static float calculateAspectRatio(
            float left, float top, float right, float bottom
    ) {
        return (right - left) / (bottom - top);
    }

    public enum Type {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        LEFT,
        TOP,
        RIGHT,
        BOTTOM,
        CENTER
    }


}
