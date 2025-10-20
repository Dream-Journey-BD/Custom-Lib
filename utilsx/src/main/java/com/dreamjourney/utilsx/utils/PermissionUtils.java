package com.dreamjourney.utilsx.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    private static final String TAG = "Permission_LOG";

    // Permission Code Here
    public static boolean askPermission(
            Activity activity, String permission, String name,
            ActivityResultLauncher<String> launcher
    ) {

        if (ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            Log.d(TAG, name + " Permission Already Granted ✅");
            return true;

        } else if (activity.shouldShowRequestPermissionRationale(permission)) {
            // Show rationale if needed
            new AlertDialog.Builder(activity)
                    .setTitle(name + " Permission")
                    .setMessage(name + " permission is required, please allow it.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        if (launcher != null) {
                            launcher.launch(permission);
                        } else {
                            Log.d(TAG, "Activity Result Launcher is NULL");
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();

            return false;
        } else {
            // Request permission
            if (launcher != null) {
                launcher.launch(permission);
            } else Log.d(TAG, "Activity Result Launcher is NULL");
            return false;
        }


    }

    public static void requestPermissionWithSetting(
            @NonNull Activity activity, boolean isGranted, String permission, String name
    ) {
        if (!isGranted && !activity.shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(activity)
                    .setTitle(name + " Permission Required")
                    .setMessage("You have permanently denied " + name +
                            " permission. Please enable it from app settings.")
                    .setPositiveButton("Go to Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(
                                Uri.fromParts("package", activity.getPackageName(),
                                        null)
                        );
                        activity.startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, whichBtn) -> dialog.dismiss())
                    .create()
                    .show();
        }
    }

}
