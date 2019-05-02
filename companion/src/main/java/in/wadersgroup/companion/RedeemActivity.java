package in.wadersgroup.companion;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
 * Created by romil_wadersgroup on 26/3/16.
 */
public class RedeemActivity extends AppCompatActivity {

    Spinner operator, circle;
    ArrayList<HashMap<String, String>> operatorCodesList = new ArrayList<>();
    ArrayList<HashMap<String, String>> circleCodesList = new ArrayList<>();
    HashMap<String, String> operatorHashMap = new HashMap<>();
    HashMap<String, String> circleHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_redeem_points);

        if (CommonMethods.isNetworkAvailable(RedeemActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(RedeemActivity.this);

        }

        /*if (CommonMethods.isLocationAvailable(RedeemActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(RedeemActivity.this);

        }*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("REDEEM POINTS");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final String money = getIntent().getStringExtra("money");

        TextView rupees = (TextView) findViewById(R.id.tvRupees);
        operator = (Spinner) findViewById(R.id.spinnerOperator);
        circle = (Spinner) findViewById(R.id.spinnerCircle);
        final EditText amount = (EditText) findViewById(R.id.etAmount);
        Button recharge = (Button) findViewById(R.id.bRecharge);

        if (CommonMethods.isNetworkAvailable(RedeemActivity.this)) {

            new HitRechargeServer().execute();
        } else {
            CommonMethods.wirelessDialog(RedeemActivity.this);

        }


        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");
        toolbar_title.setTypeface(arvoRegular);
        rupees.setTypeface(arvoRegular);
        rupees.setText("You have " + money + " rupees for Recharge. Enter the details below to redeem your money.");

        recharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!amount.getText().toString().contentEquals("")) {
                    if (Integer.valueOf(amount.getText().toString()) < Integer.valueOf(money)) {
                        if (CommonMethods.isNetworkAvailable(RedeemActivity.this)) {
                            new GetRecharge().execute(operatorHashMap.get(operator.getSelectedItem().toString().trim()), circleHashMap.get(circle.getSelectedItem().toString().trim()), amount.getText().toString().trim());
                        } else {
                            CommonMethods.wirelessDialog(RedeemActivity.this);

                        }
                    } else {
                        CommonMethods.showShortToast(RedeemActivity.this, "Recharge amount has to be less than total money.");
                    }
                } else {
                    CommonMethods.showShortToast(RedeemActivity.this, "Please enter recharge amount");
                }
            }
        });


    }

    class HitRechargeServer extends AsyncTask<Void, Void, Void> {

        ProgressDialog pDialog;
        boolean successFlag = false;
        String res, message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(RedeemActivity.this);
            pDialog.setMessage("Please Wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {


            OkHttpClient client = new OkHttpClient();
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(RedeemActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(RedeemActivity.this, "secret",
                                "secretKey"), "utf-8");
                Request request = new Request.Builder()
                        .url(Constants.RECHARGE_DATA_URL + email + "&secret_key=" + secret)
                        .build();

                client.connectTimeoutMillis();
                client.readTimeoutMillis();
                client.writeTimeoutMillis();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    res = response.body().string();
                } else {
                    System.out.println("Timeout ho gaya BC");
                }
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

            List<String> spinnerOperators = new ArrayList<>();
            List<String> spinnerCircles = new ArrayList<>();

            pDialog.dismiss();
            if (successFlag) {
                try {
                    JSONObject fullJson = new JSONObject(res.trim());
                    JSONObject responseJson = fullJson.getJSONObject("response");
                    JSONArray operatorCodes = responseJson.getJSONArray("operator_codes");
                    JSONArray circleCodes = responseJson.getJSONArray("circle_codes");

                    for (int i = 0; i < operatorCodes.length(); i++) {

                        HashMap<String, String> opCodes = new HashMap<>();
                        String opName = operatorCodes.getJSONObject(i).getString("operator_name");
                        String opCode = operatorCodes.getJSONObject(i).getString("operator_code");
                        opCodes.put(opName, opCode);
                        operatorCodesList.add(opCodes);
                        spinnerOperators.add(opName);
                        operatorHashMap.put(opName, opCode);

                    }

                    for (int i = 0; i < circleCodes.length(); i++) {

                        HashMap<String, String> circCodes = new HashMap<>();
                        String circleName = circleCodes.getJSONObject(i).getString("circle_name");
                        String circleCode = circleCodes.getJSONObject(i).getString("circle_code");
                        circCodes.put(circleName, circleCode);
                        circleCodesList.add(circCodes);
                        spinnerCircles.add(circleName);
                        circleHashMap.put(circleName, circleCode);

                    }

                    ArrayAdapter<String> adapterOperators = new ArrayAdapter<>(RedeemActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, spinnerOperators);
                    ArrayAdapter<String> adapterCircles = new ArrayAdapter<>(RedeemActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, spinnerCircles);
                    operator.setAdapter(adapterOperators);
                    circle.setAdapter(adapterCircles);


                } catch (JSONException e) {
                    AnalyticsApplication.getInstance().trackException(e);
                    e.printStackTrace();
                }
            } else {
                CommonMethods.showShortToast(RedeemActivity.this, "Something went wrong. Try Again!");
                finish();
            }

        }
    }

    class GetRecharge extends AsyncTask<String, Void, Void> {

        ProgressDialog pDialog;
        boolean successFlag = false;
        String res, message;
        boolean jsonFlag = false;
        boolean socketFlag = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(RedeemActivity.this);
            pDialog.setMessage("Getting you a recharge...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {


            OkHttpClient client = new OkHttpClient();
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(RedeemActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(RedeemActivity.this, "secret",
                                "secretKey"), "utf-8");
                String operator = URLEncoder.encode(params[0], "utf-8");
                String circle = URLEncoder.encode(params[1], "utf-8");
                String amount = URLEncoder.encode(params[2], "utf-8");
                Request request = new Request.Builder()
                        .url(Constants.RECHARGE_URL + email + "&secret_key=" + secret + "&operator=" + operator + "&circle=" + circle + "&amount=" + amount)
                        .build();

                /*client.connectTimeoutMillis();
                client.readTimeoutMillis();
                client.writeTimeoutMillis();*/

                Response response = client.newCall(request).execute();

                res = response.body().string();
                System.out.println("Timeout: " + res);

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
                jsonFlag = true;
            } catch (IOException e) {
                AnalyticsApplication.getInstance().trackException(e);
                socketFlag = true;
            } catch (JSONException e) {
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
                jsonFlag = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            String transaction_id;

            pDialog.dismiss();
            if (jsonFlag != true) {
                if (successFlag) {
                    try {
                        JSONObject fullJson = new JSONObject(res.trim());
                        JSONObject responseJson = fullJson.getJSONObject("response");
                        transaction_id = responseJson.getString("transaction_id");
                        String message = fullJson.getString("message");
                        //CommonMethods.showShortToast(RedeemActivity.this, message);
                        CommonMethods.commonDialog(RedeemActivity.this, message+"\n Transaction ID: "+transaction_id);
                        //finish();


                    } catch (JSONException e) {
                        AnalyticsApplication.getInstance().trackException(e);
                        e.printStackTrace();
                        CommonMethods.showShortToast(RedeemActivity.this, "Failed! Try again!");
                    }
                } else {
                    try {
                        if (res != null) {
                            JSONObject fullJson = new JSONObject(res.trim());
                            JSONObject responseJson = fullJson.getJSONObject("response");
                            String message = fullJson.getString("message");
                            transaction_id = responseJson.getString("transaction_id");
                            //CommonMethods.showShortToast(RedeemActivity.this, message);
                            CommonMethods.commonDialog(RedeemActivity.this, message+"\n Transaction ID: "+transaction_id);
                            //finish();
                        } else {
                            //CommonMethods.showShortToast(RedeemActivity.this, "Recharge Pending");
                            CommonMethods.commonDialog(RedeemActivity.this, "Recharge Pending");
                        }
                    } catch (JSONException e) {
                        AnalyticsApplication.getInstance().trackException(e);
                        e.printStackTrace();
                        CommonMethods.showShortToast(RedeemActivity.this, "Failed! Try again!");
                    }

                }
            } else if (socketFlag) {
                //CommonMethods.showShortToast(RedeemActivity.this, "Recharge Pending");
                CommonMethods.commonDialog(RedeemActivity.this, "Recharge Pending");
            } else {
                CommonMethods.showShortToast(RedeemActivity.this, "Failed! Try again!");
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
