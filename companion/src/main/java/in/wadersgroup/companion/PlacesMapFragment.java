package in.wadersgroup.companion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class PlacesMapFragment extends Fragment implements ConnectionCallbacks,
		OnConnectionFailedListener {

	GoogleMap map;
	GoogleApiClient mGoogleApiClient;
	Location mLastLocation;
	RelativeLayout llOperationFailed;
	ImageButton ibOperationFailed;
	View rootView;
	private FragmentActivity myContext;

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		rootView = inflater.inflate(R.layout.fragment_places_map, container,
				false);

		// Getting reference to SupportMapFragment of the activity_main

		SupportMapFragment fm = (SupportMapFragment) getChildFragmentManager()
				.findFragmentById(R.id.map);

		Typeface arvoBold = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/Arvo-Bold.ttf");

		llOperationFailed = (RelativeLayout) rootView
				.findViewById(R.id.llOperationFailed);
		TextView tvOperationFailed = (TextView) rootView
				.findViewById(R.id.tvOperationFailed);
		ibOperationFailed = (ImageButton) rootView
				.findViewById(R.id.ibOperationFailed);

		llOperationFailed.setVisibility(View.GONE);

		tvOperationFailed.setTypeface(arvoBold);

		// Getting Map for the SupportMapFragment
		map = fm.getMap();

		buildGoogleApiClient();

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		myContext = (FragmentActivity) activity;
		super.onAttach(activity);
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		mGoogleApiClient.connect();

	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}

	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub

		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);

		// Run the background task for fetching Places with the current Location

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

		mGoogleApiClient.connect();

	}

	class FetchPlaces extends AsyncTask<String, Void, Void> {

		ProgressDialog pDialog;
		boolean tryCatchFlag = false;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Please wait...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub

			try {

				String email = URLEncoder.encode(CommonMethods
						.readStringPreference(getActivity(), "userDetails",
								"email"), "utf-8");

				String secret = URLEncoder.encode(CommonMethods
						.readStringPreference(getActivity(), "secret",
								"secretKey"), "utf-8");

				String location = URLEncoder.encode(params[0], "utf-8");
				String tag = URLEncoder.encode(params[1], "utf-8");

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}

	}

}
