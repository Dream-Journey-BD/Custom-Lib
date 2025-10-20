package com.dreamjourney.utilsx.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;

import androidx.annotation.NonNull;

import com.dreamjourney.utilsx.R;

public class DialogUtils {

    // Its Custom Progress Dialog
    @NonNull
    public static Dialog progressDialog(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_progress_dialog);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) window.setBackgroundDrawableResource(R.drawable.empty_rect);
        return dialog;
    }

}
