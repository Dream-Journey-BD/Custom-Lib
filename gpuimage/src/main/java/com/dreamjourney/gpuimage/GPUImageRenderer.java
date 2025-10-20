package com.dreamjourney.gpuimage;

import static com.dreamjourney.gpuimage.util.TextureRotationUtil.TEXTURE_NO_ROTATION;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import androidx.annotation.NonNull;

import com.dreamjourney.gpuimage.filter.GPUImageFilter;
import com.dreamjourney.gpuimage.util.OpenGlUtils;
import com.dreamjourney.gpuimage.util.Rotation;
import com.dreamjourney.gpuimage.util.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GPUImageRenderer implements GLSurfaceView.Renderer, GLTextureView.Renderer {
    private static final int NO_IMAGE = -1;
    public static final float[] CUBE = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    private GPUImageFilter filter;

    public final Object surfaceChangedWaiter = new Object();

    private int glTextureId = NO_IMAGE;
    private final FloatBuffer glCubeBuffer;
    private final FloatBuffer glTextureBuffer;
    private IntBuffer glRgbBuffer;

    private int outputWidth;
    private int outputHeight;
    private int imageWidth;
    private int imageHeight;
    private int addedPadding;

    private final Queue<Runnable> runOnDraw;
    private final Queue<Runnable> runOnDrawEnd;
    private Rotation rotation;
    private boolean flipHorizontal;
    private boolean flipVertical;
    private GPUImage.ScaleType scaleType = GPUImage.ScaleType.CENTER_CROP;

    private float backgroundRed = 0;
    private float backgroundGreen = 0;
    private float backgroundBlue = 0;

    public GPUImageRenderer(final GPUImageFilter filter) {
        this.filter = filter;
        runOnDraw = new LinkedList<>();
        runOnDrawEnd = new LinkedList<>();

        glCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        glCubeBuffer.put(CUBE).position(0);

        glTextureBuffer = ByteBuffer.allocateDirect(
                        TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        setRotation(Rotation.NORMAL, false, false);
    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        GLES20.glClearColor(backgroundRed, backgroundGreen, backgroundBlue, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        filter.ifNeedInit();
    }

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        outputWidth = width;
        outputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(filter.getProgram());
        filter.onOutputSizeChanged(width, height);
        adjustImageScaling();
        synchronized (surfaceChangedWaiter) {
            surfaceChangedWaiter.notifyAll();
        }
    }

    @Override
    public void onDrawFrame(final GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        runAll(runOnDraw);
        filter.onDraw(glTextureId, glCubeBuffer, glTextureBuffer);
        runAll(runOnDrawEnd);
    }

    /**
     * Sets the background color
     *
     * @param red   red color value
     * @param green green color value
     * @param blue  red color value
     */
    public void setBackgroundColor(float red, float green, float blue) {
        backgroundRed = red;
        backgroundGreen = green;
        backgroundBlue = blue;
    }

    private void runAll(@NonNull Queue<Runnable> queue) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (queue) {
            while (!queue.isEmpty()) {
                Objects.requireNonNull(queue.poll()).run();
            }
        }
    }

    public void onPreviewFrame(final byte[] data, final int width, final int height) {
        if (glRgbBuffer == null) {
            glRgbBuffer = IntBuffer.allocate(width * height);
        }
        if (runOnDraw.isEmpty()) {
            runOnDraw(() -> {
                GPUImageNativeLibrary.YUVtoRBGA(data, width, height, glRgbBuffer.array());
                glTextureId = OpenGlUtils.loadTexture(glRgbBuffer, width, height, glTextureId);

                if (imageWidth != width) {
                    imageWidth = width;
                    imageHeight = height;
                    adjustImageScaling();
                }
            });
        }
    }

    public void setFilter(final GPUImageFilter filter) {
        runOnDraw(() -> {
            final GPUImageFilter oldFilter = GPUImageRenderer.this.filter;
            GPUImageRenderer.this.filter = filter;
            if (oldFilter != null) {
                oldFilter.destroy();
            }
            GPUImageRenderer.this.filter.ifNeedInit();
            GLES20.glUseProgram(GPUImageRenderer.this.filter.getProgram());
            GPUImageRenderer.this.filter.onOutputSizeChanged(outputWidth, outputHeight);
        });
    }

    public void deleteImage() {
        runOnDraw(() -> {
            GLES20.glDeleteTextures(1, new int[]{
                    glTextureId
            }, 0);
            glTextureId = NO_IMAGE;
        });
    }

    public void setImageBitmap(final Bitmap bitmap) {
        setImageBitmap(bitmap, true);
    }

    public void setImageBitmap(final Bitmap bitmap, final boolean recycle) {
        if (bitmap == null) {
            return;
        }

        runOnDraw(() -> {
            Bitmap resizedBitmap = null;
            if (bitmap.getWidth() % 2 == 1) {
                resizedBitmap = Bitmap.createBitmap(
                        bitmap.getWidth() + 1, bitmap.getHeight(),
                        Bitmap.Config.ARGB_8888
                );
                // msg: set resized image density to be equal to source image density
                resizedBitmap.setDensity(bitmap.getDensity());
                Canvas can = new Canvas(resizedBitmap);
                can.drawARGB(0x00, 0x00, 0x00, 0x00);
                can.drawBitmap(bitmap, 0, 0, null);
                addedPadding = 1;
            } else {
                addedPadding = 0;
            }

            glTextureId = OpenGlUtils.loadTexture(
                    resizedBitmap != null ? resizedBitmap : bitmap, glTextureId, recycle);
            if (resizedBitmap != null) {
                resizedBitmap.recycle();
            }
            imageWidth = bitmap.getWidth();
            imageHeight = bitmap.getHeight();
            adjustImageScaling();
        });
    }

    public void setScaleType(GPUImage.ScaleType scaleType) {
        this.scaleType = scaleType;
    }

    protected int getFrameWidth() {
        return outputWidth;
    }

    protected int getFrameHeight() {
        return outputHeight;
    }

    private void adjustImageScaling() {
        final boolean rotated = rotation == Rotation.ROTATION_270 || rotation == Rotation.ROTATION_90;

        final float outWidth = rotated ? this.outputHeight : this.outputWidth;
        final float outHeight = rotated ? this.outputWidth : this.outputHeight;

        final float ratio1 = outWidth / imageWidth;
        final float ratio2 = outHeight / imageHeight;
        final float ratioMax = Math.max(ratio1, ratio2);

        final int imageWidthNew = Math.round(imageWidth * ratioMax);
        final int imageHeightNew = Math.round(imageHeight * ratioMax);

        final float ratioWidth = imageWidthNew / outWidth;
        final float ratioHeight = imageHeightNew / outHeight;

        float[] cube = CUBE;
        float[] textureCords = TextureRotationUtil.getRotation(rotation, flipHorizontal, flipVertical);

        if (scaleType == GPUImage.ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2f;
            float distVertical = (1 - 1 / ratioHeight) / 2f;
            textureCords = new float[]{
                    addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
                    addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
                    addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
                    addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
            };
        } else {
            cube = new float[]{
                    CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
                    CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
                    CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
                    CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
            };
        }

        glCubeBuffer.clear();
        glCubeBuffer.put(cube).position(0);
        glTextureBuffer.clear();
        glTextureBuffer.put(textureCords).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    public void setRotationCamera(final Rotation rotation, final boolean flipHorizontal,
                                  final boolean flipVertical) {
        setRotation(rotation, flipVertical, flipHorizontal);
    }

    public void setRotation(final Rotation rotation) {
        this.rotation = rotation;
        adjustImageScaling();
    }

    public void setRotation(final Rotation rotation,
                            final boolean flipHorizontal, final boolean flipVertical) {
        this.flipHorizontal = flipHorizontal;
        this.flipVertical = flipVertical;
        setRotation(rotation);
    }

    public Rotation getRotation() {
        return rotation;
    }

    public boolean isFlippedHorizontally() {
        return flipHorizontal;
    }

    public boolean isFlippedVertically() {
        return flipVertical;
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (runOnDraw) {
            runOnDraw.add(runnable);
        }
    }

    protected void runOnDrawEnd(final Runnable runnable) {
        synchronized (runOnDrawEnd) {
            runOnDrawEnd.add(runnable);
        }
    }

}
