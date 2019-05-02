package in.wadersgroup.companion;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class RewardsActivity extends AppCompatActivity {

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

        setContentView(R.layout.activity_rewards);

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("EARN REWARDS");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        TextView shareCodeInfo = (TextView) findViewById(R.id.tvShareYourCode);
        final TextView codeShare = (TextView) findViewById(R.id.tvShareCode);
        TextView codeShareInst = (TextView) findViewById(R.id.tvShareBaby);

        Button shareFriends = (Button) findViewById(R.id.bShareFriends);
        Button updateRoadblocks = (Button) findViewById(R.id.bUpdateRoadblocks);

        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Bold.ttf");
        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        shareCodeInfo.setTypeface(arvoBold);
        codeShare.setTypeface(arvoBold);
        codeShareInst.setTypeface(arvoRegular);
        shareFriends.setTypeface(arvoBold);
        updateRoadblocks.setTypeface(arvoBold);
        toolbar_title.setTypeface(arvoRegular);
        codeShare.setText(CommonMethods.readStringPreference(RewardsActivity.this, "userDetails", "referral_code"));

        codeShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simple text", codeShare
                        .getText().toString());
                clipboard.setPrimaryClip(clip);

                CommonMethods.showShortToast(RewardsActivity.this,
                        "Copied to Clipboard");

            }
        });

        shareFriends.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                shareApp(codeShare.getText().toString());

            }
        });

        updateRoadblocks.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                startActivity(new Intent(RewardsActivity.this,
                        DataUploadActivity.class));

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareApp(String code) {
        // TODO Auto-generated method stub

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String uri = ("https://play.google.com/store/apps/details?id=in.wadersgroup.companion");
        intent.putExtra(
                Intent.EXTRA_TEXT,
                "Companion - Making Travel Smarter."
                        + "\nDownload the app and earn 25 reward points. "
                        + "And guess what? Your friend gets 25 points too. \nUse code: "
                        + code + "\n\n" + uri);

        try {
            startActivity(Intent.createChooser(intent, "Share via"));
        } catch (android.content.ActivityNotFoundException ex) {
            AnalyticsApplication.getInstance().trackException(ex);
            // (handle error)
        }

    }

}
