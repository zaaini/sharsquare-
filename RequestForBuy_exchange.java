package com.example.ss;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RequestForBuy_exchange extends AppCompatActivity {

    TextView Req;
    Button button;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_for_buy_exchange);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Req = findViewById(R.id.requestforbuy);
        button = findViewById(R.id.sendtomail1);

        // Get the email from the intent and set it to the TextView
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            Req.setText(email);
        } else {
            Req.setText("Email not available");
        }

        // Set OnClickListener to the button to send an email
        button.setOnClickListener(v -> {
            String recipient = Req.getText().toString();
            if (recipient.equals("Email not available")) {
                Toast.makeText(RequestForBuy_exchange.this, "Invalid email address", Toast.LENGTH_SHORT).show();
            } else {
                sendEmail(recipient);
            }
        });
    }

    private void sendEmail(String recipient) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + recipient));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Test Email");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "This is a test email.");

        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        } else {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }
}
