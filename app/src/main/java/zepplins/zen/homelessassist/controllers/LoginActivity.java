package zepplins.zen.homelessassist.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.w3c.dom.Text;

import zepplins.zen.homelessassist.R;
import zepplins.zen.homelessassist.model.Model;

public class LoginActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial_view);
    }

    public void onGoToLogIn(View view) {
        setContentView(R.layout.login);
    }

    public void onGoToRegister(View view) {
        setContentView(R.layout.register);
    }

    public void onCancelClicked(View view) {
        setContentView(R.layout.initial_view);
    }

    public void onRegisterClicked(View view) {
        TextView email = (TextView) findViewById(R.id.email);
        TextView password = (TextView) findViewById(R.id.password);
        TextView name = (TextView) findViewById(R.id.name);
        Spinner accountType = (Spinner) findViewById(R.id.accountTypeSpinner);
        TextView accountCode = (TextView) findViewById(R.id.accountCode);
        registerUser(email.getText().toString(), email.getText().toString(), name.getText().toString(),
                accountType.getSelectedItem().toString(), accountCode.getText().toString());
    }

    public void onSignInClicked(View view) {
        TextView email = (TextView) findViewById(R.id.email);
        TextView password = (TextView) findViewById(R.id.password);
        signInUser(email.getText().toString(), email.getText().toString());
    }

    //Sign a user into Firebase
    public void signInUser(String email, String password) {
        FirebaseAuth auth = Model.getInstance().getAuthenticator();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent i = new Intent(getApplicationContext(), SheltersActivity.class);
                    startActivity(i);
                } else {
                    Log.d("Log In", task.getException().toString());
                    TextView failed = (TextView) findViewById(R.id.logInFailed);
                    failed.setVisibility(TextView.VISIBLE);
                }
            }
        });
    }

    //Register the user on Firebase
    public void registerUser(String email, String password, String name, String _userType, String _accountCode) {
        //final necessary to be referenced by internal class
        final FirebaseAuth auth = Model.getInstance().getAuthenticator();
        final String displayName = name;
        final String userType = _userType;
        final String accountCode = _accountCode;
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Model m = Model.getInstance();
                if (task.isSuccessful() && m.validateRegistration(userType, accountCode)) {
                    m.setUserDetails(displayName, userType);
                    Intent i = new Intent(getApplicationContext(), SheltersActivity.class);
                    startActivity(i);
                } else {
                    Log.d("Log In", task.getException().toString());
                    TextView failed = (TextView) findViewById(R.id.registerFailed);
                    failed.setVisibility(TextView.VISIBLE);
                }
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        View codeEntry = findViewById(R.id.accountCode);
        if (view == null || pos == 0) {
            codeEntry.setVisibility(View.GONE);
        } else {
            codeEntry.setVisibility(View.VISIBLE);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        onItemSelected(parent, null, 0, 0);
    }

}
