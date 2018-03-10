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
import com.google.firebase.database.Query;
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
        createVacancyListener();
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

    //Add all shelters that meet the criteria to the activeShelters list
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

    //Make active shelters entire shelter list
    public void resetActiveList() {
        activeShelters = new LinkedList<>(shelters);
    }

    //Claims a certain amount of beds in a certain shelter
    //Returns whether or not the claim is successful
    public boolean claimBeds(int amount, Shelter shelter) {
        final int numBeds = amount;
        final int newVacancy = shelter.getVacancy() - amount;
        if (newVacancy < 0) {
            return false;
        }
        final int index = shelters.indexOf(shelter);
        if (index < 0) {
            return false;
        }
        //Check if user already claimed beds
        String userEmail = mAuth.getCurrentUser().getEmail();
        Query q = mDatabase.child("users").orderByChild("email").equalTo(userEmail);
        q.addListenerForSingleValueEvent((new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot alreadyClaimed = dataSnapshot.child("numBeds");
                if (alreadyClaimed.getValue() == null || (Integer) alreadyClaimed.getValue() == 0) {
                    //no beds claimed
                    //Change vacancy in database
                    mDatabase.child("shelters").child(index + "").child("vacancy").setValue(newVacancy);

                    //Change user info in database
                    String userID = dataSnapshot.getKey();
                    mDatabase.child("users").child(userID).child("shelterID").setValue(index);
                    mDatabase.child("users").child(userID).child("numBeds").setValue(numBeds);
                } else {
                    //beds already claimed
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Database", "loadPost:onCancelled", databaseError.toException());
            }
        }));
        
        return true;
    }

    //Creates a database listener.
    //Whenever the database changes, the vacancy values are updated in our Model
    private void createVacancyListener() {
        String path = "shelters";
        DatabaseReference ref = mDatabase.child(path);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> shelterList = dataSnapshot.getChildren();
                int i = 0;
                for (DataSnapshot shelter : shelterList) {
                    int newVacancy = ((Long) shelter.child("vacancy").getValue()).intValue();
                    if (i < shelters.size()) {
                        shelters.get(i).setVacancy(newVacancy);
                    }
                    i++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Database", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    public void releaseBeds() {
        String userEmail = mAuth.getCurrentUser().getEmail();
        Query q = mDatabase.child("users").orderByChild("email").equalTo(userEmail);
        q.addListenerForSingleValueEvent((new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userID = dataSnapshot.getKey();
                mDatabase.child("users").child(userID).child("numBeds").setValue(0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Database", "loadPost:onCancelled", databaseError.toException());
            }
        }));
    }
}

