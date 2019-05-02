package in.wadersgroup.companion;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PlacesAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;

	// Test Data

	String[] placeNameArray = { "Ram Niwas Ki Dukan", "Ravangla Star",
			"Queens Hill", "Thamzi Restaurant", "Yalamber Resto Bar",
			"Restaurant 1", "Restaurant 1", "Restaurant 1", "Restaurant 1",
			"Restaurant 1" };

	String[] placeDistanceArray = { "1 Kms", "2 Kms", "4 Kms", "6.28 Kms",
			"0.954 Kms", "5 Kms", "5 Kms", "5 Kms", "5 Kms", "5 Kms" };

	String[] placeDurationArray = { "1 Mins", "2 Mins", "4 Mins", "6.28 Mins",
			"0.954 Mins", "5 Mins", "5 Mins", "5 Mins", "5 Mins", "5 Mins" };

	public PlacesAdapter(Activity a) {
		activity = a;

		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return placeNameArray.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.places_list_item, null);

		TextView placeName = (TextView) vi.findViewById(R.id.tvPlaceName);
		TextView placeDistance = (TextView) vi
				.findViewById(R.id.tvPlaceDistance);
		TextView placeDuration = (TextView) vi
				.findViewById(R.id.tvPlaceDuration);

		Typeface arvoBold = Typeface.createFromAsset(vi.getContext()
				.getAssets(), "fonts/Arvo-Bold.ttf");
		Typeface arvoRegular = Typeface.createFromAsset(vi.getContext()
				.getAssets(), "fonts/Arvo-Regular.ttf");

		placeName.setTypeface(arvoBold);
		placeDistance.setTypeface(arvoRegular);
		placeDuration.setTypeface(arvoRegular);

		placeName.setText(placeNameArray[position]);
		placeDistance.setText(placeDistanceArray[position]);
		placeDuration.setText(placeDurationArray[position]);

		return vi;
	}

}
