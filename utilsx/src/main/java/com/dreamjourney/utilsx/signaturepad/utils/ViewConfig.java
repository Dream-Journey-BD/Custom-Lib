package com.dreamjourney.utilsx.signaturepad.utils;

import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

public class ViewConfig {

    public static boolean isLaidOut(@NonNull View view) {
        return view.isLaidOut();

    }

    public static void removeOnGlobalLayoutListener(
            @NonNull ViewTreeObserver observer, ViewTreeObserver.OnGlobalLayoutListener victim
    ) {
        observer.removeOnGlobalLayoutListener(victim);
    }

}