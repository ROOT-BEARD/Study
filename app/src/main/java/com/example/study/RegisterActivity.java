package com.example.study;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;

    private Button registerButton;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.registerEmailInput);
        passwordInput = findViewById(R.id.registerPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);

        registerButton.setOnClickListener(v -> registerUser());

        loginLink.setOnClickListener(v -> {
            finish();
        });
    }

    private void registerUser() {

        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword =
                confirmPasswordInput.getText().toString();

        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            emailInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError(
                    "Password must be at least 6 characters"
            );
            passwordInput.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError(
                    "Passwords do not match"
            );
            confirmPasswordInput.requestFocus();
            return;
        }

        // Firebase registration will be added later.
        Toast.makeText(
                this,
                "Input validated. Firebase registration not connected yet.",
                Toast.LENGTH_SHORT
        ).show();
    }
}
