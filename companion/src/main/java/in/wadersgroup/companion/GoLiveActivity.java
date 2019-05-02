package in.wadersgroup.companion;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * Created by romil_wadersgroup on 8/2/16.
 */
public class GoLiveActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    Animation shrinkButton;
    Animation left2right;
    Animation right2left;
    Animation fadeOut, mapSlide, mapSlideRight;
    CoordinatorLayout liveMap;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        setContentView(R.layout.activity_go_live);

        liveMap = (CoordinatorLayout) findViewById(R.id.llDashboardMap);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fabCancelLiveMap = (FloatingActionButton) findViewById(R.id.fabCancel);
        FloatingActionButton fabLiveMapFilters = (FloatingActionButton) findViewById(R.id.fabFilters);


        mapSlideRight = AnimationUtils.loadAnimation(GoLiveActivity.this,
                R.anim.map_slide_right);
        right2left = AnimationUtils.loadAnimation(GoLiveActivity.this,
                R.anim.right_to_left);
        left2right = AnimationUtils.loadAnimation(GoLiveActivity.this,
                R.anim.left_to_right);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
