package adapter;

import in.wadersgroup.companion.R;
import java.util.Locale;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Romil
 *
 */
public class CountriesAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;

	public CountriesAdapter(Context context, String[] values) {
		super(context, R.layout.country_list_item, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.country_list_item, parent,
				false);
		TextView textView = (TextView) rowView.findViewById(R.id.tvCountryName);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.ivFlag);

		String[] g = values[position].split(",");
		textView.setText(GetCountryZipCode(g[1]).trim());

		String pngName = g[1].trim().toLowerCase();
		imageView.setImageResource(context.getResources().getIdentifier(
				"drawable/" + pngName, null, context.getPackageName()));
		return rowView;
	}

	private String GetCountryZipCode(String ssid) {
		Locale loc = new Locale("", ssid);

		return loc.getDisplayCountry().trim();
	}
}
