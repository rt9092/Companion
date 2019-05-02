package in.wadersgroup.companion;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by romil_wadersgroup on 22/2/16.
 */
public class PlaceDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_place_details);

        TextView name = (TextView) findViewById(R.id.tvName);
        TextView address = (TextView) findViewById(R.id.tvAddress);
        TextView contactPerson = (TextView) findViewById(R.id.tvContactPerson);
        TextView contactNumber = (TextView) findViewById(R.id.tvContactNumber);
        TextView about = (TextView) findViewById(R.id.tvAbout);

        LinearLayout llName = (LinearLayout) findViewById(R.id.llName);
        LinearLayout llAddress = (LinearLayout) findViewById(R.id.llAddress);
        LinearLayout llContactPerson = (LinearLayout) findViewById(R.id.llContactPerson);
        LinearLayout llContactNumber = (LinearLayout) findViewById(R.id.llContactNumber);
        LinearLayout llAbout = (LinearLayout) findViewById(R.id.llAbout);

        String intentName = getIntent().getStringExtra("name");
        String intentAddress = getIntent().getStringExtra("address");
        String intentContactPerson = getIntent().getStringExtra("contactPerson");
        String intentContactNumber = getIntent().getStringExtra("contactNumber");
        String intentAbout = getIntent().getStringExtra("about");

        Typeface arvoBold = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        name.setTypeface(arvoBold);
        address.setTypeface(arvoBold);
        contactPerson.setTypeface(arvoBold);
        contactNumber.setTypeface(arvoBold);
        about.setTypeface(arvoBold);

        name.setText(intentName);
        address.setText(intentAddress);

        if (!intentContactPerson.contentEquals("")) {
            contactPerson.setText(intentContactPerson);
        } else {
            llContactPerson.setVisibility(View.GONE);
        }
        if (!intentContactNumber.contentEquals("")) {
            contactNumber.setText(intentContactNumber);
        } else {
            llContactNumber.setVisibility(View.GONE);
        }
        if (!intentAbout.contentEquals("")) {
            about.setText(intentAbout);
        } else {
            llAbout.setVisibility(View.GONE);
        }


    }
}
