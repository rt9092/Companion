package in.wadersgroup.companion;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by romil_wadersgroup on 19/4/16.
 */
public class AuthorityActivity extends AppCompatActivity {

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

        setContentView(R.layout.activity_confirmation_screen);

        if (CommonMethods.isNetworkAvailable(AuthorityActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(AuthorityActivity.this);
        }

        if (CommonMethods.isLocationAvailable(AuthorityActivity.this)) {
            // Continue
        } else {
            CommonMethods.locationDialog(AuthorityActivity.this);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.dashboard_toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("GOVERNMENT");

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");

        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Bold.ttf");
        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        toolbar_title.setTypeface(arvoRegular);


        final EditText authCode = (EditText) findViewById(R.id.etAuthCode);
        Button next = (Button) findViewById(R.id.bNextAuthority);
        Button skip = (Button) findViewById(R.id.bSkipAuthority);
        ImageButton info = (ImageButton) findViewById(R.id.ibAuthCodeInfo);

        authCode.setTypeface(arvoRegular);
        next.setTypeface(arvoBold);
        skip.setTypeface(arvoBold);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonMethods.commonDialog(AuthorityActivity.this, "We have collaborated with a lot of Government Organisations." +
                        " If you are one of them, then we surely would have provided you with a Code." +
                        " Provide that code in the Box, else if you don't have any code, just skip this. To our users, we would like to say that" +
                        " we have collaborated with organisations so that we can get the roadblocks verified.");
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AuthorityActivity.this, ReferralCodeActivity.class));
                finish();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(authCode.getText().toString().trim().contentEquals("")){
                    CommonMethods.showLongToast(AuthorityActivity.this, "Please enter the Code.");
                }else{

                }
            }
        });

    }
}
