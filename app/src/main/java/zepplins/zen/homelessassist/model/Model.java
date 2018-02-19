package zepplins.zen.homelessassist.model;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import zepplins.zen.homelessassist.controllers.SheltersActivity;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by mayhul on 2/11/18.
 */

public class Model {
    private static final Model _instance = new Model();
    public static Model getInstance() { return _instance; }

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private final String adminCode = "adminsOnly";
    private final String employeeCode = "employeesOnly";

    public Model() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public FirebaseAuth getAuthenticator() {
        return mAuth;
    }

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
}

