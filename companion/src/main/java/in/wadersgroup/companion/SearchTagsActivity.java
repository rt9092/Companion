package in.wadersgroup.companion;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by romil_wadersgroup on 13/2/16.
 */
public class SearchTagsActivity extends AppCompatActivity implements View.OnClickListener {

    Button hotel, restaurant, atm, petrol, bank, toilet,
            transportation, autoRepair, pharmacy, hospital, police, finish;

    boolean hotelBool, restBool, atmBool, petrolBool, bankBool, toiletBool, transBool, autoBool, pharmBool, hospBool, policeBool;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_tags);

        hotel = (Button) findViewById(R.id.bHotels);
        restaurant = (Button) findViewById(R.id.bRestaurants);
        atm = (Button) findViewById(R.id.bATM);
        petrol = (Button) findViewById(R.id.bPetrol);
        bank = (Button) findViewById(R.id.bBanks);
        toilet = (Button) findViewById(R.id.bRestrooms);
        transportation = (Button) findViewById(R.id.bTransport);
        autoRepair = (Button) findViewById(R.id.bAutoRepair);
        pharmacy = (Button) findViewById(R.id.bPharmacy);
        hospital = (Button) findViewById(R.id.bHospital);
        police = (Button) findViewById(R.id.bPolice);
        finish = (Button) findViewById(R.id.bFinishDialogActivity);

        hotel.setOnClickListener(this);
        restaurant.setOnClickListener(this);
        atm.setOnClickListener(this);
        petrol.setOnClickListener(this);
        bank.setOnClickListener(this);
        toilet.setOnClickListener(this);
        transportation.setOnClickListener(this);
        autoRepair.setOnClickListener(this);
        pharmacy.setOnClickListener(this);
        hospital.setOnClickListener(this);
        police.setOnClickListener(this);
        finish.setOnClickListener(this);

    }

    Intent returnIntent = new Intent();
    String result = "";


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bHotels:

                if (hotelBool) {
                    hotel.setBackgroundResource(R.drawable.next_rounded);
                    hotelBool = false;
                } else {
                    hotel.setBackgroundResource(R.drawable.next_rounded_pressed);
                    hotelBool = true;
                }

                /*if (hotel.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.next_rounded).getConstantState())) {
                    hotel.setBackgroundResource(R.drawable.next_rounded_pressed);
                } else {
                    hotel.setBackgroundResource(R.drawable.next_rounded);
                }*/

                break;
            case R.id.bRestaurants:

                if (restBool) {
                    restaurant.setBackgroundResource(R.drawable.next_rounded);
                    restBool = false;
                } else {
                    restaurant.setBackgroundResource(R.drawable.next_rounded_pressed);
                    restBool = true;
                }

                /*if (restaurant.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.next_rounded).getConstantState())) {
                    restaurant.setBackgroundResource(R.drawable.next_rounded_pressed);
                } else {
                    restaurant.setBackgroundResource(R.drawable.next_rounded);
                }
*/
                break;
            case R.id.bATM:

                if (atmBool) {
                    atm.setBackgroundResource(R.drawable.next_rounded);
                    atmBool = false;
                } else {
                    atm.setBackgroundResource(R.drawable.next_rounded_pressed);
                    atmBool = true;
                }

                /*if (atm.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.next_rounded).getConstantState())) {
                    atm.setBackgroundResource(R.drawable.next_rounded_pressed);
                } else {
                    atm.setBackgroundResource(R.drawable.next_rounded);
                }*/

                break;
            case R.id.bPetrol:

                if (petrolBool) {
                    petrol.setBackgroundResource(R.drawable.next_rounded);
                    petrolBool = false;
                } else {
                    petrol.setBackgroundResource(R.drawable.next_rounded_pressed);
                    petrolBool = true;
                }

                /*if (petrol.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.next_rounded).getConstantState())) {
                    petrol.setBackgroundResource(R.drawable.next_rounded_pressed);
                } else {
                    petrol.setBackgroundResource(R.drawable.next_rounded);
                }*/

                break;
            case R.id.bBanks:

                if (bankBool) {
                    bank.setBackgroundResource(R.drawable.next_rounded);
                    bankBool = false;
                } else {
                    bank.setBackgroundResource(R.drawable.next_rounded_pressed);
                    bankBool = true;
                }

                /*if (bank.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.next_rounded).getConstantState())) {
                    bank.setBackgroundResource(R.drawable.next_rounded_pressed);
                } else {
                    bank.setBackgroundResource(R.drawable.next_rounded);
                }*/

                break;
            case R.id.bRestrooms:

                if (toiletBool) {
                    toilet.setBackgroundResource(R.drawable.next_rounded);
                    toiletBool = false;
                } else {
                    toilet.setBackgroundResource(R.drawable.next_rounded_pressed);
                    toiletBool = true;
                }

                /*if (toilet.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.next_rounded).getConstantState())) {
                    toilet.setBackgroundResource(R.drawable.next_rounded_pressed);
                } else {
                    toilet.setBackgroundResource(R.drawable.next_rounded);
                }*/

                break;
            case R.id.bTransport:

                if (transBool) {
                    transportation.setBackgroundResource(R.drawable.next_rounded);
                    transBool = false;
                } else {
                    transportation.setBackgroundResource(R.drawable.next_rounded_pressed);
                    transBool = true;
                }

                /*if (transportation.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.next_rounded).getConstantState())) {
                    transportation.setBackgroundResource(R.drawable.next_rounded_pressed);
                } else {
                    transportation.setBackgroundResource(R.drawable.next_rounded);
                }*/

                break;
            case R.id.bAutoRepair:

                if (autoBool) {
                    autoRepair.setBackgroundResource(R.drawable.next_rounded);
                    autoBool = false;
                } else {
                    autoRepair.setBackgroundResource(R.drawable.next_rounded_pressed);
                    autoBool = true;
                }

                /*if (autoRepair.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.next_rounded).getConstantState())) {
                    autoRepair.setBackgroundResource(R.drawable.next_rounded_pressed);
                } else {
                    autoRepair.setBackgroundResource(R.drawable.next_rounded);
                }*/

                break;
            case R.id.bPharmacy:

                if (pharmBool) {
                    pharmacy.setBackgroundResource(R.drawable.next_rounded);
                    pharmBool = false;
                } else {
                    pharmacy.setBackgroundResource(R.drawable.next_rounded_pressed);
                    pharmBool = true;
                }

                /*if (pharmacy.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.next_rounded).getConstantState())) {
                    pharmacy.setBackgroundResource(R.drawable.next_rounded_pressed);
                } else {
                    pharmacy.setBackgroundResource(R.drawable.next_rounded);
                }*/

                break;
            case R.id.bHospital:

                if (hospBool) {
                    hospital.setBackgroundResource(R.drawable.next_rounded);
                    hospBool = false;
                } else {
                    hospital.setBackgroundResource(R.drawable.next_rounded_pressed);
                    hospBool = true;
                }

                /*if (hospital.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.next_rounded).getConstantState())) {
                    hospital.setBackgroundResource(R.drawable.next_rounded_pressed);
                } else {
                    hospital.setBackgroundResource(R.drawable.next_rounded);
                }*/

                break;
            case R.id.bPolice:

                if (policeBool) {
                    police.setBackgroundResource(R.drawable.next_rounded);
                    policeBool = false;
                } else {
                    police.setBackgroundResource(R.drawable.next_rounded_pressed);
                    policeBool = true;
                }

                /*if (police.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.next_rounded).getConstantState())) {
                    police.setBackgroundResource(R.drawable.next_rounded_pressed);
                } else {
                    police.setBackgroundResource(R.drawable.next_rounded);
                }*/

                break;
            case R.id.bFinishDialogActivity:

                if (hotelBool) {
                    result = result + "hotel,";
                }
                if (restBool) {
                    result = result + "restaurant,";
                }
                if (atmBool) {
                    result = result + "atm,";
                }
                if (petrolBool) {
                    result = result + "gas_station,";
                }
                if (bankBool) {
                    result = result + "bank,";
                }
                if (toiletBool) {
                    result = result + "toilet,";
                }
                if (transBool) {
                    result = result + "taxi_stand,bus_station,train_station,airport,";
                }
                if (autoBool) {
                    result = result + "auto_repair,";
                }
                if (pharmBool) {
                    result = result + "pharmacy,";
                }
                if (hospBool) {
                    result = result + "hospital,";
                }
                if (policeBool) {
                    result = result + "police,";
                }

                returnIntent.putExtra("result", result);
                setResult(RESULT_OK, returnIntent);
                finish();

                break;
        }

    }
}
