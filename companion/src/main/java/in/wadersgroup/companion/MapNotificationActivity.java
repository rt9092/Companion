package in.wadersgroup.companion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by romil_wadersgroup on 7/3/16.
 */
public class MapNotificationActivity extends AppCompatActivity implements OnMapReadyCallback {

    String blockId, location, cause, address, confidence;
    CoordinatorLayout mCordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_notification);

        if (CommonMethods.isNetworkAvailable(MapNotificationActivity.this)) {

        } else {
            CommonMethods.wirelessDialog(MapNotificationActivity.this);
        }

        /*if (CommonMethods.isLocationAvailable(MapNotificationActivity.this)) {

        } else {
            CommonMethods.locationDialog(MapNotificationActivity.this);
        }*/

        mCordLayout = (CoordinatorLayout) findViewById(R.id.cLayout);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent i = getIntent();
        FloatingActionButton updateActive = (FloatingActionButton) findViewById(R.id.fabActive);
        FloatingActionButton updateInactive = (FloatingActionButton) findViewById(R.id.fabInactive);
        Button dontKnowUpdate = (Button) findViewById(R.id.bDontKnow);

        if (i.getStringExtra("header") != null) {
            if (i.getStringExtra("header").trim().contentEquals("active_tracking")) {

                blockId = i.getStringExtra("block_id");
                location = i.getStringExtra("location");
                address = i.getStringExtra("address");
                cause = i.getStringExtra("cause");
                confidence = i.getStringExtra("active");

                updateActive.setVisibility(View.GONE);
                updateInactive.setVisibility(View.GONE);
                dontKnowUpdate.setVisibility(View.GONE);

                AlertDialog.Builder builder = new AlertDialog.Builder(MapNotificationActivity.this);
                builder.setMessage(i.getStringExtra("cause").trim().substring(0, 1).toUpperCase() + i.getStringExtra("cause").trim().substring(1).toLowerCase() + " at " + getIntent().getStringExtra("address").trim() + ". Please take appropriate action and stay safe.")
                        .setTitle(i.getStringExtra("cause").trim().substring(0, 1).toUpperCase() + i.getStringExtra("cause").trim().substring(1).toLowerCase() + " Ahead");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            if (i.getStringExtra("header").trim().contentEquals("roadblock_reported")) {

                blockId = i.getStringExtra("block_id");
                location = i.getStringExtra("location");
                address = i.getStringExtra("address");
                cause = i.getStringExtra("cause");
                confidence = i.getStringExtra("active");

                updateActive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (CommonMethods.isNetworkAvailable(MapNotificationActivity.this)) {

                            new ConfirmBlock().execute("active");
                        } else {
                            CommonMethods.wirelessDialog(MapNotificationActivity.this);
                        }

                    }
                });

                updateInactive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (CommonMethods.isNetworkAvailable(MapNotificationActivity.this)) {

                            new ConfirmBlock().execute("inactive");
                        } else {
                            CommonMethods.wirelessDialog(MapNotificationActivity.this);
                        }

                    }
                });

                dontKnowUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        finish();

                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(MapNotificationActivity.this);
                builder.setMessage(i.getStringExtra("cause").trim().substring(0, 1).toUpperCase() + i.getStringExtra("cause").trim().substring(1).toLowerCase() + " at " + getIntent().getStringExtra("address").trim() + ". We saw that you are in vicinity. Please confirm and help your fellow travellers.")
                        .setTitle(i.getStringExtra("cause").trim().substring(0, 1).toUpperCase() + i.getStringExtra("cause").trim().substring(1).toLowerCase());
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        googleMap.setMyLocationEnabled(true);

        if (cause.contentEquals("landslide")) {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.valueOf(location.trim().split(",")[0].trim()), Double.valueOf(location.trim().split(",")[1].trim()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_landslide)));

        } else if (cause.contentEquals("accident")) {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.valueOf(location.trim().split(",")[0].trim()), Double.valueOf(location.trim().split(",")[1].trim()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_accident)));

        } else if (cause.contentEquals("traffic")) {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.valueOf(location.trim().split(",")[0].trim()), Double.valueOf(location.trim().split(",")[1].trim()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_traffic)));

        } else if (cause.contentEquals("construction")) {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.valueOf(location.trim().split(",")[0].trim()), Double.valueOf(location.trim().split(",")[1].trim()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_construction)));

        } else if (cause.contentEquals("other")) {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.valueOf(location.trim().split(",")[0].trim()), Double.valueOf(location.trim().split(",")[1].trim()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_options)));

        }

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(location.trim().split(",")[0].trim()), Double.valueOf(location.trim().split(",")[1].trim())), 14));
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());

                if (cause.contentEquals("landslide")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_landslide_big));
                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_landslide));
                        }
                    });
                } else if (cause.contentEquals("accident")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_accident_big));
                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_accident));
                        }
                    });
                } else if (cause.contentEquals("traffic")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_traffic_big));
                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_traffic));
                        }
                    });
                } else if (cause.contentEquals("construction")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_construction_big));
                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_construction));
                        }
                    });
                } else if (cause.contentEquals("other")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_other_big));
                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_options));
                        }
                    });
                }

                marker.showInfoWindow();

                return true;
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

            Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                    "fonts/Arvo-Regular.ttf");

            TextView tvCause = ((TextView) myContentsView.findViewById(R.id.tvRoadblockCause));

            if (cause != null) {
                tvCause.setText(cause.trim().toUpperCase());
            }
            tvCause.setTypeface(arvoRegular);
            TextView tvAddress = ((TextView) myContentsView.findViewById(R.id.tvRoadblockAddress));
            if (address != null) {
                tvAddress.setText("at  " + address.trim());
            }
            tvAddress.setTypeface(arvoRegular);
            TextView tvActiveStatus = ((TextView) myContentsView.findViewById(R.id.tvRoadblockActiveStatus));
            if (confidence != null) {
                tvActiveStatus.setText(confidence.trim());
            }
            tvActiveStatus.setTypeface(arvoRegular);

            return myContentsView;
        }
    }

    class ConfirmBlock extends AsyncTask<String, Void, Void> {

        String res;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MapNotificationActivity.this);
            pDialog.setMessage("Recording your Response...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            pDialog.dismiss();

            CommonMethods.showShortToast(MapNotificationActivity.this, "Thank you for your feedback.");

            finish();

        }

        @Override
        protected Void doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient();
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(MapNotificationActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(MapNotificationActivity.this, "secret",
                                "secretKey"), "utf-8");
                String block_id = URLEncoder.encode(blockId, "utf-8");
                String locationBlock = URLEncoder.encode(location, "utf-8");
                String confirmation_type = URLEncoder.encode(params[0], "utf-8");
                Request request = new Request.Builder()
                        .url(Constants.CONFIRMATION_URL + email + "&block_id=" + block_id + "&location=" + locationBlock + "&secret_key=" + secret + "&confirmation_type=" + confirmation_type)
                        .build();

                Response response = client.newCall(request).execute();
                res = response.body().string();
                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String success = jsonObj.getString("success").trim();
                    if (success.contentEquals("yes")) {
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


            return null;
        }
    }

}
