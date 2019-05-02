package in.wadersgroup.companion;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
 * Created by romil_wadersgroup on 13/6/16.
 */
public class YatraSpecialActivity extends AppCompatActivity {

    TextView errorText;
    ImageView nothingImage;
    List<HashMap<String, String>> notificationArray = new ArrayList<>();
    Button setNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            int sdkVersion = Build.VERSION.SDK_INT;
            if (sdkVersion >= 21) {

                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setStatusBarColor(getResources().getColor(R.color.statusbar));
                }
            }

            setContentView(R.layout.activity_yatra_special);

            if (CommonMethods.isNetworkAvailable(YatraSpecialActivity.this)) {
                // Continue
            } else {
                CommonMethods.wirelessDialog(YatraSpecialActivity.this);
            }

            final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                    "fonts/Arvo-Regular.ttf");
            final Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                    "fonts/Arvo-Bold.ttf");

            setNotif = (Button) findViewById(R.id.bSetNotificationReceive);
            setNotif.setTypeface(arvoBold);

            errorText = (TextView) findViewById(R.id.tvError);
            nothingImage = (ImageView) findViewById(R.id.ivNothing);
            errorText.setTypeface(arvoRegular);
            errorText.setVisibility(View.GONE);
            nothingImage.setVisibility(View.GONE);

            Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
            TextView toolbar_title = (TextView) toolbar
                    .findViewById(R.id.toolbar_title);
            toolbar_title.setText("KAILASH MANSAROVAR");

            setSupportActionBar(toolbar);

            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar_title.setTypeface(arvoRegular);
            try {
                new FetchYatraNotifications().execute();
            } catch (Exception e) {
                CommonMethods.showLongToast(YatraSpecialActivity.this, e.toString());
            }

            setNotif.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (setNotif.getText().toString().trim().contentEquals("STOP RECEIVING YATRA NOTIFICATIONS")) {
                        new SetNotificationButton().execute("no");
                    } else if (setNotif.getText().toString().trim().contentEquals("START RECEIVING YATRA NOTIFICATIONS")) {
                        new SetNotificationButton().execute("yes");
                    }
                }
            });
        } catch (Exception e) {
            CommonMethods.showLongToast(YatraSpecialActivity.this, e.toString());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    class FetchYatraNotifications extends AsyncTask<Void, Void, Void> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(YatraSpecialActivity.this);
            pDialog.setMessage("Getting Yatra Notifications...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            ListView listView = (ListView) findViewById(R.id.listNotifications);
            if (notificationArray.size() != 0) {
                listView.setAdapter(new YatraAdapter(YatraSpecialActivity.this, notificationArray));

                /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {

                        Intent i = new Intent(CityBasedActivity.this, PlaceDetailsBigActivity.class);
                        i.putExtra("name", touristArray.get(position).get("name"));
                        i.putExtra("address", touristArray.get(position).get("address"));
                        i.putExtra("opening_time", touristArray.get(position).get("opening_time"));
                        i.putExtra("closing_time", touristArray.get(position).get("closing_time"));
                        i.putExtra("about", touristArray.get(position).get("about"));
                        i.putExtra("rating", touristArray.get(position).get("public_rating"));
                        i.putExtra("image_uri", touristArray.get(position).get("image_uri"));
                        i.putExtra("location", touristArray.get(position).get("location"));
                        i.putExtra("place_id", touristArray.get(position).get("place_id"));
                        startActivity(i);
                    }
                });*/
            } else {
                errorText.setVisibility(View.VISIBLE);
                nothingImage.setVisibility(View.VISIBLE);
                errorText.setText("Sorry! Currently we do not have any Notifications." +
                        "You will get the updates as soon as the officials send us the updates. Thank you.");
            }

            pDialog.dismiss();
            try {
                new YatraNotificationButtonStatus().execute();
            } catch (Exception e) {
                CommonMethods.showLongToast(YatraSpecialActivity.this, e.toString());
            }

        }

        @Override
        protected Void doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            try {
                Request request = new Request.Builder()
                        .url("http://wadersgroup.in/companion_apis/kailash/get_messages.php")
                        .build();

                Response response = client.newCall(request).execute();
                String res = response.body().string();
                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String success = jsonObj.getString("success").trim();
                    if (success.contentEquals("yes")) {
                        JSONArray notifArray = jsonObj.getJSONArray("response");

                        for (int i = 0; i < notifArray.length(); i++) {
                            JSONObject notifObject = notifArray
                                    .getJSONObject(i);
                            String notifMessage = notifObject
                                    .getString("message");
                            String notifDate = notifObject
                                    .getString("time_stamp").trim().split(" ")[0];
                            String notifTime = notifObject
                                    .getString("time_stamp").trim().split(" ")[1];


                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("notifMessage", notifMessage);
                            hm.put("notifDate", notifDate);
                            hm.put("notifTime", notifTime);

                            notificationArray.add(hm);
                        }

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

    class YatraNotificationButtonStatus extends AsyncTask<Void, Void, Void> {

        boolean buttonStat = false;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (buttonStat) {
                setNotif.setText("STOP RECEIVING YATRA NOTIFICATIONS");
            } else {
                setNotif.setText("START RECEIVING YATRA NOTIFICATIONS");
            }

        }

        @Override
        protected Void doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(YatraSpecialActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(YatraSpecialActivity.this, "secret",
                                "secretKey"), "utf-8");
                Request request = new Request.Builder()
                        .url("http://wadersgroup.in/companion_apis/kailash/switch.php?method=get&email=" + email + "&secret_key=" + secret)
                        .build();

                Response response = client.newCall(request).execute();
                String res = response.body().string();
                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String success = jsonObj.getString("success").trim();
                    if (success.contentEquals("yes")) {
                        String switchStat = jsonObj.getString("status");

                        if (switchStat.trim().contentEquals("yes")) {
                            buttonStat = true;
                        } else {
                            buttonStat = false;
                        }

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

    class SetNotificationButton extends AsyncTask<String, Void, Void> {

        ProgressDialog pDialog;
        Boolean apiCheck, buttonCheck;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(YatraSpecialActivity.this);
            pDialog.setMessage("Recording your Response...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (apiCheck) {
                if (buttonCheck) {
                    setNotif.setText("STOP RECEIVING YATRA NOTIFICATIONS");
                } else {
                    setNotif.setText("START RECEIVING YATRA NOTIFICATIONS");
                }
            } else {
                CommonMethods.showLongToast(YatraSpecialActivity.this, "Something went wrong. Try Again.");
            }

            pDialog.dismiss();

        }

        @Override
        protected Void doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient();
            try {
                if (params[0].trim().contentEquals("yes")) {
                    buttonCheck = true;
                } else {
                    buttonCheck = false;
                }
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(YatraSpecialActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(YatraSpecialActivity.this, "secret",
                                "secretKey"), "utf-8");
                String status = URLEncoder.encode(params[0], "utf-8");
                Request request = new Request.Builder()
                        .url("http://wadersgroup.in/companion_apis/kailash/switch.php?method=set&status=" + status + "&email=" + email + "&secret_key=" + secret)
                        .build();

                Response response = client.newCall(request).execute();
                String res = response.body().string();
                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String success = jsonObj.getString("success").trim();
                    if (success.contentEquals("yes")) {
                        apiCheck = true;
                    } else {
                        apiCheck = false;
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
