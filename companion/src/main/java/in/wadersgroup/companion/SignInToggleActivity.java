package in.wadersgroup.companion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * @author Romil
 */
public class SignInToggleActivity extends Activity {


    ViewPager mViewPager;
    TextView tvCue;
    GoogleApiClient googleApiClient;
    CustomPagerAdapter mCustomPagerAdapter;
    int[] mResources = {R.drawable.check_blocks, R.drawable.get_alerts,
            R.drawable.get_amenities, R.drawable.go_tourism};
    String[] mTextResources = {"Check route while sitting at your home.", "Get alerts for roadblocks while driving.",
            "Get amenities while you are on road.", "Visit beautiful places at your destination"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_in);

        if (CommonMethods.isNetworkAvailable(SignInToggleActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(SignInToggleActivity.this);

        }

        /*if(CommonMethods.isLocationAvailable(SignInToggleActivity.this)){
            // Continue
        }else{
            CommonMethods.locationDialog(SignInToggleActivity.this);

        }*/

        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        tvCue = (TextView) findViewById(R.id.tvCues);
        TextView madeWithLove = (TextView) findViewById(R.id.tvSuper);
        madeWithLove.setTypeface(arvoBold);
        SpannableStringBuilder ssb = new SpannableStringBuilder("Made with     in India");
        Bitmap love = BitmapFactory.decodeResource(getResources(), R.drawable.with_love);
        ssb.setSpan(new ImageSpan(love), 10, 13, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        madeWithLove.setText(ssb, TextView.BufferType.SPANNABLE);
        tvCue.setTypeface(arvoBold);
        tvCue.setText("Check route while sitting at your home.");

        String checkRegx = CommonMethods.readStringPreference(
                SignInToggleActivity.this, "skipper", "loginSkip");

        String otpCheck = CommonMethods.readStringPreference(SignInToggleActivity.this, "skipper", "otpSkip");


        mCustomPagerAdapter = new CustomPagerAdapter(this);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                tvCue.setText(mTextResources[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mViewPager.setAdapter(mCustomPagerAdapter);

        final CirclePageIndicator circleIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        circleIndicator.setFillColor(getResources().getColor(R.color.toolbar));
        circleIndicator.setRadius(9.0F);
        circleIndicator.setViewPager(mViewPager);

        if (checkRegx.contentEquals("skip")) {
            startActivity(new Intent(SignInToggleActivity.this,
                    DashboardActivity.class));
            finish();
        } else if (otpCheck.contentEquals("skip")) {
            startActivity(new Intent(SignInToggleActivity.this,
                    OTPActivity.class));
            finish();
        } else {

            // Buttons
            Button signIn = (Button) findViewById(R.id.bSignIn);
            Button signUp = (Button) findViewById(R.id.bSignUp);


            //Animation animationScaleUp = AnimationUtils.loadAnimation(this,
            //		R.anim.pop_out);
            Animation animationSlideUp = AnimationUtils.loadAnimation(this,
                    R.anim.fade_in);
            //Animation animationScaleDown = AnimationUtils.loadAnimation(this,
            //		R.anim.pop_in);

            //AnimationSet growShrink = new AnimationSet(true);
            //growShrink.addAnimation(animationScaleUp);
            // growShrink.addAnimation(animationScaleDown);
            //companionLogo.startAnimation(growShrink);

            Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                    "fonts/Arvo-Bold.ttf");

            signIn.setText("Sign In");
            signIn.setTypeface(arvoBold);
            //companionTitle.setTypeface(arvoBold);
            signUp.setText("Sign Up");
            signUp.setTypeface(arvoBold);
            signIn.setAnimation(animationSlideUp);
            signUp.setAnimation(animationSlideUp);

            signUp.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    startActivity(new Intent(SignInToggleActivity.this,
                            RegisterActivity.class));
                    finish();
                }
            });

            signIn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    startActivity(new Intent(SignInToggleActivity.this,
                            LoginActivity.class));
                    finish();

                }
            });

        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(SignInToggleActivity.this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        SignInToggleActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                AnalyticsApplication.getInstance().trackException(e);
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    class CustomPagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;

        public CustomPagerAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mResources.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((FrameLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.pager_item,
                    container, false);

            ImageView imageView = (ImageView) itemView
                    .findViewById(R.id.imageView);

            imageView.setImageResource(mResources[position]);

            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((FrameLayout) object);
        }
    }

}
