package in.wadersgroup.companion;

//(Service) Sends Location Data anonymously to the Companion Server

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * @author Romil
 * 
 */
public class AnonymousLocationService extends Service implements
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	protected static final String TAG = "location-updates-sample";

	/**
	 * The desired interval for location updates. Inexact. Updates may be more or less frequent.
	 */
	public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

	/**
	 * The fastest rate for active location updates. Exact. Updates will never be more frequent
	 * than this value.
	 */
	public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
			UPDATE_INTERVAL_IN_MILLISECONDS / 2;

	// Keys for storing activity state in the Bundle.
	protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
	protected final static String LOCATION_KEY = "location-key";
	protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

	/**
	 * Provides the entry point to Google Play services.
	 */
	protected GoogleApiClient mGoogleApiClient;

	/**
	 * Stores parameters for requests to the FusedLocationProviderApi.
	 */
	protected LocationRequest mLocationRequest;

	/**
	 * Represents a geographical location.
	 */
	protected Location mCurrentLocation;

	/**
	 * Tracks the status of the location updates request. Value changes when the user presses the
	 * Start Updates and Stop Updates buttons.
	 */
	protected Boolean mRequestingLocationUpdates;

	/**
	 * Time when the location was updated represented as a String.
	 */
	protected String mLastUpdateTime;



	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		mRequestingLocationUpdates = true;
		mLastUpdateTime = "";
		buildGoogleApiClient();
		mGoogleApiClient.connect();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

		mCurrentLocation = location;
		String latitude = String.valueOf(location.getLatitude());
		String longitude = String.valueOf(location.getLongitude());
		String locSend = latitude + ", " + longitude;
		new NetworkAsync().execute(locSend);

		System.out.println("location updating");

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub

		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
				+ result.getErrorCode());

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub

		if (mRequestingLocationUpdates) {
			startLocationUpdates();
		}

	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub

		Log.i(TAG, "Connection suspended");
		mGoogleApiClient.connect();

	}

	protected void createLocationRequest() {
		LocationRequest mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	/**
	 * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
	 * LocationServices API.
	 */
	protected synchronized void buildGoogleApiClient() {
		Log.i(TAG, "Building GoogleApiClient");
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
		createLocationRequest();
	}

	protected void startLocationUpdates() {
		// The final argument to {@code requestLocationUpdates()} is a
		// LocationListener
		// (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
		LocationRequest mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);
	}

	// Method to send location updates to he Server

	public void sendLocationUpdates(String location) {

		String email;
		try {
			email = URLEncoder.encode(CommonMethods.readStringPreference(
					AnonymousLocationService.this, "userDetails", "email"),
					"utf-8");

			String locationSend = URLEncoder.encode(location, "utf-8");
			String secret_key = URLEncoder.encode(CommonMethods
					.readStringPreference(AnonymousLocationService.this,
							"secret", "secretKey"), "utf-8");

			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			// Put URL Below
			HttpGet httpPost = new HttpGet(
					Constants.ANONYMOUS_LOCATION_UPDATE_URL + email
							+ "&location=" + locationSend + "&secret_key="
							+ secret_key);

			HttpResponse response = httpClient.execute(httpPost, localContext);
			String res = EntityUtils.toString(response.getEntity());
		} catch (UnsupportedEncodingException e) {
			AnalyticsApplication.getInstance().trackException(e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			AnalyticsApplication.getInstance().trackException(e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			AnalyticsApplication.getInstance().trackException(e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// To send the Location Updates using a background thread

	class NetworkAsync extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			sendLocationUpdates(params[0]);
			return null;
		}

	}

}
