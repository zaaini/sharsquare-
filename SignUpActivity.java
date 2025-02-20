package com.example.ss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private EditText signupUsername, signupEmail, signupPassword, signupConfirmPassword, signupDOB;
    private Spinner signupGender;
    private Button signupButton;
    private TextView loginRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize FirebaseAuth and FirebaseDatabase reference
        auth = FirebaseAuth.getInstance();

        // Custom FirebaseDatabase instance URL
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://sharesquare-3c99c-default-rtdb.firebaseio.com/");
        // Reference to the users node
        databaseReference = database.getReference().child("userDetails");

        // Reference UI components
        signupUsername = findViewById(R.id.signup_first_name);
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupConfirmPassword = findViewById(R.id.signup_confirm_password);
        signupDOB = findViewById(R.id.signup_dob);
        signupGender = findViewById(R.id.signup_gender);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        // Set up button listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSignUp();
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
    }

    private void handleSignUp() {
        final String username = signupUsername.getText().toString().trim();
        final String email = signupEmail.getText().toString().trim();
        final String password = signupPassword.getText().toString().trim();
        String confirmPassword = signupConfirmPassword.getText().toString().trim();
        final String dob = signupDOB.getText().toString().trim();
        final String gender = signupGender.getSelectedItem().toString();

        // Validate inputs
        if (!validateInputs(email, password, confirmPassword)) return;

        // Create user with FirebaseAuth
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser != null) {
                        sendVerificationEmail(firebaseUser, username, email, dob, gender);
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, "Could not register. Please try again later." + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validateInputs(String email, String password, String confirmPassword) {
        if (email.isEmpty()) {
            signupEmail.setError("Email cannot be empty");
            return false;
        }
        if (password.isEmpty()) {
            signupPassword.setError("Password cannot be empty");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            signupConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private void sendVerificationEmail(FirebaseUser firebaseUser, String username, String email, String dob, String gender) {
        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    saveUserData(firebaseUser.getUid(), username, email, dob, gender);
                } else {
                    Toast.makeText(SignUpActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUserData(String uid, String username, String email, String dob, String gender) {
        User user = new User(username, email, dob, gender);
        databaseReference.child(uid).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "Registered successfully. Please check your email for verification", Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // User class to structure the data
    public static class User {
        public String username, email, dob, gender;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String email, String dob, String gender) {
            this.username = username;
            this.email = email;
            this.dob = dob;
            this.gender = gender;
        }
    }
}
