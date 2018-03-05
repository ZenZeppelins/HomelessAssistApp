package zepplins.zen.homelessassist.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import zepplins.zen.homelessassist.R;
import zepplins.zen.homelessassist.model.Model;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Make sure all shelters are shown on creation
        Model.getInstance().resetActiveList();
        setContentView(R.layout.initial_view);
    }

    public void onGoToLogIn(View view) {
        setContentView(R.layout.login);
    }

    public void onGoToRegister(View view) {
        setContentView(R.layout.register);
        //Set options in the registration spinner
        Spinner spinner = (Spinner) findViewById(R.id.accountTypeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.account_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Set listeners for spinner
        //When Admin or Employee is selected, show the TextView for the account code
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                View codeEntry = findViewById(R.id.accountCode);
                if (selectedItemView == null || pos == 0) {
                    codeEntry.setVisibility(View.GONE);
                } else {
                    codeEntry.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                onItemSelected(parentView, null, 0, 0);
            }
        });
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
        registerUser(email.getText().toString(), password.getText().toString(), name.getText().toString(),
                accountType.getSelectedItem().toString(), accountCode.getText().toString());
    }

    public void onSignInClicked(View view) {
        TextView email = (TextView) findViewById(R.id.email);
        TextView password = (TextView) findViewById(R.id.password);
        signInUser(email.getText().toString(), password.getText().toString());
    }

    //Sign a user into Firebase
    public void signInUser(String email, String password) {
        if (email == null || password == null || email.length() == 0 || password.length() == 0) {
            failedSignIn();
            return;
        }
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
                    failedSignIn();
                }
            }
        });
    }

    //Register the user on Firebase
    public void registerUser(String _email, String password, String name, String _userType, String _accountCode) {
        if (_email == null || password == null || _email.length() == 0 || password.length() == 0) {
            failedRegistration();
            return;
        }
        //final necessary to be referenced by internal class
        final FirebaseAuth auth = Model.getInstance().getAuthenticator();
        final String displayName = name;
        final String email = _email;
        final String userType = _userType;
        final String accountCode = _accountCode;
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Model m = Model.getInstance();
                //Check that the user is allowed to register as that userType
                if (task.isSuccessful() && m.validateRegistration(userType, accountCode)) {
                    m.setUserDetails(email, displayName, userType);
                    Intent i = new Intent(getApplicationContext(), SheltersActivity.class);
                    startActivity(i);
                } else {
                    Log.d("Log In", task.getException().toString());
                    failedRegistration();
                }
            }
        });
    }

    //What to do when a registration fails
    private void failedRegistration() {
        TextView failed = (TextView) findViewById(R.id.registerFailed);
        failed.setVisibility(TextView.VISIBLE);
    }

    //What to do when sign in fails
    private void failedSignIn() {
        TextView failed = (TextView) findViewById(R.id.logInFailed);
        failed.setVisibility(TextView.VISIBLE);
    }

}
