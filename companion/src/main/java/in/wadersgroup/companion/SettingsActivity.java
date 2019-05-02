package in.wadersgroup.companion;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class SettingsActivity extends AppCompatActivity {


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        super.onCreate(savedInstanceState);

        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if (sdkVersion >= 21) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.statusbar));
        }

        setContentView(R.layout.activity_settings);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar
                .findViewById(R.id.toolbar_title);
        toolbar_title.setText("SETTINGS");

        setSupportActionBar(toolbar);

        final Drawable upArrow = getResources().getDrawable(
                R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setHomeButtonEnabled(true);

        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        toolbar_title.setTypeface(arvoRegular);

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new MyPreferenceFragment())
                .commit();

    }

    public static class MyPreferenceFragment extends PreferenceFragment
            implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onStart() {
            // TODO Auto-generated method stub
            super.onStart();

        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);


            Preference signOutPref = findPreference("sign_out_preference");

            Preference privacy = findPreference("privacy_preference");
            Preference terms = findPreference("terms_preference");


            privacy.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Uri uri = Uri.parse("http://companion.wadersgroup.in/privacy.html");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);

                    return false;
                }
            });

            terms.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Uri uri = Uri.parse("http://companion.wadersgroup.in/tnc.html");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);

                    return false;
                }
            });

            signOutPref
                    .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            // TODO Auto-generated method stub

							/*if (mGoogleApiClient.isConnected()) {
                                Plus.AccountApi
										.clearDefaultAccount(mGoogleApiClient);
								mGoogleApiClient.disconnect();

								startActivity(new Intent(getActivity(),
										LoginActivity.class)
										.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
												& Intent.FLAG_ACTIVITY_NEW_TASK));

							}*/

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String checkRegx = "";
                                    CommonMethods.createStringPreference(getActivity(), "skipper", "loginSkip", checkRegx);
                                    CommonMethods.createStringPreference(getActivity(), "skipper", "otpSkip", checkRegx);
                                    Intent intent = new Intent(getActivity(), SignInToggleActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    getActivity().finish();
                                }
                            });
                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            });

                            builder.setMessage("Are you sure you want to Sign Out?");
                            AlertDialog dialog = builder.create();

                            dialog.show();

                            return false;
                        }
                    });

        }

        @Override
        public void onConnectionFailed(ConnectionResult arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onConnected(Bundle arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onConnectionSuspended(int arg0) {
            // TODO Auto-generated method stub

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

}
