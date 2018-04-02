package zepplins.zen.homelessassist.model;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

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
 * Facade class that has methods to manipulate data as needed
 */
public final class Model {
    private static Model _instance;

    public static Model getInstance() {
        if (_instance == null) {
            _instance = new Model();
        }
        return _instance;
    }

    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;
    private final List<Shelter> shelters;
    private List<Shelter> activeShelters;

    //When Model is created, Firebase is instantiated and the shelters are acquired from the DB
    private Model() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        shelters = new LinkedList<>();
        activeShelters = new LinkedList<>();
        loadShelters();
        createVacancyListener();
    }

    /**
     * Gets all shelters that were loaded from the last search/filtering
     * Initially all the shelters
     * @return The list of shelters that met the criteria
     */
    public List<Shelter> getActiveShelters() {
        return activeShelters;
    }

    /**
     * Used to log people in to Firebase
     * @return The authenticator
     */
    public FirebaseAuth getAuthenticator() {
        return mAuth;
    }

    /**
     * This method checks if the user is allowed to register as an admin/employee
     * @param user The user type they are trying to register as
     * @param code The code they input
     * @return true if they entered the correct code
     */
    public boolean validateRegistration(String user, String code) {
        //These are the codes required to register as an admin or an employee
        String adminCode = "adminsOnly";
        String employeeCode = "employeesOnly";

        if ((user == null) || (code == null)) {
            return false;
        }
        return ("User").equals(user) || (("Admin").equals(user) && code.equals(adminCode)) || (("Shelter Employee").equals(user) && code.equals(employeeCode));
    }

    /**
     * When a user registers for the first time, this puts their data in the database
     * @param email User's email
     * @param displayName User's display name
     * @param userType User type (normal/admin/employee)
     */
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

    /**
     * Add all shelters that meet the criteria to the activeShelters list
     * @param gender The gender the shelter should support
     * @param age The age range the shelter should support
     * @param name Subset of text that the name should contain
     */
    public void search(Gender gender, AgeRange age, String name) {
        activeShelters.clear();
        for (Shelter s : shelters) {
            //Check if gender doesn't match
            if (gender != null) {
                if ((gender == Gender.MALE) && (s.getRestrictions().contains("Women"))) {
                    continue;
                } else if ((gender == Gender.FEMALE) && (s.getRestrictions().contains("Men"))) {
                    continue;
                }
            }
            //Check if age doesn't match
            if (age != null) {
                if ((age == AgeRange.FAMILIES) &&
                        (!s.getRestrictions().toLowerCase().contains("famil"))) {
                    continue;
                } else if ((age == AgeRange.CHILDREN) &&
                        (!s.getRestrictions().toLowerCase().contains("child"))) {
                    continue;
                } else if ((age == AgeRange.YOUNG_ADULTS) &&
                        (!s.getRestrictions().toLowerCase().contains("young"))) {
                    continue;
                }
            }
            //Check if name doesn't match
            if ((name != null) &&
                    (!s.getShelterName().toLowerCase().contains(name.toLowerCase()))) {
                continue;
            }
            activeShelters.add(s);
        }
    }

    /**
     * Clears the last search, so that all shelters show up on the list/map
     */
    public void resetActiveList() {
        activeShelters = new LinkedList<>(shelters);
    }

    /**
     * Claims beds for the user in a shelter by connecting to the Firebase DB
     * They can only claim beds if there are beds to be claimed and the user hasn't already claimed
     * beds.
     * @param amount How many beds to claim
     * @param shelter Which shelter to claim them at
     */
    public void claimBeds(int amount, Shelter shelter) {
        final int numBeds = amount;
        final int newVacancy = shelter.getVacancy() - amount;
        if (newVacancy < 0) {
            return;
        }
        final int index = shelters.indexOf(shelter);
        if (index < 0) {
            return;
        }
        //Check if user already claimed beds
        String userEmail = mAuth.getCurrentUser().getEmail();
        Query q = mDatabase.child("users").orderByChild("email").equalTo(userEmail);
        q.addListenerForSingleValueEvent((new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> userDataMap = (Map<String, Object>) dataSnapshot.getValue();
                String userID = "";
                Map<String, Object> userInfo = null;
                //Get the first map entry
                for (Map.Entry<String, Object> entry : userDataMap.entrySet()) {
                    userID = entry.getKey();
                    userInfo = (Map<String, Object>) entry.getValue();
                    break;
                }
                Long num = (Long) userInfo.get("numBeds");
                int alreadyClaimed = (num == null) ? 0 : (num.intValue());
                //Only allow the user to claim beds if they have 0 beds claimed
                if (alreadyClaimed == 0) {
                    //no beds claimed
                    //Change vacancy in database
                    mDatabase.child("shelters").child(index + "")
                            .child("vacancy").setValue(newVacancy);

                    //Change user info in database
                    mDatabase.child("users").child(userID).child("shelterID").setValue(index);
                    mDatabase.child("users").child(userID).child("numBeds").setValue(numBeds);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Database", "loadPost:onCancelled", databaseError.toException());
            }
        }));

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

    /**
     * Releases any beds the current user has claimed in Firebase
     */
    public void releaseBeds() {
        String userEmail = mAuth.getCurrentUser().getEmail();
        Query q = mDatabase.child("users").orderByChild("email").equalTo(userEmail);
        q.addListenerForSingleValueEvent((new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> userDataMap = (Map<String, Object>) dataSnapshot.getValue();
                String userID = "";
                Map<String, Object> userInfo = null;
                //Get the first map entry
                for (Map.Entry<String, Object> entry : userDataMap.entrySet()) {
                    userID = entry.getKey();
                    userInfo = (Map<String, Object>) entry.getValue();
                    break;
                }
                Long num = (Long) userInfo.get("numBeds");
                int claimed = (num == null) ? 0 : (num.intValue());
                //If the user has more than 0 beds claimed, increase vacancy of released shelter
                if (claimed != 0) {
                    Long shelterNum = (Long) userInfo.get("shelterID");
                    int shelterID = (shelterNum == null) ? 0 : (shelterNum.intValue());
                    int newVacancy = shelters.get(shelterID).getVacancy() + claimed;
                    mDatabase.child("shelters").child(shelterID + "").child("vacancy").
                            setValue(newVacancy);
                }
                mDatabase.child("users").child(userID).child("numBeds").setValue(0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Database", "loadPost:onCancelled", databaseError.toException());
            }
        }));
    }
}

