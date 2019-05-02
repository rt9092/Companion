package in.wadersgroup.companion;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import com.squareup.picasso.Picasso;

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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by romil_wadersgroup on 24/2/16.
 */
public class PlaceDetailsBigActivity extends AppCompatActivity {

    ProgressBar progressNearby;
    String location;
    CardView nearbyCard;
    LinearLayout nearby1, nearby2, nearby3;
    Typeface arvoRegular;
    String touristImageUri;
    String placeID;
    String touristRating;
    TextView tvRating;
    LinearLayout rating;

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

        setContentView(R.layout.activity_place_details_big);

        if (CommonMethods.isNetworkAvailable(PlaceDetailsBigActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(PlaceDetailsBigActivity.this);

        }

        /*if (CommonMethods.isLocationAvailable(PlaceDetailsBigActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(PlaceDetailsBigActivity.this);

        }*/

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.TOP)
                .build();
        Slidr.attach(this, config);
        progressNearby = (ProgressBar) findViewById(R.id.pbNearby);
        nearbyCard = (CardView) findViewById(R.id.cardNearby);

        nearby1 = (LinearLayout) findViewById(R.id.llNearby1);
        nearby2 = (LinearLayout) findViewById(R.id.llNearby2);
        nearby3 = (LinearLayout) findViewById(R.id.llNearby3);
        rating = (LinearLayout) findViewById(R.id.llRatings);

        nearby1.setVisibility(View.GONE);
        nearby2.setVisibility(View.GONE);
        nearby3.setVisibility(View.GONE);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        TextView tvOpeningClosing = (TextView) findViewById(R.id.tvTPOpeningClosingTime);
        TextView tvAddress = (TextView) findViewById(R.id.tvTouristPlaceAddress);
        TextView tvAbout = (TextView) findViewById(R.id.tvTouristPlaceAbout);
        tvRating = (TextView) findViewById(R.id.tvTouristPlaceRating);

        Intent i = getIntent();
        String touristName = i.getStringExtra("name");
        String touristAddress = i.getStringExtra("address");
        String touristOpeningTime = i.getStringExtra("opening_time");
        String touristClosingTime = i.getStringExtra("closing_time");
        String touristAbout = i.getStringExtra("about");
        touristRating = i.getStringExtra("rating");
        touristImageUri = i.getStringExtra("image_uri");
        location = i.getStringExtra("location");
        placeID = i.getStringExtra("place_id");

        System.out.println("Data: " + touristRating + " " + touristImageUri);

        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Bold.ttf");
        arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");
        tvOpeningClosing.setTypeface(arvoRegular);
        tvAddress.setTypeface(arvoRegular);
        tvAbout.setTypeface(arvoRegular);
        tvRating.setTypeface(arvoRegular);
        int openingTime = 0, closingTime = 0;
        String openingFinal, closingFinal;

        if (touristOpeningTime.trim().contentEquals("00:00:00")) {

            tvOpeningClosing.setVisibility(View.GONE);

        } else {

            if (Integer.valueOf(touristOpeningTime.split(":")[0].trim()) > 12) {
                openingTime = Integer.valueOf(touristOpeningTime.split(":")[0].trim()) - 12;
                openingFinal = String.valueOf(openingTime) + ":" + touristOpeningTime.split(":")[1].trim() + " PM";
            } else {
                openingTime = Integer.valueOf(touristOpeningTime.split(":")[0].trim());
                openingFinal = String.valueOf(openingTime) + ":" + touristOpeningTime.split(":")[1].trim() + " AM";
            }
            if (Integer.valueOf(touristClosingTime.split(":")[0].trim()) > 12) {
                closingTime = Integer.valueOf(touristClosingTime.split(":")[0].trim()) - 12;
                closingFinal = String.valueOf(closingTime) + ":" + touristClosingTime.split(":")[1].trim() + " PM";
            } else {
                closingTime = Integer.valueOf(touristClosingTime.split(":")[0].trim());
                closingFinal = String.valueOf(closingTime) + ":" + touristClosingTime.split(":")[1].trim() + " AM";
            }
            tvOpeningClosing.setText(openingFinal + "   to   " + closingFinal);
        }

        collapsingToolbar.setTitle(touristName);
        tvAddress.setText(touristAddress);
        tvAbout.setText(touristAbout);

        FloatingActionButton fabRating = (FloatingActionButton) findViewById(R.id.fabRating);

        /*BadgeView badge = new BadgeView(this, fabRating);
        badge.setText("3.7");
        badge.setBadgeBackgroundColor(R.color.toolbar);
        badge.show();*/

        fabRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog rankDialog = new Dialog(PlaceDetailsBigActivity.this, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
                rankDialog.setContentView(R.layout.rating_dialog);
                rankDialog.setCancelable(true);
                final RatingBar ratingBar = (RatingBar) rankDialog.findViewById(R.id.dialog_ratingbar);
                Button rateIt = (Button) rankDialog.findViewById(R.id.bRateIt);
                rateIt.setTypeface(arvoRegular);
                rateIt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (CommonMethods.isNetworkAvailable(PlaceDetailsBigActivity.this)) {

                            new PublishRatings().execute(String.valueOf(ratingBar.getRating()));
                            rankDialog.dismiss();
                            CommonMethods.showShortToast(PlaceDetailsBigActivity.this, String.valueOf(ratingBar.getRating()));
                        } else {
                            CommonMethods.wirelessDialog(PlaceDetailsBigActivity.this);

                        }
                    }
                });
                rankDialog.show();
            }
        });

        loadBackdrop(touristImageUri);

        if (CommonMethods.isNetworkAvailable(PlaceDetailsBigActivity.this)) {

            new FetchNearby().execute(location);
        } else {
            CommonMethods.wirelessDialog(PlaceDetailsBigActivity.this);

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!touristRating.contentEquals("unrated")) {
            tvRating.setText(touristRating + " Stars.");
        } else {
            rating.setVisibility(View.GONE);
        }

    }

    private void loadBackdrop(String imageUri) {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        //Glide.with(this).load(R.drawable.tathagata).centerCrop().into(imageView);
        System.out.println("Image URI: " + imageUri);
        Picasso.with(PlaceDetailsBigActivity.this)
                .load(imageUri)
                .placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)
                .into(imageView);
        /*ImageLoader image = new ImageLoader(PlaceDetailsBigActivity.this);
        image.DisplayImage(imageUri, imageView);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    List<HashMap<String, String>> touristArray = new ArrayList<HashMap<String, String>>();

    class FetchNearby extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressNearby.setVisibility(View.GONE);

            System.out.println("Tourist Array Size: " + touristArray.size());

            if (touristArray.size() != 0) {

                if (touristArray.size() >= 3) {
                    TextView nearbyText1 = (TextView) findViewById(R.id.tvNearbyPlace1);
                    nearbyText1.setText(touristArray.get(0).get("name"));
                    TextView nearbyText2 = (TextView) findViewById(R.id.tvNearbyPlace2);
                    nearbyText2.setText(touristArray.get(1).get("name"));
                    TextView nearbyText3 = (TextView) findViewById(R.id.tvNearbyPlace3);
                    nearbyText3.setText(touristArray.get(2).get("name"));
                    nearbyText1.setTypeface(arvoRegular);
                    nearbyText2.setTypeface(arvoRegular);
                    nearbyText3.setTypeface(arvoRegular);
                    nearby1.setVisibility(View.VISIBLE);
                    nearby2.setVisibility(View.VISIBLE);
                    nearby3.setVisibility(View.VISIBLE);
                    nearby1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(PlaceDetailsBigActivity.this, PlaceDetailsBigActivity.class);
                            i.putExtra("name", touristArray.get(0).get("name"));
                            i.putExtra("address", touristArray.get(0).get("address"));
                            i.putExtra("opening_time", touristArray.get(0).get("opening_time"));
                            i.putExtra("closing_time", touristArray.get(0).get("closing_time"));
                            i.putExtra("about", touristArray.get(0).get("about"));
                            i.putExtra("rating", touristArray.get(0).get("public_rating"));
                            i.putExtra("image_uri", touristArray.get(0).get("image_uri"));
                            i.putExtra("location", touristArray.get(0).get("location"));
                            i.putExtra("place_id", touristArray.get(0).get("place_id"));
                            startActivity(i);
                        }
                    });
                    nearby2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(PlaceDetailsBigActivity.this, PlaceDetailsBigActivity.class);
                            i.putExtra("name", touristArray.get(1).get("name"));
                            i.putExtra("address", touristArray.get(1).get("address"));
                            i.putExtra("opening_time", touristArray.get(1).get("opening_time"));
                            i.putExtra("closing_time", touristArray.get(1).get("closing_time"));
                            i.putExtra("about", touristArray.get(1).get("about"));
                            i.putExtra("rating", touristArray.get(1).get("public_rating"));
                            i.putExtra("image_uri", touristArray.get(1).get("image_uri"));
                            i.putExtra("location", touristArray.get(1).get("location"));
                            i.putExtra("place_id", touristArray.get(1).get("place_id"));
                            startActivity(i);
                        }
                    });
                    nearby3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(PlaceDetailsBigActivity.this, PlaceDetailsBigActivity.class);
                            i.putExtra("name", touristArray.get(2).get("name"));
                            i.putExtra("address", touristArray.get(2).get("address"));
                            i.putExtra("opening_time", touristArray.get(2).get("opening_time"));
                            i.putExtra("closing_time", touristArray.get(2).get("closing_time"));
                            i.putExtra("about", touristArray.get(2).get("about"));
                            i.putExtra("rating", touristArray.get(2).get("public_rating"));
                            i.putExtra("image_uri", touristArray.get(2).get("image_uri"));
                            i.putExtra("location", touristArray.get(2).get("location"));
                            i.putExtra("place_id", touristArray.get(2).get("place_id"));
                            startActivity(i);
                        }
                    });
                } else if (touristArray.size() == 2) {
                    TextView nearbyText1 = (TextView) findViewById(R.id.tvNearbyPlace1);
                    nearbyText1.setText(touristArray.get(0).get("name"));
                    TextView nearbyText2 = (TextView) findViewById(R.id.tvNearbyPlace2);
                    nearbyText2.setText(touristArray.get(1).get("name"));
                    nearbyText1.setTypeface(arvoRegular);
                    nearbyText2.setTypeface(arvoRegular);
                    nearby1.setVisibility(View.VISIBLE);
                    nearby2.setVisibility(View.VISIBLE);
                    nearby1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(PlaceDetailsBigActivity.this, PlaceDetailsBigActivity.class);
                            i.putExtra("name", touristArray.get(0).get("name"));
                            i.putExtra("address", touristArray.get(0).get("address"));
                            i.putExtra("opening_time", touristArray.get(0).get("opening_time"));
                            i.putExtra("closing_time", touristArray.get(0).get("closing_time"));
                            i.putExtra("about", touristArray.get(0).get("about"));
                            i.putExtra("rating", touristArray.get(0).get("public_rating"));
                            i.putExtra("image_uri", touristArray.get(0).get("image_uri"));
                            i.putExtra("location", touristArray.get(0).get("location"));
                            i.putExtra("place_id", touristArray.get(0).get("place_id"));
                            startActivity(i);
                        }
                    });
                    nearby2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(PlaceDetailsBigActivity.this, PlaceDetailsBigActivity.class);
                            i.putExtra("name", touristArray.get(1).get("name"));
                            i.putExtra("address", touristArray.get(1).get("address"));
                            i.putExtra("opening_time", touristArray.get(1).get("opening_time"));
                            i.putExtra("closing_time", touristArray.get(1).get("closing_time"));
                            i.putExtra("about", touristArray.get(1).get("about"));
                            i.putExtra("rating", touristArray.get(1).get("public_rating"));
                            i.putExtra("image_uri", touristArray.get(1).get("image_uri"));
                            i.putExtra("location", touristArray.get(1).get("location"));
                            i.putExtra("place_id", touristArray.get(1).get("place_id"));
                            startActivity(i);
                        }
                    });
                } else if (touristArray.size() == 1) {
                    TextView nearbyText1 = (TextView) findViewById(R.id.tvNearbyPlace1);
                    nearbyText1.setText(touristArray.get(0).get("name"));
                    nearbyText1.setTypeface(arvoRegular);
                    nearby1.setVisibility(View.VISIBLE);
                    nearby1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(PlaceDetailsBigActivity.this, PlaceDetailsBigActivity.class);
                            i.putExtra("name", touristArray.get(0).get("name"));
                            i.putExtra("address", touristArray.get(0).get("address"));
                            i.putExtra("opening_time", touristArray.get(0).get("opening_time"));
                            i.putExtra("closing_time", touristArray.get(0).get("closing_time"));
                            i.putExtra("about", touristArray.get(0).get("about"));
                            i.putExtra("rating", touristArray.get(0).get("public_rating"));
                            i.putExtra("image_uri", touristArray.get(0).get("image_uri"));
                            i.putExtra("location", touristArray.get(0).get("location"));
                            i.putExtra("place_id", touristArray.get(0).get("place_id"));
                            startActivity(i);
                        }
                    });
                }

            } else {
                // Make textView
                nearbyCard.setVisibility(View.GONE);
            }


        }

        @Override
        protected Void doInBackground(String... params) {

            String res;

            try {
                String locationString = URLEncoder.encode(params[0], "utf-8");
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(PlaceDetailsBigActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(PlaceDetailsBigActivity.this, "secret",
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

                        System.out.println("Nearby Response:" + res);
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

                                if (!touristLocation.trim().contentEquals(params[0])) {

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
    }

    class PublishRatings extends AsyncTask<String, Void, Void> {

        String res;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(PlaceDetailsBigActivity.this);
            pDialog.setMessage("Recording your Response...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            pDialog.dismiss();

        }

        @Override
        protected Void doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient();
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(PlaceDetailsBigActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(PlaceDetailsBigActivity.this, "secret",
                                "secretKey"), "utf-8");
                String placeId = URLEncoder.encode(placeID, "utf-8");
                String rating = URLEncoder.encode(params[0], "utf-8");
                Request request = new Request.Builder()
                        .url(Constants.RATING_URL + email + "&place_id=" + placeId + "&rating=" + rating + "&secret_key=" + secret)
                        .build();

                Response response = client.newCall(request).execute();
                res = response.body().string();
                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String success = jsonObj.getString("success").trim();
                    if (success.contentEquals("yes")) {
                        touristRating = jsonObj.getString("public_rating");
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
