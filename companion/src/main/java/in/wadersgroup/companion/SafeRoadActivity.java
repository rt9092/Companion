package in.wadersgroup.companion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
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
import java.util.List;

public class SafeRoadActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    GoogleMap map;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    RelativeLayout llOperationFailed;
    ImageButton ibOperationFailed;
    String source, destination;
    Typeface arvoRegular;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_safe_road);

        if (CommonMethods.isNetworkAvailable(SafeRoadActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(SafeRoadActivity.this);

        }

        /*if (CommonMethods.isLocationAvailable(SafeRoadActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(SafeRoadActivity.this);

        }*/

        // Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Bold.ttf");
        arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        llOperationFailed = (RelativeLayout) findViewById(R.id.llOperationFailed);
        TextView tvOperationFailed = (TextView) findViewById(R.id.tvOperationFailed);
        ibOperationFailed = (ImageButton) findViewById(R.id.ibOperationFailed);

        llOperationFailed.setVisibility(View.GONE);

        tvOperationFailed.setTypeface(arvoBold);

        // Getting Map for the SupportMapFragment
        map = fm.getMap();

        buildGoogleApiClient();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (weakHashMap.get(marker.getId()) != null) {

                    String blockId = weakHashMap.get(marker.getId()).trim()
                            .split("----")[0].trim();
                    String roadblockTypes = weakHashMap.get(marker.getId()).trim()
                            .split("----")[1].trim();
                    String roadblockConfidence = weakHashMap.get(marker.getId()).trim()
                            .split("----")[2].trim();
                    String roadblockAddress = weakHashMap.get(marker.getId()).trim()
                            .split("----")[3].trim();

                    map.setInfoWindowAdapter(new MyInfoWindowAdapter());
                }

                return false;
            }
        });

    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            TextView tvCause = ((TextView) myContentsView.findViewById(R.id.tvRoadblockCause));
            tvCause.setText(weakHashMap.get(marker.getId()).trim()
                    .split("----")[1].trim().toUpperCase());
            tvCause.setTypeface(arvoRegular);
            TextView tvAddress = ((TextView) myContentsView.findViewById(R.id.tvRoadblockAddress));
            tvAddress.setText("at  " + weakHashMap.get(marker.getId()).trim()
                    .split("----")[3].trim());
            tvAddress.setTypeface(arvoRegular);
            TextView tvActiveStatus = ((TextView) myContentsView.findViewById(R.id.tvRoadblockActiveStatus));
            tvActiveStatus.setText(weakHashMap.get(marker.getId()).trim()
                    .split("----")[2].trim());
            tvActiveStatus.setTypeface(arvoRegular);

            return myContentsView;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    List<String> roadblockLocation = new ArrayList<String>();
    List<String> roadblockCause = new ArrayList<String>();
    List<String> roadblockId = new ArrayList<String>();
    List<String> roadblockActiveStatus = new ArrayList<String>();
    List<String> roadblockAddress = new ArrayList<String>();
    HashMap<String, String> weakHashMap = new HashMap<String, String>();

    public class FetchRoad extends AsyncTask<String, Void, Void> {

        ProgressDialog pDialog;
        String res = "";
        String safePoly;
        boolean resultBoolean = false;
        String sourceJson, destJson;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            pDialog = new ProgressDialog(SafeRoadActivity.this);
            pDialog.setMessage("Checking your route...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (tryCatchFlag) {

                if (resultBoolean) {

                    new DrawNewPolylines().execute(safePoly);

                    Double sourceLatitude = Double.valueOf(sourceJson.trim().split(",")[0].trim());
                    Double sourceLongitude = Double.valueOf(sourceJson.trim().split(",")[1].trim());
                    Double dstnLatitude = Double.valueOf(destJson.trim().split(",")[0].trim());
                    Double dstnLongitude = Double.valueOf(destJson.trim().split(",")[1].trim());

                    map.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(sourceLatitude, sourceLongitude), 11));

                    map.addMarker(new MarkerOptions().position(new LatLng(sourceLatitude, sourceLongitude))
                            .title("SOURCE").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_journey_start)));

                    map.addMarker(new MarkerOptions().position(new LatLng(dstnLatitude, dstnLongitude))
                            .title("DESTINATION").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_journey_end)));

                    if (roadblockLocation.size() != 0) {

                        for (int i = 0; i < roadblockCause.size(); i++) {

                            Double latitude = Double.valueOf(roadblockLocation
                                    .get(i).trim().split(", ")[0]);
                            Double longitude = Double.valueOf(roadblockLocation
                                    .get(i).trim().split(", ")[1]);

                            if (roadblockCause.get(i).trim()
                                    .contentEquals("landslide")) {
                                Marker mMarker = map.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longitude))
                                        .title(roadblockCause.get(i).toUpperCase())
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.ic_pointer_landslide)));
                                weakHashMap.put(mMarker.getId(), roadblockId.get(i) + "----"
                                        + roadblockCause.get(i) + "----" + roadblockActiveStatus.get(i) + "----" + roadblockAddress.get(i) + "----" + "BS");
                            } else if (roadblockCause.get(i).trim()
                                    .contentEquals("accident")) {
                                Marker mMarker = map.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longitude))
                                        .title(roadblockCause.get(i).toUpperCase())
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.ic_pointer_accident)));
                                weakHashMap.put(mMarker.getId(), roadblockId.get(i) + "----"
                                        + roadblockCause.get(i) + "----" + roadblockActiveStatus.get(i) + "----" + roadblockAddress.get(i) + "----" + "BS");
                            } else if (roadblockCause.get(i).trim()
                                    .contentEquals("traffic")) {
                                Marker mMarker = map.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longitude))
                                        .title(roadblockCause.get(i).toUpperCase())
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.ic_pointer_traffic)));
                                weakHashMap.put(mMarker.getId(), roadblockId.get(i) + "----"
                                        + roadblockCause.get(i) + "----" + roadblockActiveStatus.get(i) + "----" + roadblockAddress.get(i) + "----" + "BS");
                            } else if (roadblockCause.get(i).trim()
                                    .contentEquals("construction")) {
                                Marker mMarker = map.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longitude))
                                        .title(roadblockCause.get(i).toUpperCase())
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.ic_pointer_construction)));
                                weakHashMap.put(mMarker.getId(), roadblockId.get(i) + "----"
                                        + roadblockCause.get(i) + "----" + roadblockActiveStatus.get(i) + "----" + roadblockAddress.get(i) + "----" + "BS");
                            } else if (roadblockCause.get(i).trim()
                                    .contentEquals("other")) {
                                Marker mMarker = map.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longitude))
                                        .title(roadblockCause.get(i).toUpperCase())
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.ic_pointer_options)));
                                weakHashMap.put(mMarker.getId(), roadblockId.get(i) + "----"
                                        + roadblockCause.get(i) + "----" + roadblockActiveStatus.get(i) + "----" + roadblockAddress.get(i) + "----" + "BS");
                            }
                        }
                    } else {
                        CommonMethods.showShortToast(SafeRoadActivity.this,
                                "Congratulations! No Roadblocks! Have a happy journey!");

                    }
                } else {
                    CommonMethods.showShortToast(SafeRoadActivity.this, "Destination not found. Try again.");
                    finish();
                }
            } else {
                llOperationFailed.setVisibility(View.VISIBLE);
                ibOperationFailed
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub

                                if (source.trim().contentEquals("My Location")) {
                                    source = String.valueOf(mLastLocation
                                            .getLatitude())
                                            + ", "
                                            + String.valueOf(mLastLocation
                                            .getLongitude());

                                    if (CommonMethods.isNetworkAvailable(SafeRoadActivity.this)) {

                                        new FetchRoad()
                                                .execute(source, destination);
                                    } else {
                                        CommonMethods.wirelessDialog(SafeRoadActivity.this);

                                    }
                                } else {
                                    if (CommonMethods.isNetworkAvailable(SafeRoadActivity.this)) {

                                        new FetchRoad()
                                                .execute(source, destination);
                                    } else {
                                        CommonMethods.wirelessDialog(SafeRoadActivity.this);

                                    }
                                }

                            }
                        });
            }

            pDialog.dismiss();

        }

        boolean tryCatchFlag = false;

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub

            try {
                String source = URLEncoder.encode(params[0], "utf-8");

                String dest = URLEncoder.encode(params[1], "utf-8");
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(SafeRoadActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(SafeRoadActivity.this, "secret",
                                "secretKey"), "utf-8");

                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                // Put URL Below
                HttpGet httpPost = new HttpGet(Constants.SAFE_ROAD_URL + email
                        + "&src=" + source + "&dest=" + dest + "&secret_key="
                        + secret);

                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                res = EntityUtils.toString(response.getEntity());
                System.out.println("Route Info: " + res);
                tryCatchFlag = true;

                if (res != null) {
                    try {
                        System.out.println("Response of JSON:" + res);
                        JSONObject jsonObj = new JSONObject(res.trim()); // JSONException
                        JSONArray myJson = jsonObj.getJSONArray("route");
                        JSONObject routeArrayObject = myJson.getJSONObject(0);
                        JSONObject bestRouteJSONObject = routeArrayObject
                                .getJSONObject("best_route");

                        sourceJson = routeArrayObject.getString("start_location");
                        destJson = routeArrayObject.getString("end_location");

                        safePoly = bestRouteJSONObject
                                .getString("polyline");
                        if (safePoly.trim().contentEquals("null")) {
                            resultBoolean = false;
                        } else {

                            resultBoolean = true;

                            JSONArray jsonRoadblock = routeArrayObject
                                    .getJSONArray("road_blocks");

                            if (jsonRoadblock.length() >= 0) {

                                for (int i = 0; i < jsonRoadblock.length(); i++) {

                                    JSONObject roadBlock = jsonRoadblock
                                            .getJSONObject(i);

                                    String roadB = roadBlock.getString("location");
                                    String cause = roadBlock.getString("cause");
                                    String activeStatus = roadBlock.getString("active");
                                    String blockId = roadBlock.getString("block_id");
                                    String addressRoadblock = roadBlock.getString("address");
                                    roadblockLocation.add(roadB);
                                    roadblockCause.add(cause);
                                    roadblockActiveStatus.add(activeStatus);
                                    roadblockId.add(blockId);
                                    roadblockAddress.add(addressRoadblock);

                                }
                            } else {
                                CommonMethods.showShortToast(SafeRoadActivity.this,
                                        "No Roadblocks! YAY!!");
                            }
                        }
                    } catch (JSONException e) {
                        AnalyticsApplication.getInstance().trackException(e);
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ServiceHandler",
                            "Couldn't get any data from the url");
                }

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
                tryCatchFlag = false;
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
                tryCatchFlag = false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
                tryCatchFlag = false;
            }

            return null;
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

                lineOptions.color(getResources().getColor(R.color.statusbar));

                System.out.println("End Point: " + endPoint);
                /*LatLng myLocation = new LatLng(Double.valueOf(source.trim()
                        .split(",")[0].trim()), Double.valueOf(source.trim().split(
                        ",")[1].trim()));*/


                System.out.println("First Point of Polyline: " + points.get(0));
                Polyline line = map.addPolyline(lineOptions);

            }

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

    public class DrawRoadblockPolylines extends
            AsyncTask<String, Void, List<HashMap<String, String>>> {

        List<HashMap<String, String>> route = new ArrayList<HashMap<String, String>>();
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

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
                lineOptions.width(16);

                lineOptions.color(getResources().getColor(R.color.red));

                System.out.println("End Point: " + endPoint);

                // Drawing polyline in the Google Map for the i-th route
                Polyline line = map.addPolyline(lineOptions);

            }

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

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        Intent i = getIntent();
        source = i.getStringExtra("source");
        destination = i.getStringExtra("destination");

        if (source.trim().contentEquals("My Location")) {
            source = String.valueOf(mLastLocation.getLatitude()) + ", "
                    + String.valueOf(mLastLocation.getLongitude());
            if (CommonMethods.isNetworkAvailable(SafeRoadActivity.this)) {

                new FetchRoad()
                        .execute(source, destination);
            } else {
                CommonMethods.wirelessDialog(SafeRoadActivity.this);

            }
        } else {
            if (CommonMethods.isNetworkAvailable(SafeRoadActivity.this)) {

                new FetchRoad()
                        .execute(source, destination);
            } else {
                CommonMethods.wirelessDialog(SafeRoadActivity.this);

            }
        }

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub

        mGoogleApiClient.connect();

    }

}
