package zepplins.zen.homelessassist.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import zepplins.zen.homelessassist.R;
import zepplins.zen.homelessassist.model.AgeRange;
import zepplins.zen.homelessassist.model.Gender;
import zepplins.zen.homelessassist.model.Model;

public class SearchActivity extends AppCompatActivity {
    private String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_shelters);
        source = getIntent().getStringExtra("source");
        //Set options in the gender spinner
        Spinner spinner = (Spinner) findViewById(R.id.genderSpinner);
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Gender.values()));

        //Set options in the age range spinner
        spinner = (Spinner) findViewById(R.id.ageSpinner);
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, AgeRange.values()));
    }

    //Reload shelter list screen when back is clicked
    public void backClicked(View view) {
        if (source.equals("shelters")) {
            Intent i = new Intent(getApplicationContext(), SheltersActivity.class);
            startActivity(i);
        } else if (source.equals("map")) {
            Intent i = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(i);
        }
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
        backClicked(null);
    }
}
