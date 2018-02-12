package zepplins.zen.homelessassist.controllers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import zepplins.zen.homelessassist.R;
import zepplins.zen.homelessassist.model.Model;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    //Sign a user into Firebase
    public void signInUser(String email, String password) {
        FirebaseAuth auth = Model.getInstance().getAuthenticator();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Call method for successful sign in
                } else {
                    //updateUI("a", "b");
                    //Call method for failed sign in
                }
            }
        });
    }

    //Register the user on Firebase
    public void registerUser(String email, String password, String name) {
        //final necessary to be referenced by internal class
        final FirebaseAuth auth = Model.getInstance().getAuthenticator();
        final String displayName = name;
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Set user's name
                    FirebaseUser user = auth.getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build();
                    user.updateProfile(profileUpdates);
                    //Call method for successful registration
                } else {
                    //Call method for failed registration
                }
            }
        });
    }

}
