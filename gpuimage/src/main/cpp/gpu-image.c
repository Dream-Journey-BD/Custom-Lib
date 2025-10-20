#include <jni.h>
#include <android/bitmap.h>
#include <GLES2/gl2.h>

// 🧩 Convert YUV420 image data to RGBA pixel array
JNIEXPORT void JNICALL
Java_com_dreamjourney_gpuimage_GPUImageNativeLibrary_YUVtoRBGA(
        JNIEnv *env, jobject obj, jbyteArray yuv420sp,
        jint width, jint height, jintArray rgbOut
) {
    int sz, i, j, Y, Cr = 0, Cb = 0, pixPtr = 0, jDiv2 = 0, R = 0, G = 0, B = 0, cOff;
    int w = width, h = height;
    sz = w * h;

    // ⚙️ Get direct access to Java arrays for fast processing
    jint *rgbData = (jint *) (*env)->GetPrimitiveArrayCritical(env, rgbOut, 0);
    jbyte *yuv = (jbyte *) (*env)->GetPrimitiveArrayCritical(env, yuv420sp, 0);

    for (j = 0; j < h; j++) {
        pixPtr = j * w;
        jDiv2 = j >> 1;
        for (i = 0; i < w; i++) {
            Y = yuv[pixPtr];
            if (Y < 0) Y += 255;
            if ((i & 0x1) != 1) {
                cOff = sz + jDiv2 * w + (i >> 1) * 2;
                Cb = yuv[cOff];
                if (Cb < 0) Cb += 127; else Cb -= 128;
                Cr = yuv[cOff + 1];
                if (Cr < 0) Cr += 127; else Cr -= 128;
            }

            // 🎨 Convert YUV to RGB using BT.601 approximation
            Y = Y + (Y >> 3) + (Y >> 5) + (Y >> 7);
            R = Y + (Cr << 1) + (Cr >> 6);
            if (R < 0) R = 0; else if (R > 255) R = 255;
            G = Y - Cb + (Cb >> 3) + (Cb >> 4) - (Cr >> 1) + (Cr >> 3);
            if (G < 0) G = 0; else if (G > 255) G = 255;
            B = Y + Cb + (Cb >> 1) + (Cb >> 4) + (Cb >> 5);
            if (B < 0) B = 0; else if (B > 255) B = 255;

            // 🖼️ Write pixel to RGBA output
            rgbData[pixPtr++] = 0xff000000 + (R << 16) + (G << 8) + B;
        }
    }

    // 🔓 Release array locks
    (*env)->ReleasePrimitiveArrayCritical(env, rgbOut, rgbData, 0);
    (*env)->ReleasePrimitiveArrayCritical(env, yuv420sp, yuv, 0);
}

// 🧩 Convert YUV420 image data to ARGB pixel array
JNIEXPORT void JNICALL
Java_com_dreamjourney_gpuimage_GPUImageNativeLibrary_YUVtoARBG(
        JNIEnv *env, jobject obj, jbyteArray yuv420sp,
        jint width, jint height, jintArray rgbOut
) {
    int sz, i, j, Y, Cr = 0, Cb = 0, pixPtr = 0, jDiv2 = 0, R = 0, G = 0, B = 0, cOff;
    int w = width, h = height;
    sz = w * h;

    // ⚙️ Get direct access to Java arrays for fast processing
    jint *rgbData = (jint *) (*env)->GetPrimitiveArrayCritical(env, rgbOut, 0);
    jbyte *yuv = (jbyte *) (*env)->GetPrimitiveArrayCritical(env, yuv420sp, 0);

    for (j = 0; j < h; j++) {
        pixPtr = j * w;
        jDiv2 = j >> 1;
        for (i = 0; i < w; i++) {
            Y = yuv[pixPtr];
            if (Y < 0) Y += 255;
            if ((i & 0x1) != 1) {
                cOff = sz + jDiv2 * w + (i >> 1) * 2;
                Cb = yuv[cOff];
                if (Cb < 0) Cb += 127; else Cb -= 128;
                Cr = yuv[cOff + 1];
                if (Cr < 0) Cr += 127; else Cr -= 128;
            }

            // 🎨 Convert YUV to RGB using BT.601 approximation
            Y = Y + (Y >> 3) + (Y >> 5) + (Y >> 7);
            R = Y + (Cr << 1) + (Cr >> 6);
            if (R < 0) R = 0; else if (R > 255) R = 255;
            G = Y - Cb + (Cb >> 3) + (Cb >> 4) - (Cr >> 1) + (Cr >> 3);
            if (G < 0) G = 0; else if (G > 255) G = 255;
            B = Y + Cb + (Cb >> 1) + (Cb >> 4) + (Cb >> 5);
            if (B < 0) B = 0; else if (B > 255) B = 255;

            // 🖼️ Write pixel to ARGB output (swapped R/B)
            rgbData[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
        }
    }

    // 🔓 Release array locks
    (*env)->ReleasePrimitiveArrayCritical(env, rgbOut, rgbData, 0);
    (*env)->ReleasePrimitiveArrayCritical(env, yuv420sp, yuv, 0);
}

// 🧩 Adjust bitmap orientation by flipping vertically using OpenGL read
JNIEXPORT void JNICALL
Java_com_dreamjourney_gpuimage_GPUImageNativeLibrary_adjustBitmap(
        JNIEnv *jenv, jclass thiz,
        jobject src
) {
    unsigned char *srcByteBuffer;
    int result = 0, i, j;
    AndroidBitmapInfo srcInfo;

    // 🧠 Get bitmap info (width, height, format)
    result = AndroidBitmap_getInfo(jenv, src, &srcInfo);
    if (result != ANDROID_BITMAP_RESULT_SUCCESS) {
        return;
    }

    // 🔒 Lock bitmap pixels for direct access
    result = AndroidBitmap_lockPixels(jenv, src, (void **) &srcByteBuffer);
    if (result != ANDROID_BITMAP_RESULT_SUCCESS) {
        return;
    }

    int width = srcInfo.width;
    int height = srcInfo.height;

    // 🎮 Read pixels from current OpenGL framebuffer into bitmap buffer
    glReadPixels(
            0, 0, srcInfo.width, srcInfo.height,
            GL_RGBA, GL_UNSIGNED_BYTE, srcByteBuffer
    );

    int *pIntBuffer = (int *) srcByteBuffer;

    // 🔄 Flip bitmap vertically
    for (i = 0; i < height / 2; i++) {
        for (j = 0; j < width; j++) {
            int temp = pIntBuffer[(height - i - 1) * width + j];
            pIntBuffer[(height - i - 1) * width + j] = pIntBuffer[i * width + j];
            pIntBuffer[i * width + j] = temp;
        }
    }

    // 🔓 Unlock bitmap pixels
    AndroidBitmap_unlockPixels(jenv, src);

}