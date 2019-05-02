package in.wadersgroup.companion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RoadblockCauseDialogActivity extends AppCompatActivity {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if (sdkVersion >= 21) {

			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(getResources().getColor(R.color.statusbar));
		}

		setContentView(R.layout.activity_roadblock_cause_dialog);

		TextView landslide = (TextView) findViewById(R.id.tvLandslide);
		TextView accident = (TextView) findViewById(R.id.tvAccident);
		TextView traffic = (TextView) findViewById(R.id.tvTraffic);
		TextView construction = (TextView) findViewById(R.id.tvConstruction);
		TextView otherCause = (TextView) findViewById(R.id.tvOther);

		final Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
				"fonts/Arvo-Regular.ttf");

		landslide.setTypeface(arvoRegular);
		accident.setTypeface(arvoRegular);
		traffic.setTypeface(arvoRegular);
		construction.setTypeface(arvoRegular);
		otherCause.setTypeface(arvoRegular);

		LinearLayout llLandslide = (LinearLayout) findViewById(R.id.llLandslide);
		LinearLayout llAccident = (LinearLayout) findViewById(R.id.llAccident);
		LinearLayout llTraffic = (LinearLayout) findViewById(R.id.llTraffic);
		LinearLayout llConstruction = (LinearLayout) findViewById(R.id.llConstruction);
		LinearLayout llOther = (LinearLayout) findViewById(R.id.llOther);

		llLandslide.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent returnIntent = new Intent();
				returnIntent.putExtra("result", "landslide");
				setResult(RESULT_OK, returnIntent);
				finish();

			}
		});

		llAccident.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent returnIntent = new Intent();
				returnIntent.putExtra("result", "accident");
				setResult(RESULT_OK, returnIntent);
				finish();

			}
		});

		llTraffic.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent returnIntent = new Intent();
				returnIntent.putExtra("result", "traffic");
				setResult(RESULT_OK, returnIntent);
				finish();

			}
		});

		llConstruction.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent returnIntent = new Intent();
				returnIntent.putExtra("result", "construction");
				setResult(RESULT_OK, returnIntent);
				finish();

			}
		});

		llOther.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent returnIntent = new Intent();
				returnIntent.putExtra("result", "other");
				setResult(RESULT_OK, returnIntent);
				finish();

			}
		});

	}

}
