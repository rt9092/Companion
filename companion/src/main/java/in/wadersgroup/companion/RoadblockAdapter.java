package in.wadersgroup.companion;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by romil_wadersgroup on 2/3/16.
 */
public class RoadblockAdapter extends BaseAdapter {
    private Context context;
    List<HashMap<String, String>> roadblockArray = new ArrayList<>();

    public RoadblockAdapter(Context context, List<HashMap<String, String>> roadblockArray) {
        this.context = context;
        this.roadblockArray = roadblockArray;
    }

    @Override
    public int getCount() {
        return roadblockArray.size();
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
            gridView = inflater.inflate(R.layout.list_item_user_block, null);
        } else {
            gridView = convertView;
        }
        TextView tvType = (TextView) gridView
                .findViewById(R.id.tvRoadblockType);
        tvType.setText(roadblockArray.get(position).get("cause").toUpperCase());
        Typeface arvoRegular = Typeface.createFromAsset(context.getAssets(),
                "fonts/Arvo-Regular.ttf");
        tvType.setTypeface(arvoRegular);

        ImageView ivCause = (ImageView) gridView.findViewById(R.id.ivRoadblockCause);
        if (roadblockArray.get(position).get("cause").trim().contentEquals("landslide")) {
            ivCause.setImageResource(R.drawable.ic_pointer_landslide);
        } else if (roadblockArray.get(position).get("cause").trim().contentEquals("traffic")) {
            ivCause.setImageResource(R.drawable.ic_pointer_traffic);
        } else if (roadblockArray.get(position).get("cause").trim().contentEquals("accident")) {
            ivCause.setImageResource(R.drawable.ic_pointer_accident);
        } else if (roadblockArray.get(position).get("cause").trim().contentEquals("construction")) {
            ivCause.setImageResource(R.drawable.ic_pointer_construction);
        } else if (roadblockArray.get(position).get("cause").trim().contentEquals("other")) {
            ivCause.setImageResource(R.drawable.ic_pointer_options);
        }

        TextView tvAddress = (TextView) gridView
                .findViewById(R.id.tvRoadblockAddress);
        TextView tvConfidence = (TextView) gridView
                .findViewById(R.id.tvRoadblockConfidence);
        tvAddress.setTypeface(arvoRegular);
        tvConfidence.setTypeface(arvoRegular);
        tvAddress.setText(roadblockArray.get(position).get("address"));
        tvConfidence.setText(roadblockArray.get(position).get("active"));

        return gridView;
    }
}
