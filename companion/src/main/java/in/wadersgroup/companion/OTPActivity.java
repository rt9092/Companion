package in.wadersgroup.companion;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OTPActivity extends AppCompatActivity {

    EditText otpField;
    private static OTPActivity ins;
    TextView gettingOtp;
    ProgressBar pBar;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    boolean checkMobile = false;

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
        setContentView(R.layout.activity_otp);

        if (CommonMethods.isNetworkAvailable(OTPActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(OTPActivity.this);
        }

        /*if (CommonMethods.isLocationAvailable(OTPActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(OTPActivity.this);
        }*/

        CommonMethods.createStringPreference(
                OTPActivity.this, "skipper", "otpSkip",
                "skip");

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    //CommonMethods.showShortToast(OTPActivity.this, getString(R.string.gcm_send_message));
                } else {
                    //CommonMethods.showShortToast(OTPActivity.this, getString(R.string.token_error_message));
                }
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        ins = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.dashboard_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("VERIFICATION");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");

        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");
        toolbar_title.setTypeface(arvoBold);

        final Button verifyOTP = (Button) findViewById(R.id.bVerifyOTP);
        final Button changeMobile = (Button) findViewById(R.id.bChangeMobile);
        final EditText enterMobile = (EditText) findViewById(R.id.etEnterMobile);
        final TextView resendOTP = (TextView) findViewById(R.id.tvResendOTP);
        gettingOtp = (TextView) findViewById(R.id.tvGettingOTP);
        pBar = (ProgressBar) findViewById(R.id.progressBar);
        otpField = (EditText) findViewById(R.id.etOTP);

        if (!CommonMethods.readStringPreference(OTPActivity.this, "userDetails", "mobile").trim().contentEquals("None")) {

            enterMobile.setVisibility(View.GONE);
            resendOTP.setTypeface(arvoBold);
            gettingOtp.setTypeface(arvoBold);
            gettingOtp.setText("Getting OTP sent via SMS on number " + CommonMethods.readStringPreference(OTPActivity.this, "userDetails", "mobile"));


            resendOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new ResendOTP().execute();
                }
            });


            verifyOTP.setTypeface(arvoBold);
            otpField.setTypeface(arvoBold);


            verifyOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if (CommonMethods.isNetworkAvailable(OTPActivity.this)) {
                        new OtpVerification().execute(otpField.getText().toString().trim());
                    } else {
                        CommonMethods.wirelessDialog(OTPActivity.this);

                    }

                }
            });

            changeMobile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkMobile == true) {
                        if (enterMobile.getText().toString().length() < 10) {
                            CommonMethods.showShortToast(OTPActivity.this, "Please enter a valid mobile number");
                        } else {
                            CommonMethods.createStringPreference(OTPActivity.this,
                                    "userDetails", "mobile", enterMobile.getText().toString().trim());
                            new ResendOTP().execute();
                            enterMobile.setVisibility(View.GONE);
                            changeMobile.setText("Enter Mobile Number");
                            checkMobile = false;
                            gettingOtp.setText("Getting OTP sent via SMS on number " + CommonMethods.readStringPreference(OTPActivity.this, "userDetails", "mobile"));
                        }
                    } else {
                        enterMobile.setVisibility(View.VISIBLE);
                        changeMobile.setText("Verify this Mobile");
                        checkMobile = true;
                    }
                }
            });
        } else {
            changeMobile.setText("Verify this Mobile");
            enterMobile.setVisibility(View.VISIBLE);
            pBar.setVisibility(View.GONE);
            otpField.setVisibility(View.GONE);
            verifyOTP.setVisibility(View.GONE);
            resendOTP.setVisibility(View.GONE);
            gettingOtp.setVisibility(View.GONE);
            changeMobile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (enterMobile.getText().toString().length() < 10) {
                        CommonMethods.showShortToast(OTPActivity.this, "Please enter a valid mobile number");
                    } else {
                        CommonMethods.createStringPreference(OTPActivity.this,
                                "userDetails", "mobile", enterMobile.getText().toString().trim());
                        pBar.setVisibility(View.VISIBLE);
                        otpField.setVisibility(View.VISIBLE);
                        verifyOTP.setVisibility(View.VISIBLE);
                        resendOTP.setVisibility(View.VISIBLE);
                        gettingOtp.setVisibility(View.VISIBLE);
                        new ResendOTP().execute();
                        enterMobile.setVisibility(View.GONE);
                        changeMobile.setText("Enter Mobile Number");
                        checkMobile = false;
                        gettingOtp.setText("Getting OTP sent via SMS on number " + CommonMethods.readStringPreference(OTPActivity.this, "userDetails", "mobile"));
                        Intent intent = getIntent();
                        overridePendingTransition(0, 0);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(intent);
                    }
                }
            });
        }

    }

    public static OTPActivity getInstance() {
        return ins;
    }

    public void setOtp(final String message) {

        if (!message.trim().contentEquals("")) {

            try {
                System.out.println("OTP setter: " + message);
                OTPActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        otpField.setText(message);
                        pBar.setVisibility(View.GONE);
                        gettingOtp.setVisibility(View.GONE);
                    }
                });

            } catch (Exception e) {
                AnalyticsApplication.getInstance().trackException(e);
            }
        } else {
            CommonMethods.showShortToast(OTPActivity.this, "Something went wrong. Try again.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;

        super.onPause();
    }

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
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
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    class OtpVerification extends AsyncTask<String, Void, Void> {

        String res;
        ProgressDialog pDialog;
        boolean successTag = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(OTPActivity.this);
            pDialog.setMessage("Verifying OTP...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            pDialog.dismiss();
            if (successTag) {
                CommonMethods.showShortToast(OTPActivity.this, "Successfully verified.");
                CommonMethods.createStringPreference(
                        OTPActivity.this, "skipper", "loginSkip",
                        "skip");
                startActivity(new Intent(OTPActivity.this, ReferralCodeActivity.class));
                finish();
            } else {
                // WRONG
            }

        }

        @Override
        protected Void doInBackground(String... params) {

            OkHttpClient client = new OkHttpClient();
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(OTPActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(OTPActivity.this, "secret",
                                "secretKey"), "utf-8");
                String mobile_no = URLEncoder.encode(CommonMethods
                        .readStringPreference(OTPActivity.this,
                                "userDetails", "mobile"), "utf-8");
                String otp = URLEncoder.encode(params[0], "utf-8");
                Request request = new Request.Builder()
                        .url(Constants.MOBILE_URL + email + "&otp=" + otp + "&mobile_no=" + mobile_no + "&secret_key=" + secret)
                        .build();

                Response response = client.newCall(request).execute();
                res = response.body().string();
                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String success = jsonObj.getString("success").trim();
                    if (success.contentEquals("yes")) {
                        successTag = true;
                    } else {
                        successTag = false;
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

    class ResendOTP extends AsyncTask<Void, Void, Void> {

        String res;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(OTPActivity.this);
            pDialog.setMessage("Getting OTP again...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            pDialog.dismiss();

        }

        @Override
        protected Void doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(OTPActivity.this,
                                "userDetails", "email"), "utf-8");
                String mobile_no = URLEncoder.encode(CommonMethods
                        .readStringPreference(OTPActivity.this,
                                "userDetails", "mobile"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(OTPActivity.this, "secret",
                                "secretKey"), "utf-8");
                Request request = new Request.Builder()
                        .url(Constants.RESEND_OTP_API + email + "&secret_key=" + secret + "&mobile=" + mobile_no)
                        .build();

                Response response = client.newCall(request).execute();
                res = response.body().string();
                if (res != null) {
                    System.out.println("OTP Response: " + res);
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String success = jsonObj.getString("success").trim();
                    if (success.contentEquals("yes")) {
                        //Okay
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
