package zepplins.zen.homelessassist.model;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import zepplins.zen.homelessassist.controllers.SheltersActivity;

/**
 * Created by mayhul on 2/11/18.
 */

public class Model {
    private static final Model _instance = new Model();
    public static Model getInstance() { return _instance; }

    private FirebaseAuth mAuth;
    private final String adminCode = "adminsOnly";
    private final String employeCode = "employeesOnly";

    public Model() {
        mAuth = FirebaseAuth.getInstance();
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
        if (user.equals("Shelter Employee") && code.equals(employeCode)) {
            return true;
        }
        return false;
    }

    public void setUserDetails(String displayName, String userType) {
        FirebaseUser user = mAuth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();
        //set user type here
    }
}

