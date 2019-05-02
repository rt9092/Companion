package in.wadersgroup.companion;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

/**
 * Created by romil_wadersgroup on 5/2/16.
 */
public class FilterDialogActivity extends AppCompatActivity {

    String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_dialog);

        final CheckBox tourism = (CheckBox) findViewById(R.id.cbTourism);
        final CheckBox events = (CheckBox) findViewById(R.id.cbEvents);
        final CheckBox amenities = (CheckBox) findViewById(R.id.cbAmenities);

        Typeface arvoRegular = Typeface.createFromAsset(getAssets(),
                "fonts/Arvo-Regular.ttf");

        tourism.setTypeface(arvoRegular);
        events.setTypeface(arvoRegular);
        amenities.setTypeface(arvoRegular);

        Button filterOk = (Button) findViewById(R.id.bFilterOk);
        filterOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tourism.isChecked()) {
                    result = result + "tourist_places,";
                }
                if (events.isChecked()) {
                    result = result + "events,";
                }
                if (amenities.isChecked()) {
                    result = result + "amenities,";
                }
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", result);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });


    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.cbTourism:
                if (checked)
                    CommonMethods.showShortToast(FilterDialogActivity.this, "Roadblocks");
                else
                    break;
            case R.id.cbEvents:
                if (checked)
                    CommonMethods.showShortToast(FilterDialogActivity.this, "Events");
                else
                    break;
            case R.id.cbAmenities:
                if (checked)
                    CommonMethods.showShortToast(FilterDialogActivity.this, "Amenities");
                else
                    break;


        }
    }

}
