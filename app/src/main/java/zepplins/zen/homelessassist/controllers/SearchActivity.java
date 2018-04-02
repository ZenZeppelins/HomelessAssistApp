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

/**
 * Activity when user is searching/filtering shelters
 */
public class SearchActivity extends AppCompatActivity {
    private String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_shelters);
        source = getIntent().getStringExtra("source");
        //Set options in the gender spinner
        Spinner spinner = findViewById(R.id.genderSpinner);
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Gender.values()));

        //Set options in the age range spinner
        spinner = findViewById(R.id.ageSpinner);
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, AgeRange.values()));
    }

    /**
     * Reload shelter list screen when back is clicked. Go back to the activity that called search
     * @param view Needed for android buttons
     */
    public void backClicked(View view) {
        if ("shelters".equals(source)) {
            Intent i = new Intent(getApplicationContext(), SheltersActivity.class);
            startActivity(i);
        } else if ("map".equals(source)) {
            Intent i = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(i);
        }
    }

    /**
     * Called when search button is clicked. Use the Model search method to load the active shelters
     * @param view Needed for android buttons
     */
    public void searchClicked(View view) {
        Spinner spinner = findViewById(R.id.genderSpinner);
        Gender g = (Gender) spinner.getSelectedItem();
        spinner = findViewById(R.id.ageSpinner);
        AgeRange age = (AgeRange) spinner.getSelectedItem();
        TextView textView = findViewById(R.id.searchShelterName);
        String search = textView.getText().toString();

        Model.getInstance().search(g, age, search);
        backClicked(null);
    }
}
