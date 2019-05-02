package in.wadersgroup.companion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Calendar;

/**
 * Created by romil_wadersgroup on 11/2/16.
 */
public class NotificationService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent myIntent = new Intent(NotificationService.this, NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(NotificationService.this, 0, myIntent, 0);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 15 * 60); // first time
        long frequency = 15 * 60 * 1000; // in ms
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency, pendingIntent);
        //stopSelf();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);

    }
}
