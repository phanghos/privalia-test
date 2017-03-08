package org.taitasciore.android.privaliatechnicaltest;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by roberto on 06/03/17.
 */

public final class Utils {

    public static void showSnackbar(View v, String msg) {
        Snackbar.make(v, msg, Snackbar.LENGTH_LONG).show();
    }

    public static void showSnackbar(View v, View.OnClickListener listener) {
        Snackbar.make(v, "Something went wrong", Snackbar.LENGTH_LONG)
                .setAction("try again", listener)
                .show();
    }
}
