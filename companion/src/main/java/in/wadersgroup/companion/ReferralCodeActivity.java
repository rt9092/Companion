package in.wadersgroup.companion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by romil_wadersgroup on 9/4/16.
 */
public class ReferralCodeActivity extends AppCompatActivity {

    EditText etReferralCode;

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

        setContentView(R.layout.activity_referral_code);

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("REFERRAL REWARDS");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");

        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");
        toolbar_title.setTypeface(arvoRegular);

        etReferralCode = (EditText) findViewById(R.id.etPromoCode);
        Button finishReferral = (Button) findViewById(R.id.bApplyPromoCode);
        Button skip = (Button) findViewById(R.id.bSkipThis);

        etReferralCode.setTypeface(arvoRegular);
        finishReferral.setTypeface(arvoRegular);
        skip.setTypeface(arvoRegular);

        finishReferral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etReferralCode.getText().toString().contentEquals("")) {
                    CommonMethods.showShortToast(ReferralCodeActivity.this, "Please enter the Promo Code");
                } else {
                    new HitPromoServer().execute(etReferralCode.getText().toString().trim());
                }
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReferralCodeActivity.this, DashboardActivity.class));
                finish();
            }
        });

    }

    class HitPromoServer extends AsyncTask<String, Void, Void> {

        ProgressDialog pDialog;
        boolean successFlag = false;
        String res, message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ReferralCodeActivity.this);
            pDialog.setMessage("Validating your Promo Code...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {


            OkHttpClient client = new OkHttpClient();
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(ReferralCodeActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(ReferralCodeActivity.this, "secret",
                                "secretKey"), "utf-8");
                String ref_code = URLEncoder.encode(params[0], "utf-8");

                System.out.println("Referral Details: " + email + " " + secret + " " + ref_code);

                Request request = new Request.Builder()
                        .url(Constants.PROMO_URL + email + "&secret_key=" + secret + "&ref_code=" + ref_code)
                        .build();

                Response response = client.newCall(request).execute();
                res = response.body().string();
                System.out.println("Promotion: " + res);
                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String success = jsonObj.getString("success").trim();
                    if (success.contentEquals("yes")) {
                        successFlag = true;
                    } else {
                        successFlag = false;
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            pDialog.dismiss();
            if (successFlag) {
                try {
                    JSONObject fullJson = new JSONObject(res.trim());
                    message = fullJson.getString("message");
                    CommonMethods.showShortToast(ReferralCodeActivity.this, message);
                    startActivity(new Intent(ReferralCodeActivity.this, DashboardActivity.class));
                    finish();

                } catch (JSONException e) {
                    AnalyticsApplication.getInstance().trackException(e);
                    e.printStackTrace();
                }
            } else {
                JSONObject fullJson = null;
                try {
                    fullJson = new JSONObject(res.trim());
                    message = fullJson.getString("message");
                    CommonMethods.showShortToast(ReferralCodeActivity.this, message);
                } catch (JSONException e) {
                    AnalyticsApplication.getInstance().trackException(e);
                    e.printStackTrace();
                }

            }

        }
    }

}
