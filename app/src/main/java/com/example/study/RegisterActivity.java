
package com.example.study;

import com.example.study.MainActivity;
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

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = new AuthService();

        // Connect UI elements
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.registerEmailInput);
        passwordInput = findViewById(R.id.registerPasswordInput);
        confirmPasswordInput =
                findViewById(R.id.confirmPasswordInput);

        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);

        // Create account
        registerButton.setOnClickListener(v -> registerUser());

        // Return to login
        loginLink.setOnClickListener(v -> finish());
    }

    private void registerUser() {

        String name = nameInput.getText()
                .toString().trim();

        String email = emailInput.getText()
                .toString().trim();

        String password = passwordInput.getText()
                .toString();

        String confirmPassword = confirmPasswordInput
                .getText().toString();

        // Validate display name
        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        // Validate email
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

        // Validate password
        if (password.length() < 6) {
            passwordInput.setError(
                    "Password must be at least 6 characters"
            );
            passwordInput.requestFocus();
            return;
        }

        // Confirm passwords match
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError(
                    "Passwords do not match"
            );
            confirmPasswordInput.requestFocus();
            return;
        }

        // Prevent multiple registration attempts
        registerButton.setEnabled(false);

        // Create Firebase account
        authService.register(name, email, password)
                .addOnCompleteListener(this, task -> {

                    registerButton.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                RegisterActivity.this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        openMainActivity();

                    } else {

                        Toast.makeText(
                                RegisterActivity.this,
                                "Registration failed: " +
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
                RegisterActivity.this,
                MainActivity.class
        );

        startActivity(intent);
        finishAffinity();
    }
}
