package com.example.study;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private Button btnStudy;
    private Button btnStatistics;
    private Button btnSets;
    private Button btnLogout;
    private Button buttonAuth;
    private Button btnTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        // Match IDs in activity_main.xml
        btnStudy = findViewById(R.id.study_button);
        btnStatistics = findViewById(R.id.stats_button);
        btnSets = findViewById(R.id.sets_button);
        btnLogout = findViewById(R.id.btnLogout);
        buttonAuth = findViewById(R.id.buttonAuth);
        btnTimer = findViewById(R.id.timer_button);

        // Firebase login
        buttonAuth.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    LoginActivity.class
            );
            startActivity(intent);
        });

        // Firebase logout
        btnLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();

            Toast.makeText(
                    MainActivity.this,
                    "Logged out successfully",
                    Toast.LENGTH_SHORT
            ).show();

            updateAuthButtons();
        });

        // Study button
        btnStudy.setOnClickListener(v -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user == null) {
                Toast.makeText(
                        MainActivity.this,
                        "Please log in first",
                        Toast.LENGTH_SHORT
                ).show();

                startActivity(new Intent(
                        MainActivity.this,
                        LoginActivity.class
                ));
                return;
            }

            // Study page navigation will be connected
            // to your team's existing StudyPage activity.
            Toast.makeText(
                    MainActivity.this,
                    "Study selected",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Statistics button
        btnStatistics.setOnClickListener(v -> {
            Toast.makeText(
                    MainActivity.this,
                    "Statistics selected",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Sets button
        btnSets.setOnClickListener(v -> {
            Toast.makeText(
                    MainActivity.this,
                    "Sets selected",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Timer button
        btnTimer.setOnClickListener(v -> {
            Toast.makeText(
                    MainActivity.this,
                    "Timer selected",
                    Toast.LENGTH_SHORT
            ).show();
        });

        updateAuthButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (firebaseAuth != null) {
            updateAuthButtons();
        }
    }

    private void updateAuthButtons() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            buttonAuth.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            buttonAuth.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
        }
    }
}
