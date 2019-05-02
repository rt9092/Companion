package in.wadersgroup.companion;

// (Class) This class contains common methods that are used frequently in other Activities.

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * @author Romil
 */
public class CommonMethods {

    // Method to show a short toast

    public static void showShortToast(Context context, String message) {

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

    }

    // Method to show a long toast

    public static void showLongToast(Context context, String message) {

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

    }

    // Method to create a Shared Preference

    public static void createStringPreference(Context context, String prefName,
                                              String key, String value) {

        SharedPreferences sharedPref = context.getSharedPreferences(prefName,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();

    }

    // Method to read a Shared Preference

    public static String readStringPreference(Context context, String prefName,
                                              String key) {

        SharedPreferences sharedPref = context.getSharedPreferences(prefName,
                Context.MODE_PRIVATE);

        String answer = sharedPref.getString(key, "None");
        return answer;

    }

    // Method to check if Internet Connectivity is available

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null
                && activeNetworkInfo.isConnectedOrConnecting();
    }

    // Method to check if in Sikkim and NorthBengal
    public static boolean isInSikkimAndNB(LatLng latLng) {

        // SouthWest and NorthEast bound

        LatLngBounds sikkimAndNB = new LatLngBounds(
                new LatLng(26.613217, 88.048717), new LatLng(28.108460, 88.828741));
        boolean boundCheck;
        if (sikkimAndNB.contains(latLng)) {
            boundCheck = true;
        } else {
            boundCheck = false;
        }

        return boundCheck;

    }


    // Method to check if Location Services are available

    public static boolean isLocationAvailable(Context context) {

        LocationManager lm = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            AnalyticsApplication.getInstance().trackException(ex);
        }

        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            AnalyticsApplication.getInstance().trackException(ex);
        }

        if (!gps_enabled && !network_enabled) {
            return false;
        } else {
            return true;
        }

    }

    // Method to open location settings

    public static void locationDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(viewIntent);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        builder.setMessage("Please turn on Location Services to use this feature of the application." +
                " It's a great app, we Promise!");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Method for Dialog

    public static void commonDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Method for opening wireless settings

    public static void wirelessDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        builder.setMessage("Please turn on Data Services to use the application to its true potential." +
                " It's a great app, we Promise!");
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    // Method to get IMEI code of Device

    public static String getIMEI(Context context) {
        TelephonyManager mngr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = mngr.getDeviceId();
        return imei;

    }

    // Method to generate Secret Key

    public static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            AnalyticsApplication.getInstance().trackException(e);
        }
        return null;
    }

}
