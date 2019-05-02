package in.wadersgroup.companion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

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

/**
 * Created by romil_wadersgroup on 24/2/16.
 */
public class CityBasedActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected Location mLastLocation;
    protected static final String TAG = "MainActivity";
    List<HashMap<String, String>> touristArray = new ArrayList<>();
    GoogleApiClient mGoogleApiClient;
    TextView errorText;
    ImageView nothingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 21) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(getResources().getColor(R.color.statusbar));
            }
        }

        setContentView(R.layout.activity_city_based);

        if (CommonMethods.isLocationAvailable(CityBasedActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(CityBasedActivity.this);
        }

        if (CommonMethods.isNetworkAvailable(CityBasedActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(CityBasedActivity.this);
        }

        buildGoogleApiClient();

        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        errorText = (TextView) findViewById(R.id.tvError);
        nothingImage = (ImageView) findViewById(R.id.ivNothing);
        errorText.setTypeface(arvoRegular);
        errorText.setVisibility(View.GONE);
        nothingImage.setVisibility(View.GONE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("EXPLORE");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar_title.setTypeface(arvoRegular);


    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    boolean checker = false;

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (!checker) {

            if (CommonMethods.isNetworkAvailable(CityBasedActivity.this)) {
                if (CommonMethods.isLocationAvailable(CityBasedActivity.this)) {
                    new FetchTouristPlaces().execute(mLastLocation);
                } else {
                    CommonMethods.locationDialog(CityBasedActivity.this);
                }
            } else {
                CommonMethods.wirelessDialog(CityBasedActivity.this);
            }

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    class FetchTouristPlaces extends AsyncTask<Location, Void, Void> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            checker = true;
            pDialog = new ProgressDialog(CityBasedActivity.this);
            pDialog.setMessage("Getting Awesome Places...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            ListView listView = (ListView) findViewById(R.id.listTouristPlaces);
            if(touristArray.size()!=0) {
                listView.setAdapter(new CardAdapter(CityBasedActivity.this, touristArray));

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {

                        Intent i = new Intent(CityBasedActivity.this, PlaceDetailsBigActivity.class);
                        i.putExtra("name", touristArray.get(position).get("name"));
                        i.putExtra("address", touristArray.get(position).get("address"));
                        i.putExtra("opening_time", touristArray.get(position).get("opening_time"));
                        i.putExtra("closing_time", touristArray.get(position).get("closing_time"));
                        i.putExtra("about", touristArray.get(position).get("about"));
                        i.putExtra("rating", touristArray.get(position).get("public_rating"));
                        i.putExtra("image_uri", touristArray.get(position).get("image_uri"));
                        i.putExtra("location", touristArray.get(position).get("location"));
                        i.putExtra("place_id", touristArray.get(position).get("place_id"));
                        startActivity(i);
                    }
                });
            }else{
                errorText.setVisibility(View.VISIBLE);
                nothingImage.setVisibility(View.VISIBLE);
                errorText.setText("Sorry! Currently we do not have any Places in your Area." +
                        "We are working very hard to get them. Please keep checking regularly.");
            }

            pDialog.dismiss();

        }

        @Override
        protected Void doInBackground(Location... params) {
            String res;

            try {
                String locationString = URLEncoder.encode(String.valueOf(params[0].getLatitude()) + "," + String.valueOf(params[0].getLongitude()), "utf-8");
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(CityBasedActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(CityBasedActivity.this, "secret",
                                "secretKey"), "utf-8");
                String format = URLEncoder.encode("tourist_places", "utf-8");

                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                // Put URL Below
                HttpGet httpPost = new HttpGet(Constants.GO_LIVE_URL + email
                        + "&secret_key=" + secret
                        + "&location=" + locationString + "&format=" + format);

                System.out.println(Constants.GO_LIVE_URL + email
                        + "&secret_key=" + secret
                        + "&location=" + locationString + "&format=" + format);

                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                res = EntityUtils.toString(response.getEntity());

                if (res != null) {
                    try {

                        // Check for Success message in the response here

                        System.out.println("Response of JSON:" + res);
                        System.out.println("URL: " + Constants.GO_LIVE_URL + email
                                + "&secret_key=" + secret
                                + "&location=" + locationString + "&format=" + format);
                        JSONObject jsonObj = new JSONObject(res.trim()); // JSONException

                        String success = jsonObj.getString("success").trim();
                        if (success.contentEquals("yes")) {

                            JSONObject myJson = jsonObj.getJSONObject("response");
                            JSONArray touristJsonArray = myJson.getJSONArray("tourist_places");

                            for (int i = 0; i < touristJsonArray.length(); i++) {
                                JSONObject placeObject = touristJsonArray
                                        .getJSONObject(i);
                                String touristName = placeObject
                                        .getString("name");
                                String touristLocation = placeObject
                                        .getString("location");
                                String touristTypes = placeObject
                                        .getString("type");
                                String touristAbout = placeObject.getString("about");
                                String touristAddress = placeObject
                                        .getString("address");
                                String touristOpeningTime = placeObject
                                        .getString("opening_time");
                                String touristClosingTime = placeObject
                                        .getString("closing_time");
                                String touristRating = placeObject
                                        .getString("public_rating");
                                String touristPlaceId = placeObject
                                        .getString("place_id");
                                JSONArray images = placeObject.getJSONArray("images");

                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("name", touristName);
                                hm.put("location", touristLocation);
                                hm.put("type", touristTypes);
                                hm.put("address", touristAddress);
                                hm.put("about", touristAbout);
                                hm.put("opening_time", touristOpeningTime);
                                hm.put("closing_time", touristClosingTime);
                                hm.put("public_rating", touristRating);
                                hm.put("place_id", touristPlaceId);
                                hm.put("image_uri", Constants.staticURLBitches + images.get(0));

                                touristArray.add(hm);


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
                AnalyticsApplication.getInstance().trackException(e);
                // TODO Auto-generated catch block
                e.printStackTrace();

            } catch (ClientProtocolException e) {
                AnalyticsApplication.getInstance().trackException(e);
                // TODO Auto-generated catch block
                e.printStackTrace();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();

            }

            return null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}
