package com.dreamjourney.utilsx.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

public class ShareUtils {

    public static void shareText(Context context, @NonNull String text) {

        if (text.trim().isEmpty()) {
            Toast.makeText(context, "Nothing to share", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, "Share via"));
    }

    public static void shareImage(Activity activity, Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(activity, "Nothing to share", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = DialogUtils.progressDialog(activity);
        dialog.show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Cache folder
                File cachePath = new File(activity.getCacheDir(), "images");
                //noinspection ResultOfMethodCallIgnored
                cachePath.mkdirs();

                File file = new File(cachePath, "share_image.png");
                try (FileOutputStream stream = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                }

                Uri uri = FileProvider.getUriForFile(activity,
                        activity.getPackageName() + ".provider", file
                );

                activity.runOnUiThread(() -> {
                    dialog.dismiss();
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/png");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    activity.startActivity(Intent.createChooser(shareIntent, "Share Image"));
                });

            } catch (IOException e) {
                activity.runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(activity, "Image share failed", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}
