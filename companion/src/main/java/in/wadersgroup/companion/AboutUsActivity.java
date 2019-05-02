package in.wadersgroup.companion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AboutUsActivity extends AppCompatActivity {

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

        setContentView(R.layout.activity_about_us);

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("ABOUT US");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        TextView aboutGantavya = (TextView) findViewById(R.id.tvGantavyaAbout);
        TextView superheroes = (TextView) findViewById(R.id.tvSuperheroes);

        Button rateGoogle = (Button) findViewById(R.id.bRateGoogle);
        Button likeFacebook = (Button) findViewById(R.id.bLikeFacebook);
        Button visitWebsite = (Button) findViewById(R.id.bVisitWebsite);
        Button watchVideo = (Button) findViewById(R.id.bWatchVideo);

        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Bold.ttf");
        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        aboutGantavya.setTypeface(arvoRegular);
        superheroes.setTypeface(arvoRegular);

        SpannableStringBuilder ssb = new SpannableStringBuilder("Made with     in India");
        Bitmap love = BitmapFactory.decodeResource(getResources(), R.drawable.with_love);
        ssb.setSpan(new ImageSpan(love), 10, 13, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        superheroes.setText(ssb, TextView.BufferType.SPANNABLE);

        rateGoogle.setTypeface(arvoBold);
        likeFacebook.setTypeface(arvoBold);
        visitWebsite.setTypeface(arvoBold);
        watchVideo.setTypeface(arvoBold);
        toolbar_title.setTypeface(arvoRegular);

        watchVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.youtube.com/watch?v=Dnht3mskAWY");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        visitWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://companion.wadersgroup.in");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        likeFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.facebook.com/atCompanion/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        rateGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = getApplicationContext().getPackageName();
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + packageName);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
