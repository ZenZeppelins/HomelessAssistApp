package zepplins.zen.homelessassist.model;

import android.content.Intent;
import android.opengl.GLException;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import zepplins.zen.homelessassist.controllers.SheltersActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Created by mayhul on 2/11/18.
 */

public class Model {
    private static Model _instance;

    public static Model getInstance() {
        if (_instance == null) {
            _instance = new Model();
        }
        return _instance;
    }

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private List<Shelter> shelters;
    private List<Shelter> activeShelters;
    //These are the codes required to register as an admin or an employee
    private final String adminCode = "adminsOnly";
    private final String employeeCode = "employeesOnly";

    //When Model is created, Firebase is instantiated and the shelters are acquired from the DB
    private Model() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        shelters = new LinkedList<>();
        activeShelters = new LinkedList<>();
        loadShelters();
    }

    public List<Shelter> getActiveShelters() {
        return activeShelters;
    }

    public FirebaseAuth getAuthenticator() {
        return mAuth;
    }

    //This method checks if the user is allowed to register as a admin/employee
    public boolean validateRegistration(String user, String code) {
        if (user == null || code == null) {
            return false;
        }
        if (user.equals("User")) {
            return true;
        }
        if (user.equals("Admin") && code.equals(adminCode)) {
            return true;
        }
        if (user.equals("Shelter Employee") && code.equals(employeeCode)) {
            return true;
        }
        return false;
    }

    public void setUserDetails(String email, String displayName, String userType) {
        FirebaseUser user = mAuth.getCurrentUser();
        //Set the user's name in Firebase's DB
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();
        //Add userType to database
        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("userType", userType);
        String key = mDatabase.child("users").push().getKey();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + key, userData);
        mDatabase.updateChildren(childUpdates);
    }

    private void loadShelters() {
        //Get shelters from Firebase DB and put them in shelters
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> shelterList = dataSnapshot.child("shelters").getChildren();
                for (DataSnapshot shelter : shelterList) {
                    Shelter newShelter = shelter.getValue(Shelter.class);
                    shelters.add(newShelter);
                    activeShelters.add(newShelter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Database", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    public void search(Gender gender, AgeRange age, String name) {
        activeShelters.clear();
        for (Shelter s : shelters) {
            //Check if gender doesn't match
            if (gender != null) {
                if (gender == Gender.MALE && s.getRestrictions().contains("Women")) {
                    continue;
                } else if (gender == Gender.FEMALE && s.getRestrictions().contains("Men")) {
                    continue;
                }
            }
            //Check if age doesn't match
            if (age != null) {
                if (age == AgeRange.FAMILIES && !s.getShelterName().contains("Families")) {
                    continue;
                } else if (age == AgeRange.CHILDREN && !s.getShelterName().contains("Children")) {
                    continue;
                }
            }
            //Check if name doesn't match
            if (name != null && !s.getShelterName().contains(name)) {
                continue;
            }
            activeShelters.add(s);
        }
    }
}

