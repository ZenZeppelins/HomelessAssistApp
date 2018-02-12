package zepplins.zen.homelessassist.model;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by mayhul on 2/11/18.
 */

public class Model {
    private static final Model _instance = new Model();
    public static Model getInstance() { return _instance; }

    private FirebaseAuth mAuth;

    public Model() {
        mAuth = FirebaseAuth.getInstance();
    }

    public boolean verifyUser(String username, String password) {
        return (username.equals("user") && password.equals("pass"));
    }

    public FirebaseAuth getAuthenticator() {
        return mAuth;
    }
}

