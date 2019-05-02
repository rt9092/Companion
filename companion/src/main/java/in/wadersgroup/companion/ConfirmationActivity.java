package in.wadersgroup.companion;

// (Activity) Shown when user Registers via Google and has to supply his Mobile Number here

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
public class ConfirmationActivity extends AppCompatActivity {

    EditText etPhone;
    ProgressDialog pDialog;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SECRET = "secret_key";
    String toProceed = "";

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

        setContentView(R.layout.activity_confirmation_screen);

        if (CommonMethods.isNetworkAvailable(ConfirmationActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(ConfirmationActivity.this);
        }

        /*if (CommonMethods.isLocationAvailable(ConfirmationActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(ConfirmationActivity.this);
        }*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.dashboard_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("CONFIRMATION");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");

        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Bold.ttf");
        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        TextView tvFirstName = (TextView) findViewById(R.id.tvFirstName);
        TextView tvLastName = (TextView) findViewById(R.id.tvLastName);

        TextView tvPhone = (TextView) findViewById(R.id.tvPhone);

        EditText etFirstName = (EditText) findViewById(R.id.etFirstName);
        EditText etLastName = (EditText) findViewById(R.id.etLastName);

        etPhone = (EditText) findViewById(R.id.etPhone);

        final Button finish = (Button) findViewById(R.id.bNext);
        ImageButton phoneInfo = (ImageButton) findViewById(R.id.ibPhoneInfo);
        finish.setText("Finish");

        tvFirstName.setTypeface(arvoBold);
        tvLastName.setTypeface(arvoBold);
        tvPhone.setTypeface(arvoBold);

        etFirstName.setTypeface(arvoBold);
        etLastName.setTypeface(arvoBold);

        etPhone.setTypeface(arvoBold);

        finish.setTypeface(arvoBold);
        toolbar_title.setTypeface(arvoRegular);

        Intent iConf = getIntent();
        final String firstName = iConf.getStringExtra("fName");
        final String lastName = iConf.getStringExtra("lName");
        final String email = iConf.getStringExtra("email");

        etFirstName.setText(firstName);
        etLastName.setText(lastName);

        finish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (getPhoneNumber()) {
                    CommonMethods.createStringPreference(
                            ConfirmationActivity.this, "userDetails", "phone",
                            etPhone.getText().toString());

                    if (CommonMethods.isNetworkAvailable(ConfirmationActivity.this)) {

                        new RegisterInBackground().execute(email, etPhone.getText()
                                .toString(), firstName, lastName, CommonMethods
                                .getIMEI(ConfirmationActivity.this), "normal");
                    } else {
                        CommonMethods.wirelessDialog(ConfirmationActivity.this);
                    }

                } else {
                    CommonMethods.showLongToast(getApplicationContext(),
                            "Please Enter a Valid Phone Number");
                }

            }
        });

        phoneInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ConfirmationActivity.this);

                builder.setMessage("We use your Phone Number to "
                        + "send you information via a text message "
                        + "in situations when Internet Access is not available.");

                AlertDialog dialog = builder.create();

                dialog.show();
                TextView tvDialogMessage = (TextView) dialog
                        .findViewById(android.R.id.message);
                tvDialogMessage.setTypeface(arvoRegular);

            }
        });

    }

    // Method to get Phone Number from Edit Text Box

    public boolean getPhoneNumber() {

        String phoneNumber = etPhone.getText().toString();
        if (phoneNumber != null && phoneNumber.length() >= 10
                && phoneNumber.length() <= 11) {
            return true;
        } else {
            return false;
        }

    }

    // Class for Registering user on Companion Server in a background thread

    class RegisterInBackground extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(ConfirmationActivity.this,
                    R.style.MyTheme);
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pDialog.show();

        }

        ;

        @Override
        protected String doInBackground(String... params) {
            String msg = "";
            try {
                String email = URLEncoder.encode(params[0], "utf-8");
                String mobile_no = URLEncoder.encode(params[1], "utf-8");
                String first_name = URLEncoder.encode(params[2], "utf-8");
                String last_name = URLEncoder.encode(params[3], "utf-8");
                String imei = URLEncoder.encode(params[4], "utf-8");
                String user_type = URLEncoder.encode(params[5], "utf-8");

                String md5Hash = CommonMethods.MD5(params[3] + params[0]);

                CommonMethods.createStringPreference(
                        ConfirmationActivity.this, "secret", "secretKey",
                        md5Hash);

                String md5Encoded = URLEncoder.encode(md5Hash, "utf-8");


                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(Constants.REG_SERVER_URL
                        + email + "&mobile_no=" + mobile_no + "&first_name="
                        + first_name + "&last_name=" + last_name + "&imei="
                        + imei + "&user_type=" + user_type + "&secret_key=" + md5Encoded);

                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                String res = EntityUtils.toString(response.getEntity());
                System.out.println("JSON Res: " + res);

                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String successTag = jsonObj.getString(TAG_SUCCESS);
                    if (successTag.contentEquals("yes")) {

                        String referral_code = jsonObj.getString("referral_code");
                        CommonMethods.createStringPreference(
                                ConfirmationActivity.this, "userDetails", "referral_code",
                                referral_code);

                        toProceed = "yes";
                    } else {
                        toProceed = "no";
                    }

                } else {
                    // Registration Failed
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
                            ConfirmationActivity.this, "userDetails", "mobile",
                            etPhone.getText().toString().trim());

                    startActivity(new Intent(ConfirmationActivity.this,
                            OTPActivity.class));
                    finish();
                } else if (toProceed.contentEquals("no")) {
                    CommonMethods.showLongToast(ConfirmationActivity.this,
                            "Already Registered! Please Login!");
                    startActivity(new Intent(ConfirmationActivity.this,
                            SignInToggleActivity.class));
                    finish();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();

        pDialog.dismiss();

    }

}
