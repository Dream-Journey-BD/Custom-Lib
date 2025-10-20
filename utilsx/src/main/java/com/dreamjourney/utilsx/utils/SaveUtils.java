package com.dreamjourney.utilsx.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executors;

public class SaveUtils {

    public static void saveImage(
            Activity activity, Bitmap bitmap,
            String fileName, String folderName
    ) {
        if (bitmap == null) {
            Toast.makeText(activity, "Nothing to save", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = DialogUtils.progressDialog(activity);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                OutputStream fos;
                String fName = AppUtils.generateFileName(fileName);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, fName + ".png");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/" + folderName);
                    Uri uri = activity.getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                    );
                    if (uri == null) throw new IOException("Failed to create MediaStore record.");
                    fos = activity.getContentResolver().openOutputStream(uri);
                } else {
                    File dir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), folderName
                    );
                    if (!dir.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        dir.mkdirs();
                    }
                    File file = new File(dir, fName + ".png");
                    fos = new FileOutputStream(file);
                    MediaScannerConnection.scanFile(activity,
                            new String[]{file.getAbsolutePath()},
                            null, null
                    );
                }

                if (fos != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();

                    activity.runOnUiThread(() -> {
                        dialog.dismiss();
                        Toast.makeText(activity,
                                "Image saved successfully",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
                }

            } catch (IOException e) {
                activity.runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(activity,
                            "Failed to save image", Toast.LENGTH_SHORT
                    ).show();
                });
            }
        });
    }

    public static void saveTextFile(
            Context context, String name, String text
    ) {

        if (text == null || text.trim().isEmpty()) {
            Toast.makeText(context, "Nothing to save", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = name + "_File_" + System.currentTimeMillis() + ".txt";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ → MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain");
                values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

                Uri uri = context.getContentResolver()
                        .insert(MediaStore.Files.getContentUri("external"),
                                values
                        );

                if (uri != null) {
                    try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                        if (outputStream != null) {
                            outputStream.write(text.getBytes());
                            Toast.makeText(
                                    context, "💾 File saved: Documents/" + fileName,
                                    Toast.LENGTH_LONG
                            ).show();

                        } else {
                            Toast.makeText(
                                    context, "File save failed",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(
                                context, "File Save Error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                    }
                } else {
                    Toast.makeText(
                            context, "File save failed",
                            Toast.LENGTH_LONG
                    ).show();

                }

            } else {

                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                if (!dir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdirs();
                }

                File file = new File(dir, fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(text.getBytes());
                    Toast.makeText(
                            context, "💾 File saved: Documents/" + fileName,
                            Toast.LENGTH_LONG
                    ).show();
                } catch (Exception e) {
                    Toast.makeText(
                            context, "File Save Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(
                    context, "File Save Error: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }


    }

}
