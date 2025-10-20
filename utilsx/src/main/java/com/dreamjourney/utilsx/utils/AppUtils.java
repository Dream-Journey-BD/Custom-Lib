package com.dreamjourney.utilsx.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class AppUtils {

    @NonNull
    public static String generateFileName(String name) {
        return name + " " + System.currentTimeMillis();
    }

    // Rate Us to App store
    public static void rateUs(@NonNull Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.getPackageName())));
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id="
                            + context.getPackageName()))
            );
        }
    }

    // Open Any Uri By Selecting App
    public static void openURL(@NonNull Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    context, "No application can handle this request.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Hide Keyboard By Input
    public static void hideKeyboard(@NonNull Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE
        );
        if (imm != null) imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


}
