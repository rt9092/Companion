package in.wadersgroup.companion;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by romil_wadersgroup on 24/2/16.
 */
public class CardAdapter extends BaseAdapter {

    private Context context;
    List<HashMap<String, String>> touristArray = new ArrayList<>();

    public CardAdapter(Context context, List<HashMap<String, String>> touristArray) {
        this.context = context;
        this.touristArray = touristArray;
    }

    @Override
    public int getCount() {
        return touristArray.size();
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
            gridView = inflater.inflate(R.layout.grid_card, null);
        } else {
            gridView = convertView;
        }
        TextView textView = (TextView) gridView
                .findViewById(R.id.tvTouristPlaceName);
        textView.setText(touristArray.get(position).get("name"));
        final Typeface arvoRegular = Typeface.createFromAsset(context.getAssets(),
                "fonts/Arvo-Regular.ttf");
        textView.setTypeface(arvoRegular);
        ImageView imageView = (ImageView) gridView
                .findViewById(R.id.ivTouristPlaceImage);

        Picasso.with(context)
                .load(touristArray.get(position).get("image_uri"))
                .fit().centerCrop()
                .placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)
                .into(imageView);


        return gridView;
    }
}
