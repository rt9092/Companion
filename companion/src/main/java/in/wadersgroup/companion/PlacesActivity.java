package in.wadersgroup.companion;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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

public class PlacesActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    GoogleMap map;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    HashMap<String, String> weakHashMap = new HashMap<String, String>();

    @Override
    protected void onCreate(@Nullable Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);

        setContentView(R.layout.activity_places);

        if (CommonMethods.isNetworkAvailable(PlacesActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(PlacesActivity.this);

        }

        if (CommonMethods.isLocationAvailable(PlacesActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(PlacesActivity.this);

        }

        // Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Getting Map for the SupportMapFragment
        map = fm.getMap();

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(final Marker marker) {
                // TODO Auto-generated method stub

                String placeName = weakHashMap.get(marker.getId()).trim()
                        .split("----")[0].trim();
                String placeAddress = weakHashMap.get(marker.getId()).trim()
                        .split("----")[1].trim();
                String placeTypes = weakHashMap.get(marker.getId()).trim()
                        .split("----")[2].trim();
                String placePerson = weakHashMap.get(marker.getId()).trim()
                        .split("----")[3].trim();
                String placeNumber = weakHashMap.get(marker.getId()).trim()
                        .split("----")[4].trim();
                String placeAbout = weakHashMap.get(marker.getId()).trim()
                        .split("----")[5].trim();

                if (placeTypes.contentEquals("auto_repair")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_auto_repair_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_auto_repair));
                        }
                    });

                } else if (placeTypes.contentEquals("hotel")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_hotel_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_hotel));
                        }
                    });

                } else if (placeTypes.contentEquals("restaurant")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_restaurant_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_restaurant));
                        }
                    });

                } else if (placeTypes.contentEquals("atm")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_atm_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_atm));
                        }
                    });

                } else if (placeTypes.contentEquals("gas_station")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_petrol_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_petrol));
                        }
                    });

                } else if (placeTypes.contentEquals("bank")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_bank_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_bank));
                        }
                    });

                } else if (placeTypes.contentEquals("toilet")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_toilet_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_toilet));
                        }
                    });

                } else if (placeTypes.contentEquals("pharmacy")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_pharmacy_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_pharmacy));
                        }
                    });

                } else if (placeTypes.contentEquals("hospital")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_hospital_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_hospital));
                        }
                    });

                } else if (placeTypes.contentEquals("police")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_police_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_police));
                        }
                    });

                } else if (placeTypes.contentEquals("taxi_stand")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_taxi_stand_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_taxi_stand));
                        }
                    });

                } else if (placeTypes.contentEquals("bus_station")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_bus_station_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_bus_stop));
                        }
                    });

                } else if (placeTypes.contentEquals("train_station")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_rail_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_rail));
                        }
                    });

                } else if (placeTypes.contentEquals("airport")) {

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_airport_big));
                    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_airport));
                        }
                    });

                }

                startPlaceDetailsActivity(placeName, placeAddress, placePerson, placeNumber, placeAbout);

                System.out.println("Info Window: " + marker.getId());
                return true;
            }
        });

        // map.setOnMarkerClickListener(new OnMarkerClickListener() {
        //
        // @Override
        // public boolean onMarkerClick(Marker marker) {
        // // TODO Auto-generated method stub
        //
        // String placeName = weakHashMap.get(marker.getId()).trim()
        // .split("----")[0].trim();
        // String placeAddress = weakHashMap.get(marker.getId()).trim()
        // .split("----")[1].trim();
        // ;
        // String placeTypes = weakHashMap.get(marker.getId()).trim()
        // .split("----")[2].trim();
        //
        // CommonMethods.showShortToast(PlacesActivity.this, placeName
        // + "\n" + placeAddress + "\n" + placeTypes);
        //
        // return false;
        // }
        // });

        buildGoogleApiClient();

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

    public class FetchPlaces extends AsyncTask<String, Void, Void> {

        ProgressDialog pDialog;
        String res = "";
        List<HashMap<String, String>> placesArray = new ArrayList<HashMap<String, String>>();

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            pDialog = new ProgressDialog(PlacesActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {

            // TODO Auto-generated method stub

            try {
                String queryString = URLEncoder.encode(params[0], "utf-8");
                String locationString = URLEncoder.encode(params[1], "utf-8");
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(PlacesActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(PlacesActivity.this, "secret",
                                "secretKey"), "utf-8");
                String format = URLEncoder.encode(params[2], "utf-8");

                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                // Put URL Below
                HttpGet httpPost = new HttpGet(Constants.PLACES_URL + email
                        + "&query=" + queryString + "&secret_key=" + secret
                        + "&location=" + locationString + "&format=" + format);

                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                res = EntityUtils.toString(response.getEntity());

                if (res != null) {
                    try {

                        // Check for Success message in the response here

                        System.out.println("Response of JSON:" + res);
                        JSONObject jsonObj = new JSONObject(res.trim()); // JSONException

                        String success = jsonObj.getString("success").trim();
                        if (success.contentEquals("yes")) {

                            JSONObject myJson = jsonObj.getJSONObject("response");
                            JSONArray myJSONArray = myJson.getJSONArray("amenities");

                            for (int i = 0; i < myJSONArray.length(); i++) {
                                JSONObject placeObject = myJSONArray
                                        .getJSONObject(i);
                                String placeName = placeObject
                                        .getString("name");
                                String placeLocation = placeObject
                                        .getString("location");
                                String placeTypes = placeObject
                                        .getString("type");
                                String placeAbout = placeObject.getString("about");
                                String placeContactPerson = placeObject.getString("contact_person");
                                String placeContactNumber = placeObject.getString("contact");
                                String placeAddress = placeObject
                                        .getString("address");

                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("name", placeName);
                                hm.put("location", placeLocation);
                                hm.put("type", placeTypes);
                                hm.put("address", placeAddress);
                                hm.put("contact_person", placeContactPerson);
                                hm.put("contact_number", placeContactNumber);
                                hm.put("about", placeAbout);

                                placesArray.add(hm);

                            }

                        } else {
                            // Flag to make a toast for no results
                        }

                    } catch (JSONException e) {
                        AnalyticsApplication.getInstance().trackException(e);
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ServiceHandler",
                            "Couldn't get any data from the URL");
                }

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

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            // Render Places on the MAP here.
            if (placesArray.size() != 0) {

                for (int i = 0; i < placesArray.size(); i++) {

                    HashMap<String, String> place = placesArray.get(i);

                    String placeName = place.get("name");
                    String placeLocation = place.get("location");
                    String placeTypes = place.get("type");
                    String placeAddress = place.get("address");
                    String placeContactPerson = place.get("contact_person");
                    String placeContactNumber = place.get("contact_number");
                    String placeAbout = place.get("about");

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mLastLocation.getLatitude(), mLastLocation
                                    .getLongitude()), 14));

                    LatLng position = new LatLng(Double.valueOf(placeLocation
                            .trim().split(", ")[0].trim()),
                            Double.valueOf(placeLocation.trim().split(", ")[1]
                                    .trim()));

                    int resource = 0;

                    if (placeTypes.contentEquals("auto_repair")) {

                        resource = R.drawable.ic_pointer_auto_repair;

                    } else if (placeTypes.contentEquals("hotel")) {

                        resource = R.drawable.ic_pointer_hotel;

                    } else if (placeTypes.contentEquals("restaurant")) {

                        resource = R.drawable.ic_pointer_restaurant;

                    } else if (placeTypes.contentEquals("atm")) {

                        resource = R.drawable.ic_pointer_atm;

                    } else if (placeTypes.contentEquals("gas_station")) {

                        resource = R.drawable.ic_pointer_petrol;

                    } else if (placeTypes.contentEquals("bank")) {

                        resource = R.drawable.ic_pointer_bank;

                    } else if (placeTypes.contentEquals("toilet")) {

                        resource = R.drawable.ic_pointer_toilet;

                    } else if (placeTypes.contentEquals("pharmacy")) {

                        resource = R.drawable.ic_pointer_pharmacy;

                    } else if (placeTypes.contentEquals("hospital")) {

                        resource = R.drawable.ic_pointer_hospital;

                    } else if (placeTypes.contentEquals("police")) {

                        resource = R.drawable.ic_pointer_police;

                    } else if (placeTypes.contentEquals("taxi_stand")) {

                        resource = R.drawable.ic_pointer_taxi_stand;

                    } else if (placeTypes.contentEquals("bus_station")) {

                        resource = R.drawable.ic_pointer_bus_stop;

                    } else if (placeTypes.contentEquals("train_station")) {

                        resource = R.drawable.ic_pointer_rail;

                    } else if (placeTypes.contentEquals("airport")) {

                        resource = R.drawable.ic_pointer_airport;

                    }

                    Marker mMarker = map.addMarker(new MarkerOptions()
                            .position(position).title(placeName)
                            .snippet(placeAddress).icon(BitmapDescriptorFactory.fromResource(resource)));

                    weakHashMap.put(mMarker.getId(), placeName + "----"
                            + placeAddress + "----" + placeTypes + "----" + placeContactPerson
                            + "----" + placeContactNumber + "----" + placeAbout + "----" + "BS");

                    System.out.println("Marker ID: " + mMarker.getId());

                }
            } else {
                CommonMethods.showShortToast(PlacesActivity.this, "No Places Available. We are trying" +
                        " hard to get all the places. Please bear with us!");
                finish();
            }
            pDialog.dismiss();

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // TODO Auto-generated method stub

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {

            String latitude = String.valueOf(mLastLocation.getLatitude());
            String longitude = String.valueOf(mLastLocation.getLongitude());
            String location = latitude + ", " + longitude;

            // Get the intent, verify the action and get the query
            Intent intent = getIntent();
            String format = "";
            String query;
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                query = intent.getStringExtra(SearchManager.QUERY);
                System.out.println("Query: " + query);
                JSONObject amenitiesFormat = new JSONObject();
                try {
                    // Putting data in JSON Object
                    amenitiesFormat.put("amenities", "all");
                    format = amenitiesFormat.toString();
                } catch (JSONException e) {
                    AnalyticsApplication.getInstance().trackException(e);
                    e.printStackTrace();
                }
                if (CommonMethods.isNetworkAvailable(PlacesActivity.this)) {
                    new FetchPlaces().execute(query, location, format);
                } else {
                    CommonMethods.wirelessDialog(PlacesActivity.this);

                }

            }
            String type = intent.getStringExtra("result");
            if (type != null) {
                if (!type.isEmpty()) {
                    query = "all";
                    JSONObject amenitiesFormat = new JSONObject();
                    try {

                        // Putting data in JSON Object

                        amenitiesFormat.put("amenities", type);
                        format = amenitiesFormat.toString();
                    } catch (JSONException e) {
                        AnalyticsApplication.getInstance().trackException(e);
                        e.printStackTrace();
                    }

                    if (CommonMethods.isNetworkAvailable(PlacesActivity.this)) {

                        new FetchPlaces().execute(query, location, format);
                    } else {
                        CommonMethods.wirelessDialog(PlacesActivity.this);

                    }
                } else {
                    CommonMethods.showShortToast(PlacesActivity.this, "Please select a Quick Link.");
                    finish();
                }
            }
        }

    }

    @Override
    public void onConnectionSuspended(int cause) {
        // TODO Auto-generated method stub

        mGoogleApiClient.connect();

    }

    public void startPlaceDetailsActivity(String name, String address, String contactPerson, String contactNumber, String about) {
        Intent i = new Intent(PlacesActivity.this, PlaceDetailsActivity.class);
        i.putExtra("name", name);
        i.putExtra("address", address);
        i.putExtra("contactPerson", contactPerson);
        i.putExtra("contactNumber", contactNumber);
        i.putExtra("about", about);
        startActivity(i);
    }

}
