package in.wadersgroup.companion;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;

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
public class LoginActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener,
        ResultCallback<LoadPeopleResult> {

    ProgressDialog pDialog;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_FIRST_NAME = "first_name";
    private static final String TAG_LAST_NAME = "last_name";
    private static final String TAG_MOBILE = "mobile";
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

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if (sdkVersion >= 21) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.statusbar));
        }

        setContentView(R.layout.activity_login);

        if (CommonMethods.isNetworkAvailable(LoginActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(LoginActivity.this);

        }

        /*if (CommonMethods.isLocationAvailable(LoginActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(LoginActivity.this);

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
        toolbar_title.setText("SIGN IN");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Button next = (Button) findViewById(R.id.bNext);
        Button bGoogle = (Button) findViewById(R.id.bGooglePlus);

        TextView tvEmail = (TextView) findViewById(R.id.tvEmail);
        TextView tvPassword = (TextView) findViewById(R.id.tvPassword);

        final EditText etEmail = (EditText) findViewById(R.id.etEmail);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);

        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Bold.ttf");
        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        etPassword.setTypeface(arvoBold);
        next.setTypeface(arvoBold);
        toolbar_title.setTypeface(arvoRegular);
        etEmail.setTypeface(arvoBold);
        tvEmail.setTypeface(arvoBold);
        tvPassword.setTypeface(arvoBold);

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

        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (!etEmail.getText().toString().isEmpty()
                        && !etPassword.getText().toString().isEmpty()) {

                    if (Utility.validate(etEmail.getText().toString())) {

                        if (etPassword.getText().toString().length() >= 8) {

                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "email",
                                    etEmail.getText().toString());

                            if (CommonMethods.isNetworkAvailable(LoginActivity.this)) {

                                new LoginRequest().execute(etEmail.getText()
                                        .toString(), etPassword.getText()
                                        .toString());
                            } else {
                                CommonMethods.wirelessDialog(LoginActivity.this);

                            }
                        } else {
                            CommonMethods
                                    .showLongToast(LoginActivity.this,
                                            "Password should be of at least 8 characters!");
                        }

                    } else {
                        CommonMethods.showLongToast(LoginActivity.this,
                                "Please enter a valid Email!");
                    }

                } else {
                    CommonMethods.showLongToast(LoginActivity.this,
                            "Please fill all the fields!");
                }

            }
        });

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

		/* This Line is the key */
        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(
                LoginActivity.this);

        // Show the signed-in UI

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {

            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            CommonMethods.createStringPreference(LoginActivity.this,
                    "userDetails", "email", email);

            if (CommonMethods.isNetworkAvailable(LoginActivity.this)) {

                new LoginRequestGoogle().execute(email);
            } else {
                CommonMethods.wirelessDialog(LoginActivity.this);

            }

        } else {

            Toast.makeText(getApplicationContext(), "Error! Try Again!",
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

    class LoginRequestGoogle extends AsyncTask<String, Void, Void> {

        String messageJson;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            pDialog = new ProgressDialog(LoginActivity.this, R.style.MyTheme);
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub

            try {
                String email = URLEncoder.encode(params[0], "utf-8");
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(Constants.LOGIN_SERVER_URL
                        + email);
                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                String res = EntityUtils.toString(response.getEntity());
                System.out.println("Login Google JSON: " + res);
                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String successTag = jsonObj.getString(TAG_SUCCESS);
                    if (successTag.contentEquals("yes")) {
                        String mobile = jsonObj.getString(TAG_MOBILE);
                        String firstName = jsonObj.getString(TAG_FIRST_NAME);
                        String lastName = jsonObj.getString(TAG_LAST_NAME);
                        String secret_key = jsonObj.getString(TAG_SECRET);
                        messageJson = jsonObj.getString("message");
                        String referral_code = jsonObj.getString("referral_code");
                        System.out.println("Login Mobile: " + mobile);
                        if (!mobile.trim().contentEquals("")) {

                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "fName",
                                    firstName);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "referral_code",
                                    referral_code);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "lName",
                                    lastName);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "mobile",
                                    mobile);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "secret", "secretKey",
                                    secret_key);
                            toProceed = "yes";
                        } else if (mobile.trim().contentEquals("")) {
                            toProceed = "otp";
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "fName",
                                    firstName);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "lName",
                                    lastName);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "secret", "secretKey",
                                    secret_key);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "referral_code",
                                    referral_code);
                        }
                    } else {
                        toProceed = "no";
                        messageJson = jsonObj.getString("message");
                    }
                } else {
                    // Login Failed
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

            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            pDialog.dismiss();
            if (toProceed.contentEquals("yes")) {
                CommonMethods.createStringPreference(LoginActivity.this,
                        "skipper", "loginSkip", "skip");
                startActivity(new Intent(LoginActivity.this,
                        DashboardActivity.class));
                finish();
            } else if (toProceed.contentEquals("no")) {
                CommonMethods.showLongToast(LoginActivity.this,
                        messageJson);
            } else if (toProceed.contentEquals("otp")) {
                startActivity(new Intent(LoginActivity.this, OTPActivity.class));
                finish();
            } else {
                CommonMethods.showLongToast(LoginActivity.this,
                        "Login Failed! Try again!");
            }

        }

    }

    class LoginRequest extends AsyncTask<String, Void, Void> {

        String messageJson;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            pDialog = new ProgressDialog(LoginActivity.this, R.style.MyTheme);
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                String email = URLEncoder.encode(params[0], "utf-8");
                String password = URLEncoder.encode(params[1], "utf-8");
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(Constants.LOGIN_SERVER_URL
                        + email + "&password=" + password);
                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                String res = EntityUtils.toString(response.getEntity());
                System.out.println("Login Request Normal: " + res);
                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String successTag = jsonObj.getString(TAG_SUCCESS);
                    if (successTag.contentEquals("yes")) {
                        String mobile = jsonObj.getString(TAG_MOBILE);
                        String firstName = jsonObj.getString(TAG_FIRST_NAME);
                        String lastName = jsonObj.getString(TAG_LAST_NAME);
                        String secret_key = jsonObj.getString(TAG_SECRET);
                        messageJson = jsonObj.getString("message");
                        String referral_code = jsonObj.getString("referral_code");
                        System.out.println("Login Mobile: " + mobile);
                        if (!mobile.trim().contentEquals("")) {

                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "fName",
                                    firstName);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "referral_code",
                                    referral_code);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "lName",
                                    lastName);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "mobile",
                                    mobile);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "secret", "secretKey",
                                    secret_key);
                            toProceed = "yes";
                        } else if (mobile.trim().contentEquals("")) {
                            toProceed = "otp";
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "fName",
                                    firstName);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "lName",
                                    lastName);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "secret", "secretKey",
                                    secret_key);
                            CommonMethods.createStringPreference(
                                    LoginActivity.this, "userDetails", "referral_code",
                                    referral_code);
                        }
                    } else {
                        toProceed = "no";
                        messageJson = jsonObj.getString("message");
                    }
                } else {
                    // Login Failed
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

            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            pDialog.dismiss();
            if (toProceed.contentEquals("yes")) {
                CommonMethods.createStringPreference(LoginActivity.this,
                        "skipper", "loginSkip", "skip");
                startActivity(new Intent(LoginActivity.this,
                        DashboardActivity.class));
                finish();
            } else if (toProceed.contentEquals("no")) {
                CommonMethods.showLongToast(LoginActivity.this,
                        messageJson);
            } else if (toProceed.contentEquals("otp")) {
                startActivity(new Intent(LoginActivity.this, OTPActivity.class));
                finish();
            } else {
                CommonMethods.showLongToast(LoginActivity.this,
                        "Login Failed! Try again!");
            }

        }

    }

    @Override
    public void onResult(LoadPeopleResult arg0) {
        // TODO Auto-generated method stub

    }

}
