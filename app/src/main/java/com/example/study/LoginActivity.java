
package com.example.study;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView registerLink;

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authService = new AuthService();

        // Skip login if a user is already signed in
        if (authService.isLoggedIn()) {
            openMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        // Connect UI elements
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        // Login button
        loginButton.setOnClickListener(v -> loginUser());

        // Navigate to registration
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });
    }

    private void loginUser() {

        String email = emailInput.getText()
                .toString().trim();

        String password = passwordInput.getText()
                .toString();

        // Validate email
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Prevent multiple login attempts
        loginButton.setEnabled(false);

        // Authenticate with Firebase
        authService.login(email, password)
                .addOnCompleteListener(this, task -> {

                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Login successful!",
                                Toast.LENGTH_SHORT
                        ).show();

                        openMainActivity();

                    } else {

                        Toast.makeText(
                                LoginActivity.this,
                                "Login failed: " +
                                        (task.getException() != null
                                                ? task.getException().getMessage()
                                                : "Unknown error"),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void openMainActivity() {

        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        startActivity(intent);
        finish();
    }
}
