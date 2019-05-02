package in.wadersgroup.companion;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
public class RoadblockRemovalActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    LatLng staticMap;
    String roadblockCause;
    int notificationID;
    String blockID, sourceActivity = " ";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.statusbar));
        }

        setContentView(R.layout.activity_roadblock_removal);

        if (CommonMethods.isNetworkAvailable(RoadblockRemovalActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(RoadblockRemovalActivity.this);

        }

        if (CommonMethods.isLocationAvailable(RoadblockRemovalActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(RoadblockRemovalActivity.this);

        }

        TextView tvBlockID = (TextView) findViewById(R.id.tvBlockID);
        TextView tvNotificationText = (TextView) findViewById(R.id.tvNotificationText);
        Button bRemove = (Button) findViewById(R.id.bRemoveRoadblock);
        Button bReportLater = (Button) findViewById(R.id.bReportLater);
        Button bDontKnow = (Button) findViewById(R.id.bDontKnow);

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("Notifications");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");
        final Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Bold.ttf");

        toolbar_title.setTypeface(arvoRegular);
        tvBlockID.setTypeface(arvoRegular);
        tvNotificationText.setTypeface(arvoRegular);
        bRemove.setTypeface(arvoBold);
        bReportLater.setTypeface(arvoBold);
        bDontKnow.setTypeface(arvoBold);

        blockID = getIntent().getStringExtra("block_id");
        notificationID = getIntent().getIntExtra("notification_id", 0);
        String location = getIntent().getStringExtra("location");
        roadblockCause = getIntent().getStringExtra("cause");
        sourceActivity = getIntent().getStringExtra("sourceActivity");
        System.out.println("Notification Location: " + location);
        Double latitude = Double.valueOf(location.trim().split(",")[0].trim());
        Double longitude = Double.valueOf(location.trim().split(",")[1].trim());
        staticMap = new LatLng(latitude, longitude);

        tvBlockID.setText("Block ID : " + blockID);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        bRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (CommonMethods.isNetworkAvailable(RoadblockRemovalActivity.this)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RoadblockRemovalActivity.this);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new UpdateRoadblockToServer().execute(mCurrentLocation);
                        }
                    });
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    builder.setMessage("Are you sure you want to remove this roadblock?");
                    AlertDialog dialog = builder.create();
                    dialog.show();

                } else {
                    CommonMethods.wirelessDialog(RoadblockRemovalActivity.this);

                }
                //stopService(new Intent(RoadblockRemovalActivity.this, NotificationService.class));

            }
        });
        bReportLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notificationID == 0) {
                    finish();
                } else if (sourceActivity != null && sourceActivity.contentEquals("ReportedBlocksActivity")) {
                    startService(new Intent(RoadblockRemovalActivity.this, NotificationService.class));
                    finish();
                } else {
                    startService(new Intent(RoadblockRemovalActivity.this, NotificationService.class));
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(notificationID);
                    finish();
                }
            }


        });
        bDontKnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //stopService(new Intent(RoadblockRemovalActivity.this, NotificationService.class));
                if (notificationID == 0) {
                    finish();
                } else if (sourceActivity != null && sourceActivity.contentEquals("ReportedBlocksActivity")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RoadblockRemovalActivity.this);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            stopService(new Intent(RoadblockRemovalActivity.this, NotificationService.class));
                            finish();
                        }
                    });
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    builder.setMessage("Are you sure you don't want to update the roadblock status?");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RoadblockRemovalActivity.this);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            stopService(new Intent(RoadblockRemovalActivity.this, NotificationService.class));
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(notificationID);
                            finish();
                        }
                    });
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    builder.setMessage("Are you sure you don't want to update the roadblock status?");
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }

            }
        });


        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
    public void onMapReady(GoogleMap googleMap) {

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                staticMap, 14));
        CircleOptions circleOptions = new CircleOptions()
                .center(staticMap)
                .radius(500);

        circleOptions.strokeColor(getResources().getColor(R.color.accent));

        // Fill color of the circle
        // 0x represents, this is an hexadecimal code
        // 55 represents percentage of transparency. For 100% transparency, specify 00.
        // For 0% transparency ( ie, opaque ) , specify ff
        // The remaining 6 characters(00ff00) specify the fill color
        circleOptions.fillColor(0x20F7B900);

        // Border width of the circle
        circleOptions.strokeWidth(3);

        System.out.println("Cause : " + roadblockCause);

        if (roadblockCause.trim().contentEquals("landslide")) {

            googleMap.addMarker(new MarkerOptions().position(staticMap)
                    .title("Landslide")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_pointer_landslide)));
            googleMap.addCircle(circleOptions);

        } else if (roadblockCause.trim().contentEquals("accident")) {

            googleMap.addMarker(new MarkerOptions().position(staticMap)
                    .title("Accident")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_pointer_accident)));
            googleMap.addCircle(circleOptions);

        } else if (roadblockCause.trim().contentEquals("traffic")) {

            googleMap.addMarker(new MarkerOptions().position(staticMap)
                    .title("Traffic")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_pointer_traffic)));
            googleMap.addCircle(circleOptions);

        } else if (roadblockCause.trim().contentEquals("construction")) {

            googleMap.addMarker(new MarkerOptions().position(staticMap)
                    .title("Construction")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_pointer_construction)));
            googleMap.addCircle(circleOptions);

        } else if (roadblockCause.trim().contentEquals("other")) {

            googleMap.addMarker(new MarkerOptions().position(staticMap)
                    .title("Other Cause")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_pointer_options)));
            googleMap.addCircle(circleOptions);

        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();

        }

        return super.onOptionsItemSelected(item);
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

    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class UpdateRoadblockToServer extends AsyncTask<Location, Void, Void> {

        @Override
        protected Void doInBackground(Location... params) {

            String email = null;
            try {
                email = URLEncoder.encode(CommonMethods
                        .readStringPreference(RoadblockRemovalActivity.this,
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
                        .readStringPreference(RoadblockRemovalActivity.this,
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
                    + "&secret_key=" + secret_key + "&block_id=" + blockID);

            System.out.println("Values: " + email + " " + locationServer + " " + activeFlag + " " + secret_key + " " + blockID);

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
            notificationManager.cancel(notificationID);
            stopService(new Intent(RoadblockRemovalActivity.this, NotificationService.class));
            finish();

            return null;
        }
    }

}
