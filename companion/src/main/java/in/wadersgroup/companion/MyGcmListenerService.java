package in.wadersgroup.companion;

/**
 * Created by romil_wadersgroup on 4/3/16.
 */


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    String header;

    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {

        header = data.getString("header");

        if (header.trim().contentEquals("active_tracking")) {

            String message = data.getString("message");
            String address = data.getString("address");
            String cause = data.getString("cause");
            System.out.println("Roadblock Cause: " + cause);
            String location = data.getString("location");
            String blockID = data.getString("block_id");
            String confidence = data.getString("active");

            sendNotificationForActiveTrack(blockID, message, cause, location, address, confidence);

        } else if (header.trim().contentEquals("roadblock_removed")) {

            String message = data.getString("message");
            String address = data.getString("address");
            String cause = data.getString("cause");
            String location = data.getString("location");
            String blockID = data.getString("block_id");

            sendNotificationForRoadblockRemoved(blockID, message, cause, location, address);

        } else if (header.trim().contentEquals("roadblock_reported")) {

            String message = data.getString("message");
            String confidence = data.getString("active");
            String address = data.getString("address");
            String cause = data.getString("cause");
            System.out.println("Roadblock Cause: " + cause);
            String location = data.getString("location");
            String blockID = data.getString("block_id");

            sendNotificationForRoadblockReported(blockID, message, cause, location, address, confidence);

        } else if (header.trim().contentEquals("incentive_received")) {

            String message = data.getString("message");
            String points = data.getString("points");

            sendNotificationForIncentive(message, points);

        } else if (header.trim().contentEquals("recharge_status")) {

            String message = data.getString("message");

            sendNotificationForRechargeStatus(message);

        } else if (header.trim().contentEquals("general_notification")) {

            String message = data.getString("message");
            String title = data.getString("title");

            sendNotification(message, title);

        }


        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */

    private void sendNotification(String message, String title) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("header", header);
        intent.putExtra("message", message);
        intent.putExtra("title", title);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.companion_notification_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.toolbar)).setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message)).setVibrate(new long[]{500, 500, 1000});

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendNotificationForIncentive(String message, String points) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("message", message);
        intent.putExtra("points", points);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.companion_notification_logo)
                .setContentTitle(points.trim() + " Points Received.")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.toolbar)).setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message)).setVibrate(new long[]{500, 500, 1000});

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendNotificationForActiveTrack(String blockId, String message, String cause, String location, String address, String confidence) {
        Intent intent = new Intent(this, MapNotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("header", header);
        intent.putExtra("active", confidence);
        intent.putExtra("cause", cause);
        intent.putExtra("location", location);
        intent.putExtra("address", address);
        intent.putExtra("block_id", blockId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.roadblock_notification_sound);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.companion_notification_logo)
                .setContentTitle(cause.substring(0, 1).toUpperCase() + cause.substring(1).toLowerCase() + " Ahead.")
                .setContentText(message)
                .setAutoCancel(true).setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.toolbar)).setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message)).setVibrate(new long[]{500, 500, 1000});

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendNotificationForRoadblockRemoved(String blockId, String message, String cause, String location, String address) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("header", header);
        intent.putExtra("cause", cause);
        intent.putExtra("location", location);
        intent.putExtra("address", address);
        intent.putExtra("block_id", blockId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.roadblock_notification_sound);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.companion_notification_logo)
                .setContentTitle(cause.substring(0, 1).toUpperCase() + cause.substring(1).toLowerCase() + " removed.")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.toolbar)).setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message)).setVibrate(new long[]{500, 500, 1000});

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendNotificationForRoadblockReported(String blockId, String message, String cause, String location, String address, String confidence) {
        Intent intent = new Intent(this, MapNotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("header", header);
        intent.putExtra("active", confidence);
        intent.putExtra("cause", cause);
        intent.putExtra("location", location);
        intent.putExtra("address", address);
        intent.putExtra("block_id", blockId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.roadblock_notification_sound);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.companion_notification_logo)
                .setContentTitle(cause.substring(0, 1).toUpperCase() + cause.substring(1).toLowerCase() + " reported. Please Confirm.")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.toolbar)).setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message)).setVibrate(new long[]{500, 500, 1000});

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendNotificationForRechargeStatus(String message) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("header", header);
        intent.putExtra("message", message);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.companion_notification_logo)
                .setContentTitle("Recharge Received")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.toolbar)).setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message)).setVibrate(new long[]{500, 500, 1000});

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
