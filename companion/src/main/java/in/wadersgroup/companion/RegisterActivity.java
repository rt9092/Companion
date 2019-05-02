package in.wadersgroup.companion;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Romil
 */
public class RegisterActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "Dashboard Activity";

    ProgressDialog pDialog;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SECRET = "secret_key";
    String toProceed = "";

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /*
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    EditText etFirstName;
    EditText etLastName;
    EditText etEmail;
    EditText etPhone;
    /*private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;*/

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if (sdkVersion >= 21) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.statusbar));
        }

        setContentView(R.layout.activity_register);

        if (CommonMethods.isNetworkAvailable(RegisterActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(RegisterActivity.this);

        }

        /*if (CommonMethods.isLocationAvailable(RegisterActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(RegisterActivity.this);

        }*/

        /*mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    CommonMethods.showShortToast(RegisterActivity.this, getString(R.string.gcm_send_message));
                } else {
                    CommonMethods.showShortToast(RegisterActivity.this, getString(R.string.token_error_message));
                }
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }*/

        // Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE).build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("SIGN UP");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Button next = (Button) findViewById(R.id.bNext);
        Button bGoogle = (Button) findViewById(R.id.bGooglePlus);
        ImageButton phoneInfo = (ImageButton) findViewById(R.id.ibPhoneInfo);

        TextView tvFirstName = (TextView) findViewById(R.id.tvFirstName);
        TextView tvLastName = (TextView) findViewById(R.id.tvLastName);
        TextView tvEmail = (TextView) findViewById(R.id.tvEmail);
        TextView tvPhone = (TextView) findViewById(R.id.tvPhone);
        TextView tvPassword = (TextView) findViewById(R.id.tvPassword);

        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        etPhone = (EditText) findViewById(R.id.etPhone);

        String[] recourseList = this.getResources().getStringArray(
                R.array.CountryCodes);

        // Spinner countries = (Spinner) findViewById(R.id.sCountries);
        // countries.setAdapter(new CountriesAdapter(this, recourseList));

        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Bold.ttf");
        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        tvFirstName.setTypeface(arvoBold);
        tvLastName.setTypeface(arvoBold);
        tvPhone.setTypeface(arvoBold);
        tvEmail.setTypeface(arvoBold);
        tvPassword.setTypeface(arvoBold);
        etFirstName.setTypeface(arvoBold);
        etLastName.setTypeface(arvoBold);
        etEmail.setTypeface(arvoBold);
        etPhone.setTypeface(arvoBold);
        etPassword.setTypeface(arvoBold);
        next.setTypeface(arvoBold);
        toolbar_title.setTypeface(arvoRegular);

        phoneInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        RegisterActivity.this);

                // 2. Chain together various setter methods to set the dialog
                // characteristics
                builder.setMessage("We use your Phone Number to "
                        + "send you information via a text message "
                        + "in situations when Internet Access is not available.");

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                // TextView tvAlertDialog = (TextView) dialog
                // .findViewById(android.R.id.message);
                // tvAlertDialog.setTypeface(arvoRegular);
                dialog.show();
                TextView tvDialogMessage = (TextView) dialog
                        .findViewById(android.R.id.message);
                tvDialogMessage.setTypeface(arvoRegular);

            }
        });

        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (!etFirstName.getText().toString().isEmpty()
                        && !etLastName.getText().toString().isEmpty()
                        && !etEmail.getText().toString().isEmpty()
                        && !etPhone.getText().toString().isEmpty()
                        && !etPassword.getText().toString().isEmpty()) {

                    if (!Utility.validate(etEmail.getText().toString())) {
                        CommonMethods.showLongToast(RegisterActivity.this,
                                "Please enter a valid Email!");
                    } else {
                        if (etPhone.getText().toString().length() >= 10
                                && etPhone.getText().toString().length() >= 10) {
                            if (etPassword.getText().toString().length() < 8) {
                                CommonMethods
                                        .showLongToast(RegisterActivity.this,
                                                "Please enter a Password of at least 8 characters!");
                            } else {

                                if (CommonMethods.isNetworkAvailable(RegisterActivity.this)) {

                                    new RegisterInBackground().execute(etEmail
                                                    .getText().toString(), etPhone
                                                    .getText().toString(), etFirstName
                                                    .getText().toString(), etLastName
                                                    .getText().toString(), CommonMethods
                                                    .getIMEI(RegisterActivity.this),
                                            "normal", etPassword.getText()
                                                    .toString());
                                } else {
                                    CommonMethods.wirelessDialog(RegisterActivity.this);

                                }
                            }
                        } else {
                            CommonMethods.showLongToast(RegisterActivity.this,
                                    "Please enter a valid Phone Number!");
                        }
                    }

                } else {
                    CommonMethods.showLongToast(RegisterActivity.this,
                            "Please fill all the fields!");
                }

            }
        });

        bGoogle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                pDialog = new ProgressDialog(v.getContext(), R.style.MyTheme);
                pDialog.setCancelable(false);
                pDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
                pDialog.show();

                mShouldResolve = true;
                mGoogleApiClient.connect();

            }
        });

    }

    /*@Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }*/

    /*@Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }*/

    /*private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }*/

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    /*private boolean checkPlayServices() {
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
    }*/
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO Auto-generated method stub

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    AnalyticsApplication.getInstance().trackException(e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                Toast.makeText(getApplicationContext(), "ERROR",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            // Show the signed-out UI

        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub

        mShouldResolve = false;

        if (pDialog != null) {
            pDialog.dismiss();
        }

        // Show the signed-in UI

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {

            Person currentPerson = Plus.PeopleApi
                    .getCurrentPerson(mGoogleApiClient);
            String firstName = currentPerson.getName().getGivenName();
            String lastName = currentPerson.getName().getFamilyName();
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

            // Toast.makeText(getApplicationContext(), personName + " " + email,
            // Toast.LENGTH_LONG).show();

            Intent iConf = new Intent(RegisterActivity.this,
                    ConfirmationActivity.class);

            CommonMethods.createStringPreference(RegisterActivity.this,
                    "userDetails", "fName", firstName);
            CommonMethods.createStringPreference(RegisterActivity.this,
                    "userDetails", "lName", lastName);
            CommonMethods.createStringPreference(RegisterActivity.this,
                    "userDetails", "email", email);

            iConf.putExtra("fName", firstName);
            iConf.putExtra("lName", lastName);
            iConf.putExtra("email", email);

            startActivity(iConf);

            finish();

        } else {

            Toast.makeText(getApplicationContext(), "Signed In but no Data",
                    Toast.LENGTH_LONG).show();

        }

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub

        mGoogleApiClient.connect();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent arg2) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, arg2);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve
            // further.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();

    }

    class RegisterInBackground extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(RegisterActivity.this, R.style.MyTheme);
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            String msg = "";
            try {
                String email = URLEncoder.encode(params[0], "utf-8");
                String mobile_no = URLEncoder.encode(params[1], "utf-8");
                String first_name = URLEncoder.encode(params[2], "utf-8");
                String last_name = URLEncoder.encode(params[3], "utf-8");
                String imei = URLEncoder.encode(params[4], "utf-8");
                // String gcm = URLEncoder.encode(regId, "utf-8");
                String user_type = URLEncoder.encode(params[5], "utf-8");
                String password = URLEncoder.encode(params[6], "utf-8");

                String md5Hash = CommonMethods.MD5(params[3] + params[0]);

                CommonMethods.createStringPreference(
                        RegisterActivity.this, "secret", "secretKey",
                        md5Hash);

                String md5Encoded = URLEncoder.encode(md5Hash, "utf-8");

                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(Constants.REG_SERVER_URL
                        + email + "&mobile_no=" + mobile_no + "&first_name="
                        + first_name + "&last_name=" + last_name + "&imei="
                        + imei + "&user_type=" + user_type
                        + "&password=" + password + "&secret_key=" + md5Encoded);

                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                String res = EntityUtils.toString(response.getEntity());
                System.out.println("JSON Res: " + res);

                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String successTag = jsonObj.getString(TAG_SUCCESS);
                    if (successTag.contentEquals("yes")) {
                        String referral_code = jsonObj.getString("referral_code");
                        //String otp = jsonObj.getString("otp");
                        CommonMethods.createStringPreference(
                                RegisterActivity.this, "userDetails", "referral_code",
                                referral_code);

                        toProceed = "yes";
                    } else {
                        toProceed = "no";
                    }

                } else {
                    // Registration failed
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
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }

            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {
            pDialog.dismiss();
            if (msg.contains("SERVICE_NOT_AVAILABLE")) {

                // registerInBackground("rt9092@gmail.com");
                // Work for this

            } else {
                //if (!TextUtils.isEmpty(regId)) {
                if (toProceed.contentEquals("yes")) {

                    System.out.println(msg);
                    CommonMethods.createStringPreference(
                            RegisterActivity.this, "userDetails", "fName",
                            etFirstName.getText().toString().trim());
                    CommonMethods.createStringPreference(
                            RegisterActivity.this, "userDetails", "mobile",
                            etPhone.getText().toString().trim());
                    CommonMethods.createStringPreference(
                            RegisterActivity.this, "userDetails", "lName",
                            etLastName.getText().toString().trim());
                    CommonMethods.createStringPreference(
                            RegisterActivity.this, "userDetails", "email",
                            etEmail.getText().toString().trim());
                    startActivity(new Intent(RegisterActivity.this,
                            OTPActivity.class));
                    finish();
                } else if (toProceed.contentEquals("no")) {
                    CommonMethods.showLongToast(RegisterActivity.this,
                            "Already Registered! Please Login!");
                    startActivity(new Intent(RegisterActivity.this,
                            SignInToggleActivity.class));
                    finish();
                } else {
                    CommonMethods.showLongToast(RegisterActivity.this, "Registration Failed!");
                }
            }
        }
    }

}
