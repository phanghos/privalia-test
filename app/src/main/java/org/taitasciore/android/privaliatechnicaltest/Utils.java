package org.taitasciore.android.privaliatechnicaltest;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by roberto on 06/03/17.
 */

public final class Utils {

    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
