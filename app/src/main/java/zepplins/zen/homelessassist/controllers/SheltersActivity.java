package zepplins.zen.homelessassist.controllers;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import zepplins.zen.homelessassist.R;
import zepplins.zen.homelessassist.model.Model;
import zepplins.zen.homelessassist.model.Shelter;

public class SheltersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen();
    }

    //This creates log out button and loads shelter list
    private void createScreen() {
        setContentView(R.layout.activity_shelters);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Model.getInstance().getAuthenticator().signOut();
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });

        createShelterList();
    }

    //Iterate through shelter list. For each shelter, create a TableRow and a TextView
    private void createShelterList() {
        TableLayout shelterListContainer = (TableLayout) findViewById(R.id.shelterListContainer);
        Model m = Model.getInstance();
        List<Shelter> shelterList = m.getShelters();
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(50, 20, 0, 0);
        for (int i = 0; i < shelterList.size(); i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(params);
            TextView textView = new TextView(this);
            textView.setTextColor(Color.parseColor("#4385ef"));
            textView.setTextSize(24);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setText(shelterList.get(i).getShelterName());
            //The tag of the TextView is the index of the shelter in shelterList
            textView.setTag(i);
            //When the shelter name is clicked, the shelter info screen is loaded
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shelterClicked(v);
                }
            });
            tableRow.addView(textView);
            shelterListContainer.addView(tableRow);
        }
    }

    private void shelterClicked(View v) {
        setContentView(R.layout.shelter_info_layout);
        Model m = Model.getInstance();
        //The shelter index is stored in the tag
        Shelter shelter = m.getShelters().get((Integer) v.getTag());

        //Set all off the shelter info
        TextView text = (TextView) findViewById(R.id.shelterName);
        text.setText(getString(R.string.shelterName, shelter.getShelterName()));

        text = (TextView) findViewById(R.id.shelterRestrictions);
        text.setText(getString(R.string.shelterRestrictions, shelter.getRestrictions()));

        text = (TextView) findViewById(R.id.shelterLongitude);
        text.setText(getString(R.string.shelterLongitude, shelter.getLongitude() + ""));

        text = (TextView) findViewById(R.id.shelterLatitude);
        text.setText(getString(R.string.shelterLatitude, shelter.getLatitude() + ""));

        text = (TextView) findViewById(R.id.shelterAddress);
        text.setText(getString(R.string.shelterAddress, shelter.getAddress()));

        text = (TextView) findViewById(R.id.shelterPhoneNumber);
        text.setText(getString(R.string.shelterPhone, shelter.getPhoneNumber()));

        text = (TextView) findViewById(R.id.shelterSpecialNotes);
        text.setText(getString(R.string.shelterSpecialNotes, shelter.getSpecialNotes()));
    }

    //Reload shelter list screen when back is clicked
    public void infoBackClicked(View view) {
        createScreen();
    }
}
