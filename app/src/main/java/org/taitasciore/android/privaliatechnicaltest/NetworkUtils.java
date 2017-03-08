package org.taitasciore.android.privaliatechnicaltest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by roberto on 06/03/17.
 */

public final class NetworkUtils {

    /**
     * This method determines whether connection is established or not
     * @param context Context
     * @return True if it is connected. False otherwise
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
