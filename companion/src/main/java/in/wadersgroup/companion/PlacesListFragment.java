package in.wadersgroup.companion;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class PlacesListFragment extends Fragment {

	View rootView;

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		rootView = inflater.inflate(R.layout.fragment_places_list, container,
				false);
		Typeface arvoBold = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/Arvo-Bold.ttf");

		ListView list = (ListView) rootView.findViewById(R.id.list);
		PlacesAdapter adapter = new PlacesAdapter(getActivity());
		list.setAdapter(adapter);

		return rootView;
	}

}
