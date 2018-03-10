package zepplins.zen.homelessassist.controllers;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import zepplins.zen.homelessassist.R;
import zepplins.zen.homelessassist.model.AgeRange;
import zepplins.zen.homelessassist.model.Gender;
import zepplins.zen.homelessassist.model.Model;
import zepplins.zen.homelessassist.model.Shelter;

public class SheltersActivity extends AppCompatActivity {
    //This is the shelter that's info is currently being viewed
    private Shelter activeShelter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createShelterView();
    }

    //This creates the log out button, search button and loads the shelter list
    private void createShelterView() {
        setContentView(R.layout.activity_shelters);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.logOutFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Model.getInstance().getAuthenticator().signOut();
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.searchFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSearch();
            }
        });

        createShelterList();
    }

    //Iterate through the active shelter list. For each shelter, create a TableRow and a TextView
    private void createShelterList() {
        TableLayout shelterListContainer = (TableLayout) findViewById(R.id.shelterListContainer);
        Model m = Model.getInstance();
        List<Shelter> shelterList = m.getActiveShelters();
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        //Change how the shelter list should look here!!
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
        activeShelter = m.getActiveShelters().get((Integer) v.getTag());
        loadShelterData();
    }

    //Reload shelter list screen when back is clicked
    public void backClicked(View view) {
        activeShelter = null;
        createShelterView();
    }

    //Go to search view. Set the options the spinners should have
    public void goToSearch() {
        setContentView(R.layout.search_shelters);
        //Set options in the gender spinner
        Spinner spinner = (Spinner) findViewById(R.id.genderSpinner);
        spinner.setAdapter(new ArrayAdapter<Gender>(this,
                android.R.layout.simple_spinner_item, Gender.values()));

        //Set options in the age range spinner
        spinner = (Spinner) findViewById(R.id.ageSpinner);
        spinner.setAdapter(new ArrayAdapter<AgeRange>(this,
                android.R.layout.simple_spinner_item, AgeRange.values()));
    }

    //Called when search button is clicked. Use the Model search method to load the active shelters
    public void searchClicked(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.genderSpinner);
        Gender g = (Gender) spinner.getSelectedItem();
        spinner = (Spinner) findViewById(R.id.ageSpinner);
        AgeRange age = (AgeRange) spinner.getSelectedItem();
        TextView textView = (TextView) findViewById(R.id.searchShelterName);
        String search = textView.getText().toString();

        Model.getInstance().search(g, age, search);
        createShelterView();
    }

    public void claimClicked(View view) {
        String val = ((EditText) findViewById(R.id.numBeds)).getText().toString();
        try {
            int num = Integer.parseInt(val);
            Model.getInstance().claimBeds(num, activeShelter);
        } catch (NumberFormatException nfe) {
            return;
        }
    }

    public void loadShelterData() {
        if (activeShelter == null) {
            return;
        }
        //Set all off the shelter info
        TextView text = (TextView) findViewById(R.id.shelterName);
        text.setText(getString(R.string.shelterName, activeShelter.getShelterName()));

        text = (TextView) findViewById(R.id.shelterCapacity);
        text.setText(getString(R.string.shelterCapacity, activeShelter.getCapacity()));

        text = (TextView) findViewById(R.id.shelterVacancy);
        text.setText(getString(R.string.shelterVacancy, activeShelter.getVacancy()));

        text = (TextView) findViewById(R.id.shelterRestrictions);
        text.setText(getString(R.string.shelterRestrictions, activeShelter.getRestrictions()));

        text = (TextView) findViewById(R.id.shelterLongitude);
        text.setText(getString(R.string.shelterLongitude, activeShelter.getLongitude() + ""));

        text = (TextView) findViewById(R.id.shelterLatitude);
        text.setText(getString(R.string.shelterLatitude, activeShelter.getLatitude() + ""));

        text = (TextView) findViewById(R.id.shelterAddress);
        text.setText(getString(R.string.shelterAddress, activeShelter.getAddress()));

        text = (TextView) findViewById(R.id.shelterPhoneNumber);
        text.setText(getString(R.string.shelterPhone, activeShelter.getPhoneNumber()));

        text = (TextView) findViewById(R.id.shelterSpecialNotes);
        text.setText(getString(R.string.shelterSpecialNotes, activeShelter.getSpecialNotes()));
    }
}
