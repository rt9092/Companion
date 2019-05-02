package in.wadersgroup.companion;

// (Activity) For the user to accept anonymous updates to Companion Server

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * @author Romil
 */
public class AnonymousUpdatesActivity extends AppCompatActivity {

    ImageButton agreed;
    boolean toggle = false;
    int notificationID;

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
            window.setStatusBarColor(getResources().getColor(R.color.toolbar));
        }

        setContentView(R.layout.activity_anonymous_explanation);

        if (CommonMethods.isLocationAvailable(AnonymousUpdatesActivity.this)) {

        } else {
            CommonMethods.locationDialog(AnonymousUpdatesActivity.this);
        }

        if (CommonMethods.isNetworkAvailable(AnonymousUpdatesActivity.this)) {
            // Continue
        } else {
            CommonMethods.wirelessDialog(AnonymousUpdatesActivity.this);
        }

        notificationID = getIntent().getIntExtra("notification_id", 0);

        TextView instructions = (TextView) findViewById(R.id.tvInstructions);
        agreed = (ImageButton) findViewById(R.id.ibAgreed);

        if (CommonMethods.readStringPreference(AnonymousUpdatesActivity.this,
                "toggle", "toggleAlerts").contentEquals("alertsOn")) {
            agreed.setImageResource(R.drawable.ic_alert_on);
            toggle = true;
            // startService(new Intent(AnonymousUpdatesActivity.this,
            // AnonymousLocationService.class));

        } else {
            agreed.setImageResource(R.drawable.ic_alert_off);
            toggle = false;
            // stopService(new Intent(AnonymousUpdatesActivity.this,
            // AnonymousLocationService.class));
        }

        ImageButton cancel = (ImageButton) findViewById(R.id.ibCancel);

        final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        instructions.setTypeface(arvoRegular);

        agreed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (toggle) {

                    if (Integer.valueOf(CommonMethods.readStringPreference(AnonymousUpdatesActivity.this, "activeTrack", "notif_id")) == 345) {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(Integer.valueOf(CommonMethods.readStringPreference(AnonymousUpdatesActivity.this, "activeTrack", "notif_id")));
                    }

                    CommonMethods.showLongToast(AnonymousUpdatesActivity.this,
                            "You will not receive alerts now!");
                    CommonMethods.createStringPreference(
                            AnonymousUpdatesActivity.this, "toggle",
                            "toggleAlerts", "alertsOff");
                    stopService(new Intent(AnonymousUpdatesActivity.this,
                            AnonymousLocationService.class));
                    finish();
                } else {

                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    long[] pattern = {500, 500, 1000};

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(AnonymousUpdatesActivity.this)
                                    .setSmallIcon(R.drawable.companion_notification_logo)
                                    .setSound(defaultSoundUri)
                                    .setColor(getResources().getColor(R.color.toolbar)).setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText("We are now tracking your route. We will send you alerts for roadblocks ahead. Have a safe journey.")).setVibrate(pattern)
                                    .setContentTitle("Roadblock Alerts").setAutoCancel(false).setOngoing(true)
                                    .setContentText("We are now tracking your route. We will send you alerts for roadblocks ahead. Have a safe journey.");


                    int mNotificationId = 345;
                    CommonMethods.createStringPreference(AnonymousUpdatesActivity.this, "activeTrack", "notif_id", String.valueOf(mNotificationId));

                    Intent removeIntent = new Intent(AnonymousUpdatesActivity.this, AnonymousUpdatesActivity.class);
                    removeIntent.putExtra("notification_id", mNotificationId);
                    removeIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent removePendingIntent = PendingIntent.getActivity(AnonymousUpdatesActivity.this,
                            0, removeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    mBuilder.setContentIntent(removePendingIntent);

                    // Gets an instance of the NotificationManager service
                    NotificationManager mNotifyMgr =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    // Builds the notification and issues it.
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());

                    CommonMethods.showLongToast(AnonymousUpdatesActivity.this,
                            "You will receive alerts now!");
                    CommonMethods.createStringPreference(
                            AnonymousUpdatesActivity.this, "toggle",
                            "toggleAlerts", "alertsOn");
                    startService(new Intent(AnonymousUpdatesActivity.this,
                            AnonymousLocationService.class));
                    finish();
                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                finish();

            }
        });

    }

}
