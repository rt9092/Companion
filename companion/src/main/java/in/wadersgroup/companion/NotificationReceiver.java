
package in.wadersgroup.companion;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by romil_wadersgroup on 11/2/16.
 */
public class NotificationReceiver extends BroadcastReceiver {

    Context activityContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        activityContext = context;
        new PeriodicNotificationFetch().execute();

    }

    class PeriodicNotificationFetch extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            String res = "";
            try {
                String email = URLEncoder.encode(CommonMethods
                        .readStringPreference(activityContext,
                                "userDetails", "email"), "utf-8");

                String secret_key = URLEncoder.encode(CommonMethods
                        .readStringPreference(activityContext,
                                "secret", "secretKey"), "utf-8");

                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                // Put URL Below
                HttpGet httpPost = new HttpGet(Constants.USER_BLOCKS_URL
                        + email + "&secret_key=" + secret_key);

                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                res = EntityUtils.toString(response.getEntity());

                System.out.println("Notification: " + res);

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }

            return res;
        }

        ArrayList<String> blockIdArray = new ArrayList<>();
        ArrayList<String> causeArray = new ArrayList<>();
        ArrayList<String> locationArray = new ArrayList<>();

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);

            try {
                JSONObject jObj = new JSONObject(aVoid.trim());

                String successMsg = jObj.getString("success");

                if (successMsg.contentEquals("yes")) {

                    JSONArray response = jObj.getJSONArray("response");
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject tempObj = response.getJSONObject(i);
                        String block_id = tempObj.getString("block_id");
                        String location_id = tempObj.getString("location");
                        String cause_id = tempObj.getString("cause");

                        blockIdArray.add(i, block_id);
                        causeArray.add(i, cause_id);
                        locationArray.add(i, location_id);

                    }
                    for (int i = 0; i < blockIdArray.size(); i++) {

                        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        long[] pattern = {500, 500, 1000};

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(activityContext)
                                        .setSmallIcon(R.drawable.companion_notification_logo)
                                        .setSound(defaultSoundUri)
                                        .setColor(activityContext.getResources().getColor(R.color.toolbar)).setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText("You just reported a roadblock. Click to know the details.")).setVibrate(pattern)
                                        .setContentTitle("Remove Roadblock").setAutoCancel(false).setOngoing(true)
                                        .setContentText("You just reported a roadblock. Click to know the details.");


                        AtomicInteger c = new AtomicInteger(0);
                        int mNotificationId = c.incrementAndGet();

                        Intent removeIntent = new Intent(activityContext, RoadblockRemovalActivity.class);
                        removeIntent.putExtra("block_id", blockIdArray.get(i));
                        removeIntent.putExtra("notification_id", mNotificationId);
                        removeIntent.putExtra("location", locationArray.get(i));

                        removeIntent.putExtra("cause", causeArray.get(i).trim());
                        removeIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        PendingIntent removePendingIntent = PendingIntent.getActivity(activityContext,
                                0, removeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        mBuilder.setContentIntent(removePendingIntent);


                        // Gets an instance of the NotificationManager service
                        NotificationManager mNotifyMgr =
                                (NotificationManager) activityContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        // Builds the notification and issues it.
                        mNotifyMgr.notify(mNotificationId, mBuilder.build());
                        Vibrator v = (Vibrator) activityContext.getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(1000);
                    }
                }
            } catch (JSONException e) {
                AnalyticsApplication.getInstance().trackException(e);
                e.printStackTrace();
            }

        }
    }
}
