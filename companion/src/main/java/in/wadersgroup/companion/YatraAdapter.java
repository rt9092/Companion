package in.wadersgroup.companion;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by romil_wadersgroup on 13/6/16.
 */
public class YatraAdapter extends BaseAdapter {

    private Context context;
    List<HashMap<String, String>> notificationArray = new ArrayList<>();

    public YatraAdapter(Context context, List<HashMap<String, String>> touristArray) {
        this.context = context;
        this.notificationArray = touristArray;
    }

    @Override
    public int getCount() {
        return notificationArray.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;

        if (convertView == null) {
            gridView = inflater.inflate(R.layout.yatra_notifications_item, null);
        } else {
            gridView = convertView;
        }
        TextView textView = (TextView) gridView
                .findViewById(R.id.tvNotificationText);
        textView.setText(notificationArray.get(position).get("notifMessage"));
        TextView textView1 = (TextView) gridView
                .findViewById(R.id.tvNotificationDate);
        textView1.setText("Date: " + notificationArray.get(position).get("notifDate"));
        TextView textView2 = (TextView) gridView
                .findViewById(R.id.tvNotificationTime);
        textView2.setText("Time: " + notificationArray.get(position).get("notifTime"));
        final Typeface arvoRegular = Typeface.createFromAsset(context.getAssets(),
                "fonts/Arvo-Regular.ttf");
        textView.setTypeface(arvoRegular);
        textView1.setTypeface(arvoRegular);
        textView2.setTypeface(arvoRegular);


        return gridView;
    }

}
