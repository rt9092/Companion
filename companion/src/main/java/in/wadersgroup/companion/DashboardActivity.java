package in.wadersgroup.companion;

// (Activity) Main Screen of the application. All the Navigation Activities here.

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import java.util.Locale;

import util.ColorGenerator;

/**
 * @author Romil
 */
public class DashboardActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private DrawerLayout mDrawerLayout;
    private ShowcaseView showcaseView;
    private int counter = 0;
    Animation shrinkButton;
    Animation left2right;
    Animation right2left;
    Animation fadeOut, mapSlide, mapSlideRight;
    GoogleMap mMap;
    Typeface arvoRegular;
    boolean isNotInBounds;
    ProgressDialog pDialogLocation;
    int popUpFrequency = 0;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    CoordinatorLayout liveMap;
    CardView routeCard, contactCard, dangerCard;
    FloatingActionButton anonymousUpdate;
    Button goLive;

    private static final String TAG = "MainActivity";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 21) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.statusbar));
        }

        setContentView(R.layout.activity_dashboard);

        pDialogLocation = new ProgressDialog(DashboardActivity.this);
        pDialogLocation.setMessage("Getting ready for you...");
        pDialogLocation.setCancelable(false);

        /*if (CommonMethods.isLocationAvailable(DashboardActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(DashboardActivity.this);
        }*/
        if (CommonMethods.isNetworkAvailable(DashboardActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(DashboardActivity.this);
        }


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    //CommonMethods.showShortToast(context, context.getString(R.string.gcm_send_message));
                } else {
                    //CommonMethods.showShortToast(context, context.getString(R.string.token_error_message));
                }
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent1 = new Intent(DashboardActivity.this, RegistrationIntentService.class);
            startService(intent1);
        }


        buildGoogleApiClient();

        CommonMethods.createStringPreference(DashboardActivity.this, "recent_searches", "recent", "");

        startService(new Intent(DashboardActivity.this, AlarmService.class));

        //topLevelLayout = (RelativeLayout) findViewById(R.id.top_layout);

        liveMap = (CoordinatorLayout) findViewById(R.id.llDashboardMap);
        liveMap.setVisibility(View.GONE);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fabCancelLiveMap = (FloatingActionButton) findViewById(R.id.fabCancel);
        FloatingActionButton fabLiveMapFilters = (FloatingActionButton) findViewById(R.id.fabFilters);


        // For the Guide Translucent Layout

        if (isFirstTime()) {
            // showcaseView.hide();
        }

        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Bold.ttf");
        arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        goLive = (Button) findViewById(R.id.bGoLive);
        goLive.setTypeface(arvoBold);


        CommonMethods.createStringPreference(DashboardActivity.this,
                "recentSearch", "source", "none");
        CommonMethods.createStringPreference(DashboardActivity.this,
                "recentSearch", "destination", "none");

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("DASHBOARD");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawer_icon_new);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        TextView tvHeaderDstn = (TextView) findViewById(R.id.tvHeaderDestn);
        TextView tvDZones = (TextView) findViewById(R.id.tvStateZones);

        TextView tvContact = (TextView) findViewById(R.id.tvContact);

        anonymousUpdate = (FloatingActionButton) findViewById(R.id.fabAnonymousUpdate);

        routeCard = (CardView) findViewById(R.id.cardRoute);
        contactCard = (CardView) findViewById(R.id.cardContact);
        dangerCard = (CardView) findViewById(R.id.cardDangerZones);

        toolbar_title.setTypeface(arvoRegular);
        tvHeaderDstn.setTypeface(arvoRegular);
        tvDZones.setTypeface(arvoRegular);

        tvContact.setTypeface(arvoRegular);

        Button kailashMansarovar = (Button) findViewById(R.id.bKailashMansarovar);
        kailashMansarovar.setTypeface(arvoBold);
        kailashMansarovar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CommonMethods.showLongToast(DashboardActivity.this, "Kailash Mansarovar");
                startActivity(new Intent(DashboardActivity.this, YatraSpecialActivity.class));
            }
        });

        final LinearLayout llSafeRoute = (LinearLayout) findViewById(R.id.llSafeRoute);
        final LinearLayout llLiveRoad = (LinearLayout) findViewById(R.id.llLiveRoadblock);
        final LinearLayout llReportRoad = (LinearLayout) findViewById(R.id.llReportRoadblock);

        MaterialRippleLayout.on(llSafeRoute).rippleColor(Color.BLACK)
                .rippleAlpha(0.2F).rippleOverlay(false).rippleDuration(500)
                .create().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                startActivity(new Intent(DashboardActivity.this,
                        JourneyActivity.class));

            }
        });
        MaterialRippleLayout.on(llLiveRoad).rippleColor(Color.BLACK)
                .rippleAlpha(0.2F).rippleOverlay(false).rippleDuration(500)
                .create().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (CommonMethods.isLocationAvailable(DashboardActivity.this)) {

                    if (isNotInBounds) {
                        CommonMethods.commonDialog(DashboardActivity.this, "Currently this service is not" +
                                " operational in your region. We are working very hard to bring it " +
                                "to you. So please keep on checking the app periodically.");
                    } else {

                        startActivity(new Intent(DashboardActivity.this,
                                CityBasedActivity.class));
                    }
                } else {
                    CommonMethods.locationDialog(DashboardActivity.this);
                }

            }
        });
        MaterialRippleLayout.on(llReportRoad).rippleColor(Color.BLACK)
                .rippleAlpha(0.2F).rippleOverlay(false).rippleDuration(500)
                .create().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (CommonMethods.isLocationAvailable(DashboardActivity.this)) {
                    startActivity(new Intent(DashboardActivity.this,
                            DataUploadActivity.class));
                } else {
                    CommonMethods.locationDialog(DashboardActivity.this);
                }

            }
        });

        anonymousUpdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (CommonMethods.isLocationAvailable(DashboardActivity.this)) {
                    startActivity(new Intent(DashboardActivity.this,
                            AnonymousUpdatesActivity.class));
                } else {
                    CommonMethods.locationDialog(DashboardActivity.this);
                }

            }
        });

        routeCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                // startActivity(new Intent(DashboardActivity.this,
                // RouteMapper.class));

                startActivity(new Intent(DashboardActivity.this,
                        JourneyActivity.class));

            }
        });

        contactCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                // TODO Auto-generated method stub

                if (CommonMethods.isLocationAvailable(DashboardActivity.this)) {
                    startActivity(new Intent(DashboardActivity.this,
                            DataUploadActivity.class));
                } else {
                    CommonMethods.locationDialog(DashboardActivity.this);
                }
            }
        });

        dangerCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (CommonMethods.isLocationAvailable(DashboardActivity.this)) {

                    if (isNotInBounds) {
                        CommonMethods.commonDialog(DashboardActivity.this, "Currently this service is not" +
                                " operational in your region. We are working very hard to bring it " +
                                "to you. So please keep on checking the app periodically.");
                    } else {

                        startActivity(new Intent(DashboardActivity.this,
                                CityBasedActivity.class));
                    }
                } else {
                    CommonMethods.locationDialog(DashboardActivity.this);
                }

            }
        });

        shrinkButton = AnimationUtils.loadAnimation(DashboardActivity.this,
                R.anim.disappear_anim);
        mapSlide = AnimationUtils.loadAnimation(DashboardActivity.this,
                R.anim.map_slide);
        mapSlideRight = AnimationUtils.loadAnimation(DashboardActivity.this,
                R.anim.map_slide_right);
        right2left = AnimationUtils.loadAnimation(DashboardActivity.this,
                R.anim.right_to_left);
        left2right = AnimationUtils.loadAnimation(DashboardActivity.this,
                R.anim.left_to_right);
        fadeOut = AnimationUtils.loadAnimation(DashboardActivity.this,
                R.anim.slide_up);

        mapSlideRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                routeCard.setVisibility(View.VISIBLE);
                routeCard.startAnimation(left2right);
                dangerCard.setVisibility(View.VISIBLE);
                dangerCard.startAnimation(left2right);
                contactCard.setVisibility(View.VISIBLE);
                contactCard.startAnimation(left2right);
                goLive.setVisibility(View.VISIBLE);
                goLive.startAnimation(left2right);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        right2left.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

                // Do nothing

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                routeCard.setVisibility(View.GONE);
                dangerCard.setVisibility(View.GONE);
                contactCard.setVisibility(View.GONE);
                //anonymousUpdate.setVisibility(View.GONE);
                goLive.setVisibility(View.GONE);
                liveMap.setVisibility(View.VISIBLE);
                liveMap.startAnimation(mapSlide);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        goLive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (CommonMethods.isLocationAvailable(DashboardActivity.this)) {
                    if (isNotInBounds) {
                        CommonMethods.commonDialog(DashboardActivity.this, "Currently this service is not" +
                                " operational in your region. We are working very hard to bring it " +
                                "to you. So please keep on checking the app periodically.");
                    } else {

                        routeCard.startAnimation(right2left);
                        dangerCard.startAnimation(right2left);
                        contactCard.startAnimation(right2left);
                        //anonymousUpdate.startAnimation(fadeOut);
                        goLive.startAnimation(right2left);

                        if (mGoogleApiClient.isConnected()) {
                            if (CommonMethods.isNetworkAvailable(DashboardActivity.this)) {
                                new GetPlacesFromServer().execute(String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude()), "amenities,events,tourist_places");
                            } else {
                                CommonMethods.wirelessDialog(DashboardActivity.this);
                            }
                        } else {
                            CommonMethods.showShortToast(DashboardActivity.this, "Location not available. Please try again.");
                        }
                    }
                } else {
                    CommonMethods.locationDialog(DashboardActivity.this);
                }

            }
        });

        fabCancelLiveMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liveMap.startAnimation(mapSlideRight);
                liveMap.setVisibility(View.GONE);

            }
        });

        fabLiveMapFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement Dialog for filters
                Intent i = new Intent(DashboardActivity.this, FilterDialogActivity.class);
                startActivityForResult(i, 3458);

            }
        });

    }

    public void timerDelayRemoveDialog(long time, final ProgressDialog d) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                d.dismiss();
            }
        }, time);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    int closeCounter = 0;

    @Override
    public void onBackPressed() {


        closeCounter = 1;
        if (closeCounter == 1) {
            finish();
        } else {
            liveMap.startAnimation(mapSlideRight);
            liveMap.setVisibility(View.GONE);
            CommonMethods.showShortToast(DashboardActivity.this, "Press back again to exit.");
        }
    }

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(DashboardActivity.this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(DashboardActivity.this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(DashboardActivity.this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub


        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            //View header = LayoutInflater.from(this).inflate(R.layout.nav_header, null);
            //navigationView.addHeaderView(header);

            Menu m = navigationView.getMenu();
            navigationView.getMenu().getItem(0).setChecked(true);
            for (int i = 0; i < m.size(); i++) {
                MenuItem mi = m.getItem(i);
                applyFontToMenuItem(mi);
            }

            ImageView profilePicture = (ImageView) findViewById(R.id.ivProfile);
            TextView profileName = (TextView) findViewById(R.id.tvUsername);
            profileName.setTypeface(arvoRegular);
            String firstName = CommonMethods.readStringPreference(
                    getApplicationContext(), "userDetails", "fName");
            String lastName = CommonMethods.readStringPreference(
                    getApplicationContext(), "userDetails", "lName");
            profileName.setText(firstName + " " + lastName);
            TextView profileEmail = (TextView) findViewById(R.id.tvEmailUser);
            profileEmail.setTypeface(arvoRegular);
            String email = CommonMethods.readStringPreference(
                    getApplicationContext(), "userDetails", "email");
            profileEmail.setText(email);
            TextDrawable drawable = TextDrawable.builder().buildRound(
                    String.valueOf(firstName.charAt(0)),
                    ColorGenerator.MATERIAL.getRandomColor());
            profilePicture.setImageDrawable(drawable);
            setupDrawerContent(navigationView);
        }

        super.onResume();
    }

    private boolean isFirstTime() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {

            RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // This aligns button to the bottom left side of screen
            lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            // Set margins to the button, we add 16dp margins here
            int margin = ((Number) (getResources().getDisplayMetrics().density * 16)).intValue();
            lps.setMargins(margin, margin, margin, margin);

            showcaseView = new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(findViewById(R.id.cardRoute)))
                    .setOnClickListener(this)
                    .build();
            showcaseView.setContentText("Check you route for roadblocks");
            showcaseView.setButtonPosition(lps);
            showcaseView.setStyle(R.style.CustomShowcaseTheme);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.commit();

        }
        return ranBefore;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        if (item.getItemId() == android.R.id.home) {

            mDrawerLayout.openDrawer(GravityCompat.START);

        }
        if (item.getItemId() == R.id.action_amenities) {
            startActivity(new Intent(DashboardActivity.this, ReportedBlocksActivity.class));
        }
        if (item.getItemId() == R.id.action_search) {
            if (CommonMethods.isLocationAvailable(DashboardActivity.this)) {
                if (isNotInBounds) {
                    CommonMethods.commonDialog(DashboardActivity.this, "Currently this service is not" +
                            " operational in your region. We are working very hard to bring it " +
                            "to you. Please keep on checking the app periodically.");
                } else {
                    startActivityForResult(new Intent(DashboardActivity.this, SearchTagsActivity.class), 11);
                }
            } else {
                CommonMethods.locationDialog(DashboardActivity.this);
            }
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 11) {
            if (resultCode == RESULT_OK) {

                String result = data.getStringExtra("result");
                Intent i = new Intent(DashboardActivity.this, PlacesActivity.class);
                i.putExtra("result", result);
                startActivity(i);

            }
        }
        if (requestCode == 3458) {
            if (resultCode == RESULT_OK) {

                String result = data.getStringExtra("result");
                mMap.clear();
                if (CommonMethods.isNetworkAvailable(DashboardActivity.this)) {
                    if (CommonMethods.isLocationAvailable(DashboardActivity.this)) {
                        new GetPlacesFromServer().execute(String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude()), result);
                    } else {
                        CommonMethods.locationDialog(DashboardActivity.this);
                    }
                } else {
                    CommonMethods.wirelessDialog(DashboardActivity.this);
                }
            }
        }

    }

    private void shareApp(String code) {
        // TODO Auto-generated method stub

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String uri = ("https://play.google.com/store/apps/details?id=in.wadersgroup.companion");
        intent.putExtra(
                Intent.EXTRA_TEXT,
                "Companion - Making Travel Smarter."
                        + "\nDownload the app and earn 25 reward points. "
                        + "And guess what? Your friend gets 25 points too. \nUse code: "
                        + code + "\n\n" + uri);

        try {
            startActivity(Intent.createChooser(intent, "Share via"));
        } catch (android.content.ActivityNotFoundException ex) {
            AnalyticsApplication.getInstance().trackException(ex);
            // (handle error)
        }

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView
                .setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        menuItem.setChecked(true);

                        switch (menuItem.getItemId()) {

                            case R.id.nav_home:

                                break;
                            case R.id.nav_profile:

                                startActivity(new Intent(DashboardActivity.this,
                                        ProfileActivity.class));
                                break;
                            case R.id.nav_share:

                                String codeShare = CommonMethods.readStringPreference(DashboardActivity.this, "userDetails", "referral_code");
                                shareApp(codeShare);

                                break;
                            case R.id.nav_rewards:

                                startActivity(new Intent(DashboardActivity.this,
                                        RewardsActivity.class));

                                break;
                            case R.id.nav_promo:

                                startActivity(new Intent(DashboardActivity.this,
                                        PromotionActivity.class));

                                break;

                            case R.id.nav_about:

                                startActivity(new Intent(DashboardActivity.this,
                                        AboutUsActivity.class));

                                break;
                            case R.id.nav_settings:
                                startActivity(new Intent(DashboardActivity.this,
                                        SettingsActivity.class));
                                break;
                            case R.id.nav_feedback:

                                // Intent intent = new Intent(Intent.ACTION_SENDTO,
                                // Uri.fromParts("mailto",
                                // "feedback@gantavya.com", null));
                                // startActivity(Intent.createChooser(intent,
                                // "Choose an Email client:"));

                                int versionCode = 0;
                                PackageManager packageManager = getPackageManager();

                                DisplayMetrics metrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                int widthPixels = metrics.widthPixels;
                                int heightPixels = metrics.heightPixels;
                                float widthDpi = metrics.xdpi;
                                float heightDpi = metrics.ydpi;
                                float widthInches = widthPixels / widthDpi;
                                float heightInches = heightPixels / heightDpi;

                                double diagonalInches = Math.sqrt(
                                        (widthInches * widthInches)
                                                + (heightInches * heightInches));

                                try {
                                    PackageInfo packageInfo = packageManager
                                            .getPackageInfo(getPackageName(), 0);
                                    versionCode = packageInfo.versionCode;
                                } catch (NameNotFoundException e) {
                                    // TODO Auto-generated catch block
                                    AnalyticsApplication.getInstance().trackException(e);
                                    e.printStackTrace();
                                }

                                Intent send = new Intent(Intent.ACTION_SENDTO);
                                String uriText = "mailto:"
                                        + Uri.encode("contact@wadersgroup.in")
                                        + "?subject="
                                        + Uri.encode("Feedback from "
                                        + CommonMethods
                                        .readStringPreference(
                                                DashboardActivity.this,
                                                "userDetails",
                                                "fName")
                                        + " "
                                        + CommonMethods
                                        .readStringPreference(
                                                DashboardActivity.this,
                                                "userDetails",
                                                "lName"))
                                        + "&body="
                                        + Uri.encode("Device ID : "
                                        + Secure.getString(
                                        DashboardActivity.this
                                                .getContentResolver(),
                                        Secure.ANDROID_ID)
                                        + "\nDevice Name : "
                                        + Build.MANUFACTURER
                                        .toUpperCase(Locale.ENGLISH)
                                        + " "
                                        + Build.MODEL
                                        + "\nAndroid Version : "
                                        + Build.VERSION.RELEASE
                                        + "\nApp Version Code : "
                                        + versionCode
                                        + "\nDevice Screen Size : "
                                        + diagonalInches
                                        + "\n-----------Please do not write anything "
                                        + "above this line. It helps us serve you better.");

                                Uri uri = Uri.parse(uriText);

                                send.setData(uri);
                                startActivity(Intent.createChooser(send,
                                        "Choose an Email client:"));

                                break;

                        }

                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypeFaceSpan("", font), 0,
                mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        mi.setTitle(mNewTitle);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (CommonMethods.isLocationAvailable(DashboardActivity.this)) {
            mGoogleApiClient.connect();
            pDialogLocation.show();
        } else {
            if (popUpFrequency % 5 == 0) {
                CommonMethods.locationDialog(DashboardActivity.this);
            } else {
                // Do Nothing
            }
        }

        popUpFrequency++;

        //mGoogleApiClient.connect();


        if (getIntent().getStringExtra("header") != null) {
            if (getIntent().getStringExtra("header").trim().contentEquals("recharge_status")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
                builder.setMessage(getIntent().getStringExtra("message"))
                        .setTitle("Recharge Status");
                AlertDialog dialog = builder.create();
                dialog.show();
            } else if (getIntent().getStringExtra("header").trim().contentEquals("roadblock_removed")) {
                if (getIntent().getStringExtra("block_id") != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
                    builder.setMessage(getIntent().getStringExtra("cause").trim().substring(0, 1).toUpperCase() + getIntent().getStringExtra("cause").trim().substring(1).toLowerCase() + " at " + getIntent().getStringExtra("address").trim() + " has been removed. Please continue your journey. Be safe.")
                            .setTitle(getIntent().getStringExtra("cause").trim().substring(0, 1).toUpperCase() + getIntent().getStringExtra("cause").trim().substring(1).toLowerCase() + " Removed");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            } else if (getIntent().getStringExtra("header").trim().contentEquals("general_notification")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
                builder.setMessage(getIntent().getStringExtra("message"))
                        .setTitle(getIntent().getStringExtra("title"));
                AlertDialog dialog = builder.create();
                dialog.show();
            }

        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    HashMap<String, String> weakHashMap = new HashMap<String, String>();

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.setPadding(26, 200, 0, 0);

        mMap = googleMap;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.


        getMenuInflater().inflate(R.menu.dashboard_menu, menu);

            /*SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                    .getActionView();

            SearchView.SearchAutoComplete theTextArea = (SearchView.SearchAutoComplete) searchView
                    .findViewById(android.support.v7.appcompat.R.id.search_src_text);
            theTextArea.setTextColor(Color.WHITE);
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));*/


        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            pDialogLocation.dismiss();
            if (CommonMethods.isInSikkimAndNB(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))) {


            } else {
                isNotInBounds = true;
                //goLive.setVisibility(View.GONE);
                //dangerCard.setVisibility(View.GONE);

            }
        } else {
            /*AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
            builder.setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // RESTART ACTIVITY

                    Intent intent = getIntent();
                    overridePendingTransition(0, 0);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(intent);

                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    finish();
                }
            });
            builder.setMessage("Location not available. Please try again.");
            AlertDialog dialog = builder.create();
            dialog.show();*/
            /*CommonMethods.showLongToast(DashboardActivity.this, "Location not Available! Turn on location services" +
                    " to use the app properly!");*/

            timerDelayRemoveDialog(5000, pDialogLocation);

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

    List<HashMap<String, String>> amenitiesArray = new ArrayList<HashMap<String, String>>();
    List<HashMap<String, String>> eventsArray = new ArrayList<HashMap<String, String>>();
    List<HashMap<String, String>> touristArray = new ArrayList<HashMap<String, String>>();
    List<HashMap<String, String>> roadblockArray = new ArrayList<HashMap<String, String>>();

    @Override
    public void onClick(View v) {
        int margin = ((Number) (getResources().getDisplayMetrics().density * 16)).intValue();
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switch (counter) {

            case 0:
                showcaseView.setShowcase(new ViewTarget(contactCard), true);
                showcaseView.setContentText("Actively update roadblocks that you get stuck in!");
                // This aligns button to the bottom left side of screen
                lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                // Set margins to the button, we add 16dp margins here
                lps.setMargins(margin, margin, margin, margin);
                showcaseView.setButtonPosition(lps);
                showcaseView.setStyle(R.style.CustomShowcaseTheme);
                break;

            case 1:
                showcaseView.setShowcase(new ViewTarget(dangerCard), true);
                showcaseView.setContentText("Explore nearby tourist places and events");
                // This aligns button to the bottom left side of screen
                lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                // Set margins to the button, we add 16dp margins here
                lps.setMargins(margin, margin, margin, margin);
                showcaseView.setButtonPosition(lps);
                showcaseView.setStyle(R.style.CustomShowcaseTheme2);
                break;

            case 2:
                showcaseView.hide();
                break;

        }
        counter++;
    }

    class GetPlacesFromServer extends AsyncTask<String, Void, Void> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(DashboardActivity.this);
            pDialog.setMessage("Going Live...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {

            String res;

            try {
                String locationString = URLEncoder.encode(params[0], "utf-8");
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(DashboardActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(DashboardActivity.this, "secret",
                                "secretKey"), "utf-8");
                String format = URLEncoder.encode(params[1], "utf-8");

                System.out.println("Formatty: " + format);

                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                // Put URL Below
                HttpGet httpPost = new HttpGet(Constants.GO_LIVE_URL + email
                        + "&secret_key=" + secret
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

                            try {
                                JSONArray amenitiesJsonArray = myJson.getJSONArray("amenities");
                                for (int i = 0; i < amenitiesJsonArray.length(); i++) {
                                    JSONObject placeObject = amenitiesJsonArray
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

                                    amenitiesArray.add(hm);

                                }
                            } catch (JSONException e) {
                                AnalyticsApplication.getInstance().trackException(e);
                                amenitiesArray.clear();
                                System.out.println("Amenities Clear");
                            }

                            try {
                                JSONArray eventsJsonArray = myJson.getJSONArray("events");
                                for (int i = 0; i < eventsJsonArray.length(); i++) {
                                    JSONObject placeObject = eventsJsonArray
                                            .getJSONObject(i);
                                    String eventName = placeObject
                                            .getString("name");
                                    String eventLocation = placeObject
                                            .getString("location");
                                    String eventTypes = placeObject
                                            .getString("type");
                                    String eventAbout = placeObject.getString("about");
                                    String eventContactPerson = placeObject.getString("contact_person");
                                    String eventContactNumber = placeObject.getString("contact");
                                    String eventAddress = placeObject
                                            .getString("address");
                                    String eventOpeningTime = placeObject
                                            .getString("opening_time");
                                    String eventClosingTime = placeObject
                                            .getString("closing_time");
                                    String eventStartDate = placeObject
                                            .getString("start_date");
                                    String eventEndDate = placeObject
                                            .getString("end_date");

                                    HashMap<String, String> hm = new HashMap<String, String>();
                                    hm.put("name", eventName);
                                    hm.put("location", eventLocation);
                                    hm.put("type", eventTypes);
                                    hm.put("address", eventAddress);
                                    hm.put("contact_person", eventContactPerson);
                                    hm.put("contact_number", eventContactNumber);
                                    hm.put("about", eventAbout);
                                    hm.put("opening_time", eventOpeningTime);
                                    hm.put("closing_time", eventClosingTime);
                                    hm.put("start_date", eventStartDate);
                                    hm.put("end_date", eventEndDate);

                                    eventsArray.add(hm);

                                }
                            } catch (JSONException e) {
                                AnalyticsApplication.getInstance().trackException(e);
                                eventsArray.clear();
                                System.out.println("Events Clear");
                            }

                            try {
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
                                    JSONArray imageArray = placeObject.getJSONArray("images");

                                    HashMap<String, String> hm = new HashMap<String, String>();
                                    hm.put("name", touristName);
                                    hm.put("location", touristLocation);
                                    hm.put("type", touristTypes);
                                    hm.put("address", touristAddress);
                                    hm.put("about", touristAbout);
                                    hm.put("opening_time", touristOpeningTime);
                                    hm.put("closing_time", touristClosingTime);
                                    hm.put("public_rating", touristRating);
                                    hm.put("image_uri", Constants.staticURLBitches + imageArray.get(0));

                                    touristArray.add(hm);

                                }
                            } catch (JSONException e) {
                                AnalyticsApplication.getInstance().trackException(e);
                                touristArray.clear();
                                System.out.println("Tourist Clear");
                            }
                            try {
                                JSONArray roadblockJsonArray = myJson.getJSONArray("road_blocks");


                                for (int i = 0; i < roadblockJsonArray.length(); i++) {
                                    JSONObject placeObject = roadblockJsonArray
                                            .getJSONObject(i);
                                    String blockId = placeObject
                                            .getString("block_id");
                                    String roadblockLocation = placeObject
                                            .getString("location");
                                    String roadblockTypes = placeObject
                                            .getString("cause");
                                    String roadblockConfidence = placeObject.getString("active");
                                    String roadblockAddress = placeObject.getString("address");

                                    HashMap<String, String> hm = new HashMap<String, String>();
                                    hm.put("block_id", blockId);
                                    hm.put("location", roadblockLocation);
                                    hm.put("cause", roadblockTypes);
                                    hm.put("active", roadblockConfidence);
                                    hm.put("address", roadblockAddress);

                                    roadblockArray.add(hm);

                                }
                            } catch (JSONException e) {
                                AnalyticsApplication.getInstance().trackException(e);
                                System.out.println("Roadblock Clear");
                            }

                        } else {
                            // Flag to make a toast for no results
                        }

                    } catch (JSONException e) {
                        AnalyticsApplication.getInstance().trackException(e);
                        e.printStackTrace();
                        System.out.println("All Clear");
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (amenitiesArray != null) {

                for (int i = 0; i < amenitiesArray.size(); i++) {

                    HashMap<String, String> place = amenitiesArray.get(i);

                    String placeName = place.get("name");
                    String placeLocation = place.get("location");
                    String placeTypes = place.get("type");
                    String placeAddress = place.get("address");
                    String placeContactPerson = place.get("contact_person");
                    String placeContactNumber = place.get("contact_number");
                    String placeAbout = place.get("about");

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mLastLocation.getLatitude(), mLastLocation
                                    .getLongitude()), 14));

                    LatLng position = new LatLng(Double.valueOf(placeLocation
                            .trim().split(",")[0].trim()),
                            Double.valueOf(placeLocation.trim().split(",")[1]
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

                    Marker mMarker = mMap.addMarker(new MarkerOptions()
                            .position(position).title(placeName)
                            .snippet(placeAddress).icon(BitmapDescriptorFactory.fromResource(resource)));

                    weakHashMap.put(mMarker.getId(), "amenities" + "----" + placeName + "----"
                            + placeAddress + "----" + placeTypes + "----" + placeContactPerson
                            + "----" + placeContactNumber + "----" + placeAbout + "----" + "BS");

                    System.out.println("Marker ID: " + mMarker.getId());

                }
            }
            if (eventsArray != null) {
                for (int i = 0; i < eventsArray.size(); i++) {

                    HashMap<String, String> place = eventsArray.get(i);

                    String eventName = place.get("name");
                    String eventLocation = place.get("location");
                    String eventTypes = place.get("type");
                    String eventAddress = place.get("address");
                    String eventContactPerson = place.get("contact_person");
                    String eventContactNumber = place.get("contact_number");
                    String eventAbout = place.get("about");
                    String eventOpeningTime = place.get("opening_time");
                    String eventClosingTime = place.get("closing_time");
                    String eventStartDate = place.get("start_date");
                    String eventEndDate = place.get("end_date");

                    LatLng position = new LatLng(Double.valueOf(eventLocation
                            .trim().split(",")[0].trim()),
                            Double.valueOf(eventLocation.trim().split(",")[1]
                                    .trim()));

                    Marker mMarker = mMap.addMarker(new MarkerOptions()
                            .position(position).title(eventName)
                            .snippet(eventAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_events)));

                    weakHashMap.put(mMarker.getId(), "event" + "----" + eventName + "----"
                            + eventAddress + "----" + eventTypes + "----" + eventContactPerson
                            + "----" + eventContactNumber + "----" + eventAbout + "----" + eventOpeningTime
                            + "----" + eventClosingTime + "----" + eventStartDate
                            + "----" + eventEndDate + "----" + "BS");

                    System.out.println("Marker ID: " + mMarker.getId());

                }
            }
            if (touristArray != null) {
                for (int i = 0; i < touristArray.size(); i++) {

                    HashMap<String, String> place = touristArray.get(i);

                    String touristName = place.get("name");
                    String touristLocation = place.get("location");
                    String touristTypes = place.get("type");
                    String touristAddress = place.get("address");
                    String touristAbout = place.get("about");
                    String touristOpeningTime = place.get("opening_time");
                    String touristClosingTime = place.get("closing_time");
                    String touristRating = place.get("public_rating");
                    // Get image Uri
                    String imageUri = place.get("image_uri");
                    String touristPlaceId = place.get("place_id");

                    LatLng position = new LatLng(Double.valueOf(touristLocation
                            .trim().split(",")[0].trim()),
                            Double.valueOf(touristLocation.trim().split(",")[1]
                                    .trim()));

                    Marker mMarker = mMap.addMarker(new MarkerOptions()
                            .position(position).title(touristName)
                            .snippet(touristAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_tourism)));

                    weakHashMap.put(mMarker.getId(), "tourist" + "----" + touristName + "----"
                            + touristAddress + "----" + touristTypes + "----" + touristAbout
                            + "----" + touristOpeningTime
                            + "----" + touristClosingTime + "----" + touristRating
                            + "----" + touristLocation + "----" + imageUri + "----" + touristPlaceId + "----" + "BS");

                    System.out.println("Putting Location: " + touristLocation);

                    System.out.println("Marker ID: " + mMarker.getId());

                }
            }
            for (int i = 0; i < roadblockArray.size(); i++) {

                HashMap<String, String> place = roadblockArray.get(i);

                String blockId = place.get("block_id");
                String roadblockLocation = place.get("location");
                String roadblockTypes = place.get("cause");
                String roadblockConfidence = place.get("active");
                String roadblockAddress = place.get("address");

                LatLng position = new LatLng(Double.valueOf(roadblockLocation
                        .trim().split(",")[0].trim()),
                        Double.valueOf(roadblockLocation.trim().split(",")[1]
                                .trim()));

                int resource = 0;
                String title = "";

                if (roadblockTypes.contentEquals("landslide")) {

                    resource = R.drawable.ic_pointer_landslide;
                    title = "Landslide";

                } else if (roadblockTypes.contentEquals("accident")) {

                    resource = R.drawable.ic_pointer_accident;
                    title = "Accident";

                } else if (roadblockTypes.contentEquals("construction")) {

                    resource = R.drawable.ic_pointer_construction;
                    title = "Construction";

                } else if (roadblockTypes.contentEquals("traffic")) {

                    resource = R.drawable.ic_pointer_traffic;
                    title = "Traffic";

                } else if (roadblockTypes.contentEquals("other")) {

                    resource = R.drawable.ic_pointer_options;
                    title = "Other Cause";

                }

                Marker mMarker = mMap.addMarker(new MarkerOptions()
                        .position(position).title(title).icon(BitmapDescriptorFactory.fromResource(resource)));

                weakHashMap.put(mMarker.getId(), "roadblock" + "----" + blockId + "----"
                        + roadblockTypes + "----" + roadblockConfidence + "----" + roadblockAddress + "----" + "BS");

                System.out.println("Marker ID: " + mMarker.getId());

            }

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(final Marker marker) {
                    mMap.setInfoWindowAdapter(new

                                    MyInfoWindowAdapter()

                    );

                    String type = weakHashMap.get(marker.getId()).trim().split("----")[0].trim();

                    if (type.contentEquals("amenities")) {

                        String placeName = weakHashMap.get(marker.getId()).trim()
                                .split("----")[1].trim();
                        String placeAddress = weakHashMap.get(marker.getId()).trim()
                                .split("----")[2].trim();
                        String placeTypes = weakHashMap.get(marker.getId()).trim()
                                .split("----")[3].trim();
                        String placePerson = weakHashMap.get(marker.getId()).trim()
                                .split("----")[4].trim();
                        String placeNumber = weakHashMap.get(marker.getId()).trim()
                                .split("----")[5].trim();
                        String placeAbout = weakHashMap.get(marker.getId()).trim()
                                .split("----")[6].trim();

                        if (placeTypes.contentEquals("auto_repair")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_auto_repair));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_auto_repair_big));

                        } else if (placeTypes.contentEquals("hotel")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_hotel));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_hotel_big));

                        } else if (placeTypes.contentEquals("restaurant")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_restaurant));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_restaurant_big));

                        } else if (placeTypes.contentEquals("atm")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_atm));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_atm_big));

                        } else if (placeTypes.contentEquals("gas_station")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_petrol));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_petrol_big));

                        } else if (placeTypes.contentEquals("bank")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_bank));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_bank_big));

                        } else if (placeTypes.contentEquals("toilet")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_toilet));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_toilet_big));

                        } else if (placeTypes.contentEquals("pharmacy")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_pharmacy));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_pharmacy_big));

                        } else if (placeTypes.contentEquals("hospital")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_hospital));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_hospital_big));

                        } else if (placeTypes.contentEquals("police")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_police));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_police_big));

                        } else if (placeTypes.contentEquals("taxi_stand")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_taxi_stand));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_taxi_stand_big));

                        } else if (placeTypes.contentEquals("bus_station")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_bus_stop));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_bus_station_big));

                        } else if (placeTypes.contentEquals("train_station")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_rail));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_rail_big));

                        } else if (placeTypes.contentEquals("airport")) {

                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_airport));
                                }
                            });
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_airport_big));

                        }


                        startPlaceDetailsActivity(placeName, placeAddress, placePerson, placeNumber, placeAbout);


                    } else if (type.contentEquals("event")) {

                        String eventName = weakHashMap.get(marker.getId()).trim()
                                .split("----")[1].trim();
                        String eventAddress = weakHashMap.get(marker.getId()).trim()
                                .split("----")[2].trim();
                        String eventTypes = weakHashMap.get(marker.getId()).trim()
                                .split("----")[3].trim();
                        String eventContactPerson = weakHashMap.get(marker.getId()).trim()
                                .split("----")[4].trim();
                        String eventContactNumber = weakHashMap.get(marker.getId()).trim()
                                .split("----")[5].trim();
                        String eventAbout = weakHashMap.get(marker.getId()).trim()
                                .split("----")[6].trim();
                        String eventOpeningTime = weakHashMap.get(marker.getId()).trim()
                                .split("----")[6].trim();
                        String eventClosingTime = weakHashMap.get(marker.getId()).trim()
                                .split("----")[6].trim();
                        String eventStartDate = weakHashMap.get(marker.getId()).trim()
                                .split("----")[6].trim();
                        String eventEndDate = weakHashMap.get(marker.getId()).trim()
                                .split("----")[6].trim();

                        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_events));
                            }
                        });
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_events_big));


                    } else if (type.contentEquals("tourist")) {

                        String touristName = weakHashMap.get(marker.getId()).trim()
                                .split("----")[1].trim();
                        String touristAddress = weakHashMap.get(marker.getId()).trim()
                                .split("----")[2].trim();
                        String touristTypes = weakHashMap.get(marker.getId()).trim()
                                .split("----")[3].trim();
                        String touristAbout = weakHashMap.get(marker.getId()).trim()
                                .split("----")[4].trim();
                        String touristOpeningTime = weakHashMap.get(marker.getId()).trim()
                                .split("----")[5].trim();
                        String touristClosingTime = weakHashMap.get(marker.getId()).trim()
                                .split("----")[6].trim();
                        String touristRating = weakHashMap.get(marker.getId()).trim()
                                .split("----")[7].trim();
                        String touristLocation = weakHashMap.get(marker.getId()).trim()
                                .split("----")[8].trim();
                        String imageUri = weakHashMap.get(marker.getId()).trim()
                                .split("----")[9].trim();
                        String placeId = weakHashMap.get(marker.getId()).trim()
                                .split("----")[10].trim();

                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_tourism_big));

                        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_tourism));
                            }
                        });

                        System.out.println("Getting Location: " + touristLocation);

                        startTouristDetailsActivity(touristName, touristAddress, touristOpeningTime, touristClosingTime, touristAbout, touristRating, imageUri, touristLocation, placeId);

                    } else if (type.contentEquals("roadblock")) {

                        String blockId = weakHashMap.get(marker.getId()).trim()
                                .split("----")[1].trim();
                        String roadblockTypes = weakHashMap.get(marker.getId()).trim()
                                .split("----")[2].trim();

                        if (roadblockTypes.contentEquals("landslide")) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_landslide_big));
                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_landslide));
                                }
                            });
                        } else if (roadblockTypes.contentEquals("accident")) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_accident_big));
                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_accident));
                                }
                            });
                        } else if (roadblockTypes.contentEquals("traffic")) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_traffic_big));
                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_traffic));
                                }
                            });
                        } else if (roadblockTypes.contentEquals("construction")) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_construction_big));
                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_construction));
                                }
                            });
                        } else if (roadblockTypes.contentEquals("other")) {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_other_big));
                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pointer_options));
                                }
                            });
                        }

                        String roadblockConfidence = weakHashMap.get(marker.getId()).trim()
                                .split("----")[3].trim();
                        String roadblockAddress = weakHashMap.get(marker.getId()).trim()
                                .split("----")[4].trim();

                        marker.showInfoWindow();

                    }

                    return true;
                }
            });

            pDialog.dismiss();

        }
    }

    public void startPlaceDetailsActivity(String name, String address, String contactPerson, String contactNumber, String about) {
        Intent i = new Intent(DashboardActivity.this, PlaceDetailsActivity.class);
        i.putExtra("name", name);
        i.putExtra("address", address);
        i.putExtra("contactPerson", contactPerson);
        i.putExtra("contactNumber", contactNumber);
        i.putExtra("about", about);
        startActivity(i);
    }

    public void startTouristDetailsActivity(String name, String address, String openingTime, String closingTime, String about, String rating, String imageUri, String location, String placeId) {
        Intent i = new Intent(DashboardActivity.this, PlaceDetailsBigActivity.class);
        i.putExtra("place_id", placeId);
        i.putExtra("name", name);
        i.putExtra("address", address);
        i.putExtra("opening_time", openingTime);
        i.putExtra("closing_time", closingTime);
        i.putExtra("about", about);
        i.putExtra("rating", rating);
        i.putExtra("image_uri", imageUri);
        i.putExtra("location", location);

        System.out.println("Location of Selected Tourist Place: " + location);

        startActivity(i);
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
                    .split("----")[2].trim().toUpperCase());
            tvCause.setTypeface(arvoRegular);
            TextView tvAddress = ((TextView) myContentsView.findViewById(R.id.tvRoadblockAddress));
            tvAddress.setText("at  " + weakHashMap.get(marker.getId()).trim()
                    .split("----")[4].trim());
            tvAddress.setTypeface(arvoRegular);
            TextView tvActiveStatus = ((TextView) myContentsView.findViewById(R.id.tvRoadblockActiveStatus));
            tvActiveStatus.setText(weakHashMap.get(marker.getId()).trim()
                    .split("----")[3].trim());
            tvActiveStatus.setTypeface(arvoRegular);

            return myContentsView;
        }
    }


}
