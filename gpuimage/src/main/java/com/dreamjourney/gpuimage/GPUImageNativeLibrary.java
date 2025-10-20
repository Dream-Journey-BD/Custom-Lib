package com.dreamjourney.gpuimage;

import android.graphics.Bitmap;

public class GPUImageNativeLibrary {

    static {
        System.loadLibrary("gpu-image");
    }

    public static native void YUVtoRBGA(byte[] yuv, int width, int height, int[] out);

    public static native void YUVtoARBG(byte[] yuv, int width, int height, int[] out);

    public static native void adjustBitmap(Bitmap srcBitmap);

}
