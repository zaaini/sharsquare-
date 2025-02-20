package com.example.ss;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FeedbackActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextFeedback;
    private Button buttonSubmit;

    // Firebase Database reference
    private DatabaseReference databaseFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Initialize Firebase Database reference
        databaseFeedback = FirebaseDatabase.getInstance().getReference("feedback");

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextFeedback = findViewById(R.id.editTextFeedback);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });
    }

    private void submitFeedback() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String feedback = editTextFeedback.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(feedback)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create feedback ID and save feedback data
        Feedback feedbackData = new Feedback(name, email, feedback);
        databaseFeedback.push().setValue(feedbackData);

        Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_LONG).show();

        // Clear the input fields after submission
        editTextName.setText("");
        editTextEmail.setText("");
        editTextFeedback.setText("");
    }
}
