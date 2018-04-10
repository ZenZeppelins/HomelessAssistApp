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
    private final int MAP_ZOOM = 11;

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
        Model m = Model.getInstance();
        List<Shelter> active = m.getActiveShelters();
        //For each shelter, load a marker at the location with the corresponding info
        for (Shelter s : active) {
            LatLng loc = new LatLng(s.getLatitude(), s.getLongitude());
            String snippet = "Phone: " + s.getPhoneNumber();
            googleMap.addMarker(new MarkerOptions().position(loc).
                    title(s.getShelterName()).snippet(snippet));
        }
        //Calculate the average latitude and longitude
        double latAvg = m.averageLatitude();
        double longAvg = m.averageLongitude();
        //Move camera to average lat/long of shelters
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latAvg, longAvg)));
        //11 found through experimentation
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM));
    }
}
