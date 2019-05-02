package in.wadersgroup.companion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Romil
 */
public class DataUploadActivity extends AppCompatActivity implements
        OnMapReadyCallback, ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {

    GoogleMap myMap;
    LatLng roadPointLocation;
    CoordinatorLayout mCordLayout;
    protected Location mLastLocation;
    protected boolean mAddressRequested;
    protected String mAddressOutput;

    protected static final String TAG = "location-updates-sample";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    FloatingActionButton clearPolylines;
    List<Polyline> allPolylines = new ArrayList<Polyline>();
    List<PolylineOptions> lineOptionsList = new ArrayList<PolylineOptions>();

    Double startLat, startLng;
    boolean popUp = false;
    List<String> jsonStrings = new ArrayList<String>();
    List<String> endPointStrings = new ArrayList<String>();
    int markerLength = 0;
    ArrayList<HashMap<String, String>> endPointsPolylines;
    TextView causeFlag;
    String resultFlagRoadblock;
    ProgressDialog pDialog;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if (sdkVersion >= 21) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.statusbar));
        }

        setContentView(R.layout.activity_data_upload);

        if (CommonMethods.isNetworkAvailable(DataUploadActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(DataUploadActivity.this);
        }

        if (CommonMethods.isLocationAvailable(DataUploadActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(DataUploadActivity.this);
        }

        mCordLayout = (CoordinatorLayout) findViewById(R.id.cLayout);

        Typeface arvo = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");
        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Bold.ttf");

        causeFlag = (TextView) findViewById(R.id.tvCauseFlag);
        causeFlag.setTypeface(arvoBold);

        causeFlag.setVisibility(View.GONE);

        pDialog = new ProgressDialog(DataUploadActivity.this);
        pDialog.setMessage("Getting current location...");
        pDialog.setCancelable(true);
        pDialog.show();

        pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        final FloatingActionButton roadblockOptions = (FloatingActionButton) findViewById(R.id.fabSelectOptions);

        roadblockOptions.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivityForResult(new Intent(DataUploadActivity.this,
                        RoadblockCauseDialogActivity.class), 1);

            }
        });

        clearPolylines = (FloatingActionButton) findViewById(R.id.fabClearPoly);

        clearPolylines.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                LatLng myLocation = new LatLng(mLastLocation.getLatitude(),
                        mLastLocation.getLongitude());

                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        myLocation, 12));
            }
        });

        clearPolylines.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub

                CommonMethods.showLongToast(DataUploadActivity.this,
                        "Clear the Map");

                return true;
            }
        });

        //mResultReceiver = new AddressResultReceiver(new Handler());
        mAddressRequested = false;
        mAddressOutput = "";

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buildGoogleApiClient();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        myMap.clear();

        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                mLastLocation.getLatitude(), mLastLocation.getLongitude()), 14));

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                resultFlagRoadblock = data.getStringExtra("result");
                new GetRoadPoint().execute(mLastLocation);

            }
            if (resultCode == RESULT_CANCELED) {
                CommonMethods.showShortToast(DataUploadActivity.this, "Error! Try Again.");
            }
        }

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();

        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        /*if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }*/

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }

    }

    @Override
    public void onMapReady(GoogleMap map) {
        // TODO Auto-generated method stub

        LatLng sydney = new LatLng(26.719414, 88.36106);

        map.setMyLocationEnabled(false);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10));

        myMap = map;

        map.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                // TODO Auto-generated method stub

                // for (PolylineOptions polyline : lineOptionsList) {
                // if (PolyUtil.isLocationOnPath(point, polyline.getPoints(),
                // false, 10)) {
                // // user clicked on polyline
                //
                // CommonMethods.showShortToast(DataUploadActivity.this,
                // "Clicked Polyline");
                //
                // }
                // }

            }
        });

        // map.setOnMarkerClickListener(new OnMarkerClickListener() {
        //
        // @Override
        // public boolean onMarkerClick(Marker marker) {
        // // TODO Auto-generated method stub
        //
        // if (marker.getTitle() != null) {
        //
        // if (marker.getTitle().contains("Road")) {
        //
        // endLat = marker.getPosition().latitude;
        // endLng = marker.getPosition().longitude;
        //
        // for (Polyline line : allPolylines) {
        // line.remove();
        // }
        //
        // allPolylines.clear();
        // myMap.clear();
        //
        // LatLng origin = new LatLng(startLat, startLng);
        // LatLng dest = new LatLng(endLat, endLng);
        // int i = 0;
        //
        // while (i < jsonStrings.size()) {
        //
        // String endPointLat = endPointStrings.get(i).trim()
        // .split(",")[0];
        // String endPointLng = endPointStrings.get(i).trim()
        // .split(",")[1];
        //
        // if (String.valueOf(endLat).contentEquals(
        // endPointLat.trim())
        // && String.valueOf(endLng).contentEquals(
        // endPointLng.trim())) {
        //
        // endPointUpdateLat = Double.valueOf(endPointLat);
        // endPointUpdateLng = Double.valueOf(endPointLng);
        //
        // String markPoly = jsonStrings.get(i);
        //
        // myMap.addMarker(new MarkerOptions()
        // .position(origin)
        // .title("Roadblock Start")
        // .icon(BitmapDescriptorFactory
        // .defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
        // myMap.addMarker(new MarkerOptions()
        // .position(dest)
        // .title("Roadblock End")
        // .icon(BitmapDescriptorFactory
        // .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        //
        // popUp = true;
        // new ParserTask().execute(markPoly);
        //
        // } else {
        //
        // System.out.println("End Point Lat: "
        // + endPointLat + " Lng: " + endPointLng);
        // }
        // i++;
        //
        // }
        //
        // } else {
        //
        // // Do something
        //
        // startLat = Double.valueOf(marker.getTitle().trim()
        // .split(",")[0]);
        // startLng = Double.valueOf(marker.getTitle().trim()
        // .split(",")[1]);
        //
        // System.out.println("My Markers: " + startLat + ","
        // + startLng);
        //
        // new GetAssocMarkers().execute(
        // Double.valueOf(marker.getTitle().trim()
        // .split(",")[0]),
        // Double.valueOf(marker.getTitle().trim()
        // .split(",")[1]));
        // }
        // } else {
        // startLat = marker.getPosition().latitude;
        // startLng = marker.getPosition().longitude;
        //
        // System.out.println("My Markers: " + startLat + ","
        // + startLng);
        //
        // new GetAssocMarkers().execute(Double.valueOf(marker
        // .getTitle().trim().split(",")[0]), Double
        // .valueOf(marker.getTitle().trim().split(",")[1]));
        // }
        //
        // return true;
        // }
        // });

    }


    /*protected void startIntentService() {
        // Create an intent for passing to the intent service responsible for
        // fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is
        // instantiated and started
        // (creating a process for it if needed); if it is running then it
        // remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }*/

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub

        startLocationUpdates();
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            LatLng loc = new LatLng(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude());
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 10));
        } else {

        }

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub

        mGoogleApiClient.connect();

    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the
     * LocationServices API.
     */
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
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub

        mLastLocation = location;
        LatLng loc = new LatLng(mLastLocation.getLatitude(),
                mLastLocation.getLongitude());

        pDialog.dismiss();

        startActivityForResult(new Intent(DataUploadActivity.this,
                RoadblockCauseDialogActivity.class), 1);

        stopLocationUpdates();

        // myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 10));

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /*class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        */

    /**
     * Receives data sent from FetchAddressIntentService and updates the UI
     * in MainActivity.
     *//*
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the
            // intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {

                Toast.makeText(getApplicationContext(), mAddressOutput,
                        Toast.LENGTH_LONG).show();

            }

            // Reset. Enable the Fetch Address button and stop showing the
            // progress bar.
            mAddressRequested = false;

        }
    }*/


    class MyLinkedMap<K, V> extends LinkedHashMap<K, V> {

        public V getValue(int i) {

            Map.Entry<K, V> entry = this.getEntry(i);
            if (entry == null)
                return null;

            return entry.getValue();
        }

        public Map.Entry<K, V> getEntry(int i) {
            // check if negetive index provided
            Set<Map.Entry<K, V>> entries = entrySet();
            int j = 0;

            for (Map.Entry<K, V> entry : entries)
                if (j++ == i)
                    return entry;

            return null;

        }

    }

    class RoadUpdate extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            pDialog = new ProgressDialog(DataUploadActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            String res = "";
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(DataUploadActivity.this,
                                "userDetails", "email"), "utf-8");

                String location = URLEncoder.encode(params[0], "utf-8");

                String cause = URLEncoder.encode(params[1], "utf-8");
                String secret_key = URLEncoder.encode(CommonMethods
                        .readStringPreference(DataUploadActivity.this,
                                "secret", "secretKey"), "utf-8");

                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                // Put URL Below
                HttpGet httpPost = new HttpGet(Constants.ROAD_UPDATE_URL
                        + email + "&location=" + location + "&cause=" + cause
                        + "&secret_key=" + secret_key);

                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                res = EntityUtils.toString(response.getEntity());
                System.out.println("Data Upload Response: " + res);
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

            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            pDialog.dismiss();

            // Only execute if success encountered in JSON Response.
            String successMsg = "";
            String blockId = "";
            String message = "";
            String location = "";
            String roadblockCause = "";

            System.out.println("DataUpload JSON: " + result);

            try {
                JSONObject jObj = new JSONObject(result.trim());
                successMsg = jObj.getString("success");
                blockId = jObj.getString("block_id");
                message = jObj.getString("message");
                location = jObj.getString("location");
                roadblockCause = jObj.getString("cause");

            } catch (JSONException e) {
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }

            if (successMsg.contentEquals("yes")) {
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                long[] pattern = {500, 500, 1000};

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(DataUploadActivity.this)
                                .setSmallIcon(R.drawable.companion_notification_logo)
                                .setSound(defaultSoundUri)
                                .setColor(getResources().getColor(R.color.toolbar)).setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("You just reported a roadblock. Click to know the details.")).setVibrate(pattern)
                                .setContentTitle("Remove Roadblock").setAutoCancel(false)
                                .setContentText("You just reported a roadblock. Click to know the details.");


                AtomicInteger c = new AtomicInteger(0);
                int mNotificationId = c.incrementAndGet();

                Intent removeIntent = new Intent(DataUploadActivity.this, RoadblockRemovalActivity.class);
                removeIntent.putExtra("block_id", blockId);
                removeIntent.putExtra("notification_id", mNotificationId);
                removeIntent.putExtra("location", location);

                System.out.println("DataUpload : " + location);

                removeIntent.putExtra("cause", roadblockCause.trim());
                removeIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                PendingIntent removePendingIntent = PendingIntent.getActivity(DataUploadActivity.this,
                        0, removeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentIntent(removePendingIntent);


                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                // Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());
                CommonMethods.showLongToast(DataUploadActivity.this, message);
                finish();
            } else if (successMsg.contentEquals("no")) {
                CommonMethods.showLongToast(DataUploadActivity.this, message);
                finish();
            } else {
                CommonMethods.showLongToast(DataUploadActivity.this, "Something went wrong. Please try again.");
            }

        }

    }

    List<HashMap<String, String>> routes = new ArrayList<HashMap<String, String>>();

    class DrawPolylines extends
            AsyncTask<String, Void, List<HashMap<String, String>>> {

        @Override
        protected List<HashMap<String, String>> doInBackground(String... params) {
            // TODO Auto-generated method stub

            List<LatLng> list = decodePoly(params[0]);

            System.out.println("Replaced: " + params[0]);

            for (int l = 0; l < list.size(); l++) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                routes.add(hm);
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String startPoint = "";
            String endPoint = "";

            if (result != null) {

                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result;

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    endPoint = point.get("lat") + "," + point.get("lng");

                    points.add(position);
                }

                String len = String.valueOf(points.size());

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);

                lineOptions.color(getResources().getColor(R.color.accent));

                System.out.println("End Point: " + endPoint);

                endPointStrings.add(endPoint); // Might create Problem. Size is
                // more than it should be.

                // Drawing polyline in the Google Map for the i-th route
                Polyline line = myMap.addPolyline(lineOptions);

                allPolylines.add(line);

                if (endPointStrings.size() == markerLength) {
                    myMap.clear();
                    LatLng positionCenter = new LatLng(startLat, startLng);
                    myMap.addMarker(new MarkerOptions()
                            .position(positionCenter)
                            .title("Central Marker")
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                    try {

                        for (int i = 0; i < endPointStrings.size(); i++) {

                            String[] endLoc = endPointStrings.get(i).trim()
                                    .split(",");

                            System.out.println("XYZ: "
                                    + endPointStrings.get(i).trim());

                            new DrawPolylines().execute(jsonStrings.get(i));

                            LatLng dest = new LatLng(Double.valueOf(endLoc[0]),
                                    Double.valueOf(endLoc[1]));
                            myMap.addMarker(new MarkerOptions()
                                    .position(dest)
                                    .title("Road " + i)
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                        }
                    } catch (IndexOutOfBoundsException e) {
                        AnalyticsApplication.getInstance().trackException(e);
                        e.printStackTrace();
                    }

                } else {
                    // Do something
                }

                if (popUp) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            DataUploadActivity.this);
                    // Add the buttons
                    builder.setPositiveButton("YES",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    // User clicked OK button

                                }
                            });
                    builder.setNegativeButton("NO",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    // User cancelled the dialog

                                    CommonMethods.showLongToast(
                                            DataUploadActivity.this,
                                            "Not Uploading Data");

                                }
                            });
                    // Set other dialog properties
                    builder.setMessage("Are you sure you want to send this update?");

                    // Create the AlertDialog
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }

            } else {
                CommonMethods.showLongToast(DataUploadActivity.this,
                        "Error in retrieving data! Try Later!");
            }

        }

    }

    /**
     * Method to decode polyline points Courtesy :
     * http://jeffreysambells.com/2010
     * /05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    List<String> myPolylines = new ArrayList<String>();

    public class GetAllPolylines extends AsyncTask<Void, Void, Void> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            pDialog = new ProgressDialog(DataUploadActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub

            // String assetString = "";
            //
            // StringBuilder buf = new StringBuilder();
            // try {
            //
            // InputStream mJSON = getAssets().open("allpolylines.txt");
            //
            // BufferedReader in = new BufferedReader(new InputStreamReader(
            // mJSON, "UTF-8"));
            //
            // while ((assetString = in.readLine()) != null) {
            // buf.append(assetString);
            // }
            //
            // in.close();
            // } catch (IOException e1) {
            // // TODO Auto-generated catch block
            // e1.printStackTrace();
            // }
            //
            // String toPassToJson = buf.toString().trim();
            String toPassToJson = "";
            endPointsPolylines = new ArrayList<HashMap<String, String>>();

            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(DataUploadActivity.this,
                                "userDetails", "email"), "utf-8");

                String location = URLEncoder.encode(
                        String.valueOf(mLastLocation.getLatitude()) + ", "
                                + String.valueOf(mLastLocation.getLongitude()),
                        "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(DataUploadActivity.this,
                                "secret", "secretKey"), "utf-8");

                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(Constants.ROAD_REQUEST_URL
                        + email + "&location=" + location + "&secret_key="
                        + secret);

                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                toPassToJson = EntityUtils.toString(response.getEntity());
            } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e1);
                e1.printStackTrace();
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }

            if (toPassToJson != null) {

                try {

                    JSONObject jObj = new JSONObject(toPassToJson);
                    JSONObject jsonObj = jObj.getJSONObject("response");
                    JSONArray polylineArray = jsonObj.getJSONArray("responses");

                    for (int i = 0; i < polylineArray.length(); i++) {
                        HashMap<String, String> myEndPoints = new HashMap<String, String>();

                        JSONObject temp = polylineArray.getJSONObject(i);
                        myPolylines.add(temp.getString("polyline"));
                        myEndPoints.put("ep1", temp.getString("end_point_one"));
                        myEndPoints.put("ep2", temp.getString("end_point_two"));
                        endPointsPolylines.add(myEndPoints);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    AnalyticsApplication.getInstance().trackException(e);
                    e.printStackTrace();
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            for (int i = 0; i < myPolylines.size(); i++) {
                new DrawNewPolylines().execute(myPolylines.get(i));
            }
            pDialog.dismiss();

        }

    }

    public class DrawNewPolylines extends
            AsyncTask<String, Void, List<HashMap<String, String>>> {

        List<HashMap<String, String>> route = new ArrayList<HashMap<String, String>>();
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            pDialog = new ProgressDialog(DataUploadActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            String endPoint = "";

            if (result != null) {

                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result;

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    endPoint = point.get("lat") + "," + point.get("lng");

                    points.add(position);
                }

                String len = String.valueOf(points.size());

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);

                lineOptions.color(getResources().getColor(R.color.accent));

                System.out.println("End Point: " + endPoint);

                endPointStrings.add(endPoint); // Might create Problem. Size is
                // more than it should be.

                // Drawing polyline in the Google Map for the i-th route
                Polyline line = myMap.addPolyline(lineOptions);
                lineOptionsList.add(lineOptions);

                allPolylines.add(line);
            }

            pDialog.dismiss();

        }

        @Override
        protected List<HashMap<String, String>> doInBackground(String... params) {
            // TODO Auto-generated method stub

            List<LatLng> list = decodePoly(params[0]);

            System.out.println("Replaced: " + params[0]);

            for (int l = 0; l < list.size(); l++) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                route.add(hm);
            }

            return route;

        }

    }


    public void uploadData(final Marker marker) {

        if (CommonMethods.isNetworkAvailable(DataUploadActivity.this)) {
            if (CommonMethods.isLocationAvailable(DataUploadActivity.this)) {
                new RoadUpdate().execute(String.valueOf(marker.getPosition().latitude)
                                + ", " + String.valueOf(marker.getPosition().longitude),
                        causeFlag.getText().toString());
            } else {
                CommonMethods.locationDialog(DataUploadActivity.this);

            }
        } else {
            CommonMethods.wirelessDialog(DataUploadActivity.this);

        }
    }

    public void dontUploadData(final Marker marker) {

        CommonMethods.showShortToast(DataUploadActivity.this,
                "Not updating Roadblock");

    }

    class GetRoadPoint extends AsyncTask<Location, Void, LatLng> {

        ProgressDialog pDialog;
        String res = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(DataUploadActivity.this);
            pDialog.setMessage("Getting Roadside Point...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(LatLng aVoid) {
            super.onPostExecute(aVoid);

            pDialog.dismiss();

            roadPointLocation = aVoid;

            if (roadPointLocation != null) {

                if (resultFlagRoadblock.trim().contentEquals("landslide")) {

                    final Marker a = myMap
                            .addMarker(new MarkerOptions()
                                    .position(roadPointLocation)
                                    .title("Landslide")
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.ic_pointer_landslide)));
                    a.showInfoWindow();
                    causeFlag.setText("landslide");
                    Snackbar snackbar = Snackbar.make(
                            mCordLayout,
                            "Reporting a Landslide here?"
                                    + "\n(Swipe Right to Dismiss)",
                            Snackbar.LENGTH_INDEFINITE).setAction("YES",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    uploadData(a);

                                }
                            });

                    snackbar.setActionTextColor(getResources().getColor(
                            R.color.accent));

                    snackbar.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

                        @Override
                        public void onViewAttachedToWindow(View v) {

                            // Le Lund

                        }

                        @Override
                        public void onViewDetachedFromWindow(View v) {

                            myMap.clear();

                        }

                    });
                    snackbar.show();


                } else if (resultFlagRoadblock.trim().contentEquals("accident")) {
                    final Marker a = myMap.addMarker(new MarkerOptions()
                            .position(roadPointLocation)
                            .title("Accident")
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.ic_pointer_accident)));
                    causeFlag.setText("accident");
                    a.showInfoWindow();
                    Snackbar snackbar = Snackbar.make(
                            mCordLayout,
                            "Reporting an Accident here?"
                                    + "\n(Swipe Right to Dismiss)",
                            Snackbar.LENGTH_INDEFINITE).setAction("YES",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    uploadData(a);

                                }
                            });
                    snackbar.setActionTextColor(getResources().getColor(
                            R.color.accent));

                    snackbar.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

                        @Override
                        public void onViewAttachedToWindow(View v) {

                            // Le Lund

                        }

                        @Override
                        public void onViewDetachedFromWindow(View v) {

                            myMap.clear();

                        }

                    });

                    snackbar.show();

                } else if (resultFlagRoadblock.trim().contentEquals("traffic")) {
                    final Marker a = myMap.addMarker(new MarkerOptions()
                            .position(roadPointLocation)
                            .title("Traffic")
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.ic_pointer_traffic)));
                    causeFlag.setText("traffic");
                    a.showInfoWindow();
                    Snackbar snackbar = Snackbar.make(
                            mCordLayout,
                            "Reporting Traffic here?"
                                    + "\n(Swipe Right to Dismiss)",
                            Snackbar.LENGTH_INDEFINITE).setAction("YES",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    uploadData(a);

                                }
                            });
                    snackbar.setActionTextColor(getResources().getColor(
                            R.color.accent));

                    snackbar.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

                        @Override
                        public void onViewAttachedToWindow(View v) {

                            // Le Lund

                        }

                        @Override
                        public void onViewDetachedFromWindow(View v) {

                            myMap.clear();

                        }

                    });

                    snackbar.show();

                } else if (resultFlagRoadblock.trim().contentEquals("construction")) {
                    final Marker a = myMap
                            .addMarker(new MarkerOptions()
                                    .position(roadPointLocation)
                                    .title("Construction")
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.ic_pointer_construction)));
                    causeFlag.setText("construction");
                    a.showInfoWindow();
                    Snackbar snackbar = Snackbar.make(
                            mCordLayout,
                            "Reporting Construction here?"
                                    + "\n(Swipe Right to Dismiss)",
                            Snackbar.LENGTH_INDEFINITE).setAction("YES",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    uploadData(a);

                                }
                            });
                    snackbar.setActionTextColor(getResources().getColor(
                            R.color.accent));

                    snackbar.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

                        @Override
                        public void onViewAttachedToWindow(View v) {

                            // Le Lund

                        }

                        @Override
                        public void onViewDetachedFromWindow(View v) {

                            myMap.clear();

                        }

                    });

                    snackbar.show();

                } else if (resultFlagRoadblock.trim().contentEquals("other")) {
                    final Marker a = myMap
                            .addMarker(new MarkerOptions()
                                    .position(roadPointLocation)
                                    .title("Other Cause")
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.ic_pointer_options)));
                    causeFlag.setText("other");
                    a.showInfoWindow();
                    Snackbar snackbar = Snackbar.make(
                            mCordLayout,
                            "Reporting Other Cause here?"
                                    + "\n(Swipe Right to Dismiss)",
                            Snackbar.LENGTH_INDEFINITE).setAction("YES",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    uploadData(a);

                                }
                            });
                    snackbar.setActionTextColor(getResources().getColor(
                            R.color.accent));

                    snackbar.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

                        @Override
                        public void onViewAttachedToWindow(View v) {

                            // Le Lund

                        }

                        @Override
                        public void onViewDetachedFromWindow(View v) {

                            myMap.clear();

                        }

                    });

                    snackbar.show();

                }
            } else {
                CommonMethods.showShortToast(DataUploadActivity.this, "Location not available! Try again!");
                finish();
            }

        }

        @Override
        protected LatLng doInBackground(Location... params) {
            LatLng roadPoint = null;

            OkHttpClient client = new OkHttpClient();
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(DataUploadActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(DataUploadActivity.this, "secret",
                                "secretKey"), "utf-8");
                String location = URLEncoder.encode(String.valueOf(params[0].getLatitude()) + "," + String.valueOf(params[0].getLongitude()), "utf-8");
                Request request = new Request.Builder()
                        .url(Constants.ROAD_POINT_API + email + "&secret_key=" + secret + "&location=" + location)
                        .build();

                Response response = client.newCall(request).execute();
                res = response.body().string();
                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String success = jsonObj.getString("success").trim();
                    if (success.contentEquals("yes")) {
                        String locationString = jsonObj.getString("location");
                        Double lat = Double.valueOf(locationString.trim().split(",")[0].trim());
                        Double lon = Double.valueOf(locationString.trim().split(",")[1].trim());
                        roadPoint = new LatLng(lat, lon);
                    } else {
                        // No success
                    }
                }
            } catch (UnsupportedEncodingException e) {
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            } catch (IOException e) {
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            } catch (JSONException e) {
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }


            return roadPoint;
        }
    }

}
