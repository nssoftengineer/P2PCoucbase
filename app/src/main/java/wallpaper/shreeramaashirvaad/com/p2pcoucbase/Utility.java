package wallpaper.shreeramaashirvaad.com.p2pcoucbase;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by neeraj on 12/27/2017.
 */

public class Utility {

    @SuppressWarnings("deprecation")
    public static String getLocalIpAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }
}
