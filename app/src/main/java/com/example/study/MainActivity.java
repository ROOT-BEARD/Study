package com.example.study;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button statsButton = findViewById(R.id.stats_button);
        Button timerButton = findViewById(R.id.timer_button);
        Button studyButton = findViewById(R.id.study_button);
        Button setsButton = findViewById(R.id.sets_button);

        timerButton.setOnClickListener(v -> openScreen(TimerActivity.class));
    }
    private void openScreen(Class<?> destination) {
        startActivity(new Intent(this, destination));
    }
}