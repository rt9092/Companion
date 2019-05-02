package in.wadersgroup.companion;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import logger.Log;

/**
 * Created by romil_wadersgroup on 5/2/16.
 */
public class PassiveLocationUpdateService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "passive-location-updates";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            500;


    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;


    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a Stri
     * ng.
     */
    protected String mLastUpdateTime;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        buildGoogleApiClient();
        mGoogleApiClient.connect();

    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);

    }

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            stopLocationUpdates();
        }
        mGoogleApiClient.disconnect();

    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {


        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.

        startLocationUpdates();


    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {

        // Update the location to the server.
        mCurrentLocation = location;

        new PassiveUpdateToServer().execute(location);

        //stopLocationUpdates();
        stopSelf();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = ");

    }

    class PassiveUpdateToServer extends AsyncTask<Location, Void, Void> {

        @Override
        protected Void doInBackground(Location... params) {

            String res = "";
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(PassiveLocationUpdateService.this,
                                "userDetails", "email"), "utf-8");

                String location = URLEncoder.encode(String.valueOf(params[0].getLatitude()) + ", " + String.valueOf(params[0].getLongitude()), "utf-8");
                String secret_key = URLEncoder.encode(CommonMethods
                        .readStringPreference(PassiveLocationUpdateService.this,
                                "secret", "secretKey"), "utf-8");

                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                // Put URL Below
                HttpGet httpPost = new HttpGet(Constants.PASSIVE_UPDATES_URL
                        + email + "&location=" + location + "&cause="
                        + "&secret_key=" + secret_key);

                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                res = EntityUtils.toString(response.getEntity());

                System.out.println("Passive Location : " + res);

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }

            return null;
        }
    }

}
