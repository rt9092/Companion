package in.wadersgroup.companion;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpResponse;
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
 * Created by romil_wadersgroup on 7/2/16.
 */
public class RoadblockRemovalService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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

    String blockId = "";
    int notificationId = 0;

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


    public RoadblockRemovalService() {
        super("RoadblockRemovalService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        blockId = intent.getStringExtra("block_id");
        notificationId = intent.getIntExtra("notification_id", 0);

        System.out.println("Block ID: " + blockId);
        System.out.println("Notification ID: " + notificationId);

    }

    @Override
    public void onCreate() {
        super.onCreate();

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        buildGoogleApiClient();
        mGoogleApiClient.connect();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;

        }
        mGoogleApiClient.disconnect();

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

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);

            new UpdateRoadblockToServer().execute(mCurrentLocation);

        }


    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();

    }


    public class UpdateRoadblockToServer extends AsyncTask<Location, Void, Void> {

        @Override
        protected Void doInBackground(Location... params) {

            String email = null;
            try {
                email = URLEncoder.encode(CommonMethods
                        .readStringPreference(RoadblockRemovalService.this,
                                "userDetails", "email"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }
            String locationServer = "";
            try {
                locationServer = URLEncoder.encode(String.valueOf(params[0].getLatitude()) + ", " + String.valueOf(params[0].getLongitude()), "utf-8");
            } catch (UnsupportedEncodingException e) {
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }
            String activeFlag = "";
            try {
                activeFlag = URLEncoder.encode("inactive", "utf-8");
            } catch (UnsupportedEncodingException e) {
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }
            String secret_key = null;
            try {
                secret_key = URLEncoder.encode(CommonMethods
                        .readStringPreference(RoadblockRemovalService.this,
                                "secret", "secretKey"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }

            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            // Put URL Below
            HttpGet httpPost = new HttpGet(Constants.CONFIRMATION_URL
                    + email + "&location=" + locationServer + "&confirmation_type=" + activeFlag
                    + "&secret_key=" + secret_key + "&block_id=" + blockId);

            System.out.println("Values: " + email + " " + locationServer + " " + activeFlag + " " + secret_key + " " + blockId);

            HttpResponse response = null;
            try {
                response = httpClient.execute(httpPost,
                        localContext);
            } catch (IOException e) {
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }
            String res = "";
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);
            try {
                res = EntityUtils.toString(response.getEntity());

                System.out.println("Service Server: " + res);

            } catch (IOException e) {
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);


            //stopSelf();

            return null;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = ");

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
}
