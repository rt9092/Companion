package in.wadersgroup.companion;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    TextView rewardPoints;
    ImageView rewardCoins;
    Animation rotateImage;

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

        setContentView(R.layout.activity_profile);

        if (CommonMethods.isNetworkAvailable(ProfileActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(ProfileActivity.this);

        }

        /*if (CommonMethods.isLocationAvailable(ProfileActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(ProfileActivity.this);

        }*/

        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");
        final Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Bold.ttf");

        TextView profileName = (TextView) findViewById(R.id.tvProfileName);
        TextView profileEmail = (TextView) findViewById(R.id.tvProfileEmail);
        TextView profilePhone = (TextView) findViewById(R.id.tvProfilePhone);

        rewardPoints = (TextView) findViewById(R.id.tvRewardPoints);
        // TextView badgeName = (TextView) findViewById(R.id.tvBadgeName);

        // ImageView badgeAvatar = (ImageView) findViewById(R.id.ivBadgeAvatar);
        rewardCoins = (ImageView) findViewById(R.id.ivRewardCoins);

       /* badgeAvatar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                startActivity(new Intent(ProfileActivity.this,
                        BadgeAvatarActivity.class));

            }
        });*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("PROFILE");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        profileName.setTypeface(arvoBold);
        profileEmail.setTypeface(arvoBold);
        profilePhone.setTypeface(arvoBold);
        toolbar_title.setTypeface(arvoRegular);

        rewardPoints.setTypeface(arvoBold);
        //  badgeName.setTypeface(arvoBold);

        profileName.setText(CommonMethods.readStringPreference(
                ProfileActivity.this, "userDetails", "fName")
                + " "
                + CommonMethods.readStringPreference(ProfileActivity.this,
                "userDetails", "lName"));
        profileEmail.setText(CommonMethods.readStringPreference(
                ProfileActivity.this, "userDetails", "email"));
        profilePhone.setText(CommonMethods.readStringPreference(
                ProfileActivity.this, "userDetails", "mobile"));

        CardView profileCard = (CardView) findViewById(R.id.cardProfile);
        CardView rewardCard = (CardView) findViewById(R.id.cardRewards);

        rotateImage = AnimationUtils.loadAnimation(this,
                R.anim.anim_rotate);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator()); // add this
        fadeIn.setDuration(700);

        rewardPoints.setAnimation(fadeIn);
        //  badgeName.setAnimation(fadeIn);

        //  badgeAvatar.setAnimation(rotateImage);

        if (getIntent().getStringExtra("points") != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setMessage("Congratulations. You just received " + getIntent().getStringExtra("points").trim() + " incentive points. Keep up the good work!")
                    .setTitle("Congrats!");
            AlertDialog dialog = builder.create();
            dialog.show();
            rewardPoints.setText("You have " + getIntent().getStringExtra("points").trim() + " reward points.");
        }

        if (CommonMethods.isNetworkAvailable(ProfileActivity.this)) {

            new HitPromoServer().execute();
        } else {
            CommonMethods.wirelessDialog(ProfileActivity.this);

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

    class HitPromoServer extends AsyncTask<Void, Void, Void> {

        ProgressDialog pDialog;
        boolean successFlag = false;
        String res, message, points;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setMessage("Getting your Incentive Points...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {


            OkHttpClient client = new OkHttpClient();
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(ProfileActivity.this,
                                "userDetails", "email"), "utf-8");
                String secret = URLEncoder.encode(CommonMethods
                        .readStringPreference(ProfileActivity.this, "secret",
                                "secretKey"), "utf-8");
                Request request = new Request.Builder()
                        .url(Constants.USER_ACCOUNT_URL + email + "&secret_key=" + secret)
                        .build();

                Response response = client.newCall(request).execute();
                res = response.body().string();
                if (res != null) {
                    JSONObject jsonObj = new JSONObject(res.trim());
                    String success = jsonObj.getString("success").trim();
                    if (success.contentEquals("yes")) {
                        successFlag = true;
                        JSONObject responseJson = jsonObj.getJSONObject("response");
                        message = jsonObj.getString("message").trim();
                        points = responseJson.getString("points").trim();
                    } else {
                        successFlag = false;
                        message = jsonObj.getString("message").trim();
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
            rewardCoins.setAnimation(rotateImage);
            if (successFlag) {
                rewardPoints.setText("You have " + points + " reward points.");
            } else {
                CommonMethods.showShortToast(ProfileActivity.this, message);
            }

        }
    }

}
