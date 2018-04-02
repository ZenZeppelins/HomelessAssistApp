package zepplins.zen.homelessassist.controllers;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import zepplins.zen.homelessassist.R;
import zepplins.zen.homelessassist.model.Model;
import zepplins.zen.homelessassist.model.Shelter;

/**
 * Activity when user is looking at Map view
 */
public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab = findViewById(R.id.searchMapFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SearchActivity.class);
                i.putExtra("source", "map");
                startActivity(i);
            }
        });

        fab = findViewById(R.id.backMapFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SheltersActivity.class);
                startActivity(i);
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * Load all the pins that are active
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        List<Shelter> active = Model.getInstance().getActiveShelters();
        double latTotal = 0;
        double longTotal = 0;
        int count = 0;
        //For each shelter, load a marker at the location with the corresponding info
        for (Shelter s : active) {
            LatLng loc = new LatLng(s.getLatitude(), s.getLongitude());
            String snippet = "Phone: " + s.getPhoneNumber();
            googleMap.addMarker(new MarkerOptions().position(loc).
                    title(s.getShelterName()).snippet(snippet));
            latTotal += s.getLatitude();
            longTotal += s.getLongitude();
            count++;
        }
        //Calculate the average latitute and longitude
        double latAvg = latTotal / count;
        double longAvg = longTotal / count;
        //If no shelters are in active, set camera to Georgia Tech
        if (active.isEmpty()) {
            latAvg = 33.7756;
            longAvg = -84.3963;
        }
        //Move camera to average lat/long of shelters
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latAvg, longAvg)));
        //11 found through experimentation
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(11));
    }
}
