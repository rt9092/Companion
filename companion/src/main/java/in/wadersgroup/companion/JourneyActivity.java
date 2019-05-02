package in.wadersgroup.companion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class JourneyActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * GoogleApiClient wraps our service connection to Google Play Services and
     * provides access to the user's sign in state as well as the Google's APIs.
     */
    protected GoogleApiClient mGoogleApiClient;

    AutoCompleteTextView destinationAuto;
    AutoCompleteTextView sourceAuto;

    private PlaceAutocompleteAdapter mAdapter;

    private static final LatLngBounds BOUNDS_SIKKIM = new LatLngBounds(
            new LatLng(26.526078, 88.549850), new LatLng(28.160597, 88.511398));

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using
        // AutoManage
        // functionality, which automatically sets up the API client to handle
        // Activity lifecycle
        // events. If your activity does not extend FragmentActivity, make sure
        // to call connect()
        // and disconnect() explicitly.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 55, this).addApi(Places.GEO_DATA_API)
                .build();

        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if (sdkVersion >= 21) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.statusbar));
        }

        setContentView(R.layout.activity_autocomplete_journey);

        if (CommonMethods.isNetworkAvailable(JourneyActivity.this)) {

        } else {
            CommonMethods.wirelessDialog(JourneyActivity.this);
        }

        /*if (CommonMethods.isLocationAvailable(JourneyActivity.this)) {

        } else {
            CommonMethods.locationDialog(JourneyActivity.this);
        }*/

        TextView recentHeading = (TextView) findViewById(R.id.tvRecent);
        TextView recent1 = (TextView) findViewById(R.id.tvRecent1);
        TextView recent2 = (TextView) findViewById(R.id.tvRecent2);
        TextView recent3 = (TextView) findViewById(R.id.tvRecent3);
        TextView recent4 = (TextView) findViewById(R.id.tvRecent4);

        recentHeading.setVisibility(View.GONE);
        recent1.setVisibility(View.GONE);
        recent2.setVisibility(View.GONE);
        recent3.setVisibility(View.GONE);
        recent4.setVisibility(View.GONE);

        String recent = CommonMethods.readStringPreference(JourneyActivity.this, "recent_searches", "recent");
        if (recent.contentEquals("")) {
        } else {
            String[] recentsSearch = recent.split("-----");
            recentHeading.setVisibility(View.VISIBLE);
            if (recentsSearch.length == 1) {
                recent1.setVisibility(View.VISIBLE);
                recent1.setText(recentsSearch[0]);
            } else if (recentsSearch.length == 2) {
                recent1.setVisibility(View.VISIBLE);
                recent1.setText(recentsSearch[0]);
                recent2.setVisibility(View.VISIBLE);
                recent2.setText(recentsSearch[1]);
            } else if (recentsSearch.length == 3) {
                recent1.setVisibility(View.VISIBLE);
                recent1.setText(recentsSearch[0]);
                recent2.setVisibility(View.VISIBLE);
                recent2.setText(recentsSearch[1]);
                recent3.setVisibility(View.VISIBLE);
                recent3.setText(recentsSearch[2]);
            } else if (recentsSearch.length == 4) {
                recent1.setVisibility(View.VISIBLE);
                recent1.setText(recentsSearch[0]);
                recent2.setVisibility(View.VISIBLE);
                recent2.setText(recentsSearch[1]);
                recent3.setVisibility(View.VISIBLE);
                recent3.setText(recentsSearch[2]);
                recent4.setVisibility(View.VISIBLE);
                recent4.setText(recentsSearch[3]);
            }
        }

        Button safeRoute = (Button) findViewById(R.id.bSafeRoute);

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("JOURNEY DETAILS");

        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        safeRoute.setTypeface(arvoRegular);

        toolbar_title.setTypeface(arvoRegular);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        sourceAuto = (AutoCompleteTextView) findViewById(R.id.autocomplete_places_source);
        sourceAuto.setOnItemClickListener(mAutocompleteClickListener);
        sourceAuto.setTypeface(arvoRegular);
        sourceAuto.setText("My Location");
        destinationAuto = (AutoCompleteTextView) findViewById(R.id.autocomplete_places_destination);
        destinationAuto.setOnItemClickListener(mAutocompleteClickListener);
        destinationAuto.setTypeface(arvoRegular);

        // Set up the adapter that will retrieve suggestions from the Places Geo
        // Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(this,
                android.R.layout.simple_spinner_dropdown_item,
                mGoogleApiClient, BOUNDS_SIKKIM, null);
        destinationAuto.setAdapter(mAdapter);
        sourceAuto.setAdapter(mAdapter);

        sourceAuto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                sourceAuto.setText("");
            }
        });

        destinationAuto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                destinationAuto.setText("");
            }
        });

        safeRoute.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (!sourceAuto.getText().toString().trim().contentEquals("")) {
                    if (!destinationAuto.getText().toString().trim()
                            .contentEquals("")) {
                        if (sourceAuto.getText().toString().trim().contentEquals("My Location")) {
                            if (CommonMethods.isLocationAvailable(JourneyActivity.this)) {
                                Intent i = new Intent(JourneyActivity.this,
                                        SafeRoadActivity.class);

                                i.putExtra("source", sourceAuto.getText().toString()
                                        .trim());
                                i.putExtra("destination", destinationAuto.getText()
                                        .toString().trim());

                                String recentSearches = CommonMethods.readStringPreference(JourneyActivity.this, "recent_searches", "recent");
                                if (!recentSearches.contentEquals("")) {
                                    String[] recents = recentSearches.split("-----");
                                    if (recents.length < 4) {
                                        recentSearches = recentSearches + destinationAuto.getText().toString().trim() + ",";
                                        CommonMethods.createStringPreference(JourneyActivity.this, "recent_searches", "recent", recentSearches);
                                    } else {
                                        String recentSearchesNew = recentSearches.replace(recentSearches.split("-----")[3], destinationAuto.getText().toString().trim() + ",");
                                        CommonMethods.createStringPreference(JourneyActivity.this, "recent_searches", "recent", recentSearchesNew);
                                    }
                                }

                                startActivity(i);
                            } else {
                                CommonMethods.locationDialog(JourneyActivity.this);
                            }
                        } else {
                            Intent i = new Intent(JourneyActivity.this,
                                    SafeRoadActivity.class);

                            i.putExtra("source", sourceAuto.getText().toString()
                                    .trim());
                            i.putExtra("destination", destinationAuto.getText()
                                    .toString().trim());

                            String recentSearches = CommonMethods.readStringPreference(JourneyActivity.this, "recent_searches", "recent");
                            if (!recentSearches.contentEquals("")) {
                                String[] recents = recentSearches.split("-----");
                                if (recents.length < 4) {
                                    recentSearches = recentSearches + destinationAuto.getText().toString().trim() + ",";
                                    CommonMethods.createStringPreference(JourneyActivity.this, "recent_searches", "recent", recentSearches);
                                } else {
                                    String recentSearchesNew = recentSearches.replace(recentSearches.split("-----")[3], destinationAuto.getText().toString().trim() + ",");
                                    CommonMethods.createStringPreference(JourneyActivity.this, "recent_searches", "recent", recentSearchesNew);
                                }
                            }

                            startActivity(i);
                        }
                    } else {
                        CommonMethods.showShortToast(JourneyActivity.this,
                                "Please enter Destination");
                    }
                } else {
                    CommonMethods.showShortToast(JourneyActivity.this,
                            "Please enter Source");
                }

            }
        });

    }

    /**
     * Listener that handles selections from suggestions from the
     * AutoCompleteTextView that displays Place suggestions. Gets the place id
     * of the selected item and issues a request to the Places Geo Data API to
     * retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById
     * (com.google.android.gms.common.api.GoogleApiClient, String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            /*
             * Retrieve the place ID of the selected item from the Adapter. The
			 * adapter stores each Place suggestion in a PlaceAutocomplete
			 * object from which we read the place ID.
			 */
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter
                    .getItem(position);
            final String placeId = String.valueOf(item.placeId);

			/*
             * Issue a request to the Places Geo Data API to retrieve a Place
			 * object with additional details about the place.
			 */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            /*Toast.makeText(getApplicationContext(),
                    "Clicked: " + item.description, Toast.LENGTH_SHORT).show();*/
            // GET DETAILS FOR SELECTED ITEM
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the
     * first place result in the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully

                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            // GET PLACE DETAILS HERE

            places.release();
        }
    };

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO Auto-generated method stub

        // TODO(Developer): Check error code and notify the user of error state
        // and resolution.
        Toast.makeText(
                this,
                "Could not connect to Google API Client: Error "
                        + connectionResult.getErrorCode(), Toast.LENGTH_SHORT)
                .show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}
