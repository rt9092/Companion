package in.wadersgroup.companion;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.AdapterView;
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
 * Created by romil_wadersgroup on 2/3/16.
 */
public class ReportedBlocksActivity extends AppCompatActivity {

    TextView errorText;
    ImageView nothingImage;
    ListView listView;
    RoadblockAdapter adapterRoadblock;

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

        setContentView(R.layout.activity_reported_blocks);

        if (CommonMethods.isNetworkAvailable(ReportedBlocksActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(ReportedBlocksActivity.this);

        }

        /*if (CommonMethods.isLocationAvailable(ReportedBlocksActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(ReportedBlocksActivity.this);

        }*/

        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        errorText = (TextView) findViewById(R.id.tvError);
        nothingImage = (ImageView) findViewById(R.id.ivNothing);
        errorText.setTypeface(arvoRegular);
        errorText.setVisibility(View.GONE);
        nothingImage.setVisibility(View.GONE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("REPORTS");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        toolbar_title.setTypeface(arvoRegular);


    }

    List<HashMap<String, String>> roadblockArray = new ArrayList<HashMap<String, String>>();

    class FetchUserBlocks extends AsyncTask<Void, Void, Void> {

        String res;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ReportedBlocksActivity.this);
            pDialog.setMessage("Getting your Reports...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            listView = (ListView) findViewById(R.id.listRoadblocks);
            if (roadblockArray.size() != 0) {

                adapterRoadblock = new RoadblockAdapter(ReportedBlocksActivity.this, roadblockArray);

                listView.setAdapter(adapterRoadblock);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {

                        Intent i = new Intent(ReportedBlocksActivity.this, RoadblockRemovalActivity.class);
                        i.putExtra("block_id", roadblockArray.get(position).get("block_id"));
                        i.putExtra("location", roadblockArray.get(position).get("location"));
                        i.putExtra("cause", roadblockArray.get(position).get("cause"));
                        i.putExtra("sourceActivity", "ReportedBlocksActivity");
                        startActivity(i);
                    }
                });
            } else {
                errorText.setVisibility(View.VISIBLE);
                nothingImage.setVisibility(View.VISIBLE);
                errorText.setText("You have not reported any roadblocks" +
                        " yet! Update roadblocks when you get stuck in one!");
            }

            pDialog.dismiss();

        }

        @Override
        protected Void doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(ReportedBlocksActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(ReportedBlocksActivity.this, "secret",
                                "secretKey"), "utf-8");
                Request request = new Request.Builder()
                        .url(Constants.USER_BLOCKS_URL + email + "&secret_key=" + secret)
                        .build();

                Response response = client.newCall(request).execute();
                res = response.body().string();
                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String success = jsonObj.getString("success").trim();
                    if (success.contentEquals("yes")) {
                        JSONArray jsonResponse = jsonObj.getJSONArray("response");
                        for (int i = 0; i < jsonResponse.length(); i++) {
                            JSONObject tempObj = jsonResponse.getJSONObject(i);
                            String block_id = tempObj.getString("block_id");
                            String location = tempObj.getString("location");
                            String cause = tempObj.getString("cause");
                            String address = tempObj.getString("address");
                            String confidence = tempObj.getString("active");

                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("block_id", block_id);
                            hm.put("location", location);
                            hm.put("cause", cause);
                            hm.put("active", confidence);
                            hm.put("address", address);

                            roadblockArray.add(hm);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        roadblockArray.clear();
        if (adapterRoadblock != null) {
            adapterRoadblock.notifyDataSetChanged();
        }
        if (CommonMethods.isNetworkAvailable(ReportedBlocksActivity.this)) {

            new FetchUserBlocks().execute();
        } else {
            CommonMethods.wirelessDialog(ReportedBlocksActivity.this);

        }

    }


}
