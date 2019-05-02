package in.wadersgroup.companion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
 * Created by romil_wadersgroup on 3/3/16.
 */
public class PromotionActivity extends AppCompatActivity {

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

        setContentView(R.layout.activity_promotions);

        if (CommonMethods.isNetworkAvailable(PromotionActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(PromotionActivity.this);

        }

        /*if (CommonMethods.isLocationAvailable(PromotionActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(PromotionActivity.this);

        }*/

        Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("PROMOTIONS");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        toolbar_title.setTypeface(arvoRegular);

        //final EditText enterPromo = (EditText) findViewById(R.id.etPromoCode);
        Button applyPromo = (Button) findViewById(R.id.bApplyPromo);
        Button goToRewards = (Button) findViewById(R.id.bRewardsScreen);
        TextView pointsText = (TextView) findViewById(R.id.tvPromoCode);

        applyPromo.setTypeface(arvoRegular);
        pointsText.setTypeface(arvoRegular);
        goToRewards.setTypeface(arvoRegular);

        applyPromo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonMethods.isNetworkAvailable(PromotionActivity.this)) {
                    new HitPromoServer().execute();
                } else {
                    CommonMethods.wirelessDialog(PromotionActivity.this);

                }
            }
        });

        goToRewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(PromotionActivity.this, RewardsActivity.class));

            }
        });

    }

    class HitPromoServer extends AsyncTask<Void, Void, Void> {

        ProgressDialog pDialog;
        boolean successFlag = false;
        String res, message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(PromotionActivity.this);
            pDialog.setMessage("Please Wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {


            OkHttpClient client = new OkHttpClient();
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(PromotionActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(PromotionActivity.this, "secret",
                                "secretKey"), "utf-8");
                Request request = new Request.Builder()
                        .url(Constants.USER_ACCOUNT_URL + email + "&secret_key=" + secret)
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
                    JSONObject responseJson = fullJson.getJSONObject("response");
                    String points = responseJson.getString("points");
                    String min_points = responseJson.getString("min_redeem_points");
                    String rupees = responseJson.getString("money");

                    if (Integer.valueOf(points) >= Integer.valueOf(min_points)) {
                        Intent i = new Intent(PromotionActivity.this, RedeemActivity.class);
                        i.putExtra("money", rupees);
                        startActivity(i);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PromotionActivity.this);
                        builder.setMessage("Oops! You need to have at least " + min_points + " points to redeem. Currently you have " + points + " points. Keep collecting.");
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                } catch (JSONException e) {
                    AnalyticsApplication.getInstance().trackException(e);
                    e.printStackTrace();
                }
            } else {
                CommonMethods.showShortToast(PromotionActivity.this, message);
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
