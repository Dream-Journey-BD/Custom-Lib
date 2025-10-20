package com.dreamjourney.utilsx.utils;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CopyUtils {

    // Copy Text As Clipboard
    public static void copyToClipboard(@NonNull Context context, String text) {

        if (text == null || text.trim().isEmpty()) {
            Toast.makeText(context, "Nothing to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    // Copy Image In Clipboard
    public static void copyImageToClipboard(@NonNull Context context, Bitmap bitmap) {

        if (bitmap == null) {
            Toast.makeText(context, "Nothing to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = AppUtils.generateFileName("clipboard_image");
        try {
            // 1. Cache folder এ temporary file
            File cachePath = new File(context.getCacheDir(), "clipboard_images");
            if (!cachePath.exists()) {
                //noinspection ResultOfMethodCallIgnored
                cachePath.mkdirs();
            }

            File file = new File(cachePath, fileName + ".png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // 2. FileProvider থেকে URI নিন
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

            // 3. Clipboard এ copy
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newUri(context.getContentResolver(), "Image", uri);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context, "Image copied to clipboard", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            //e.printStackTrace();
            Toast.makeText(context, "Failed to copy image", Toast.LENGTH_SHORT).show();
        }
    }

}
