package com.example.study.Pages;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.study.R;

import java.util.ArrayList;
import java.util.List;

public class CompleteActivity extends AppCompatActivity {

    Button continueButton, retryButton;
    TextView totalText, timeText;
    String setId;
    int total;

    List<String> learned = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.complete_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        total = getIntent().getIntExtra("totalCards", 0);
        learned = getIntent().getStringArrayListExtra("learned");
        setId = getIntent().getStringExtra("setId");
        //CardService.markLearnedCards(learned);

        continueButton = findViewById(R.id.buttonContinue);
        retryButton = findViewById(R.id.buttonRetry);
        totalText = findViewById(R.id.textviewTotal);
        timeText = findViewById(R.id.textTime);

        continueButton.setOnClickListener((e)->{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
        retryButton.setOnClickListener((e)->{
            Intent intent = new Intent(this, StudyActivity.class);
            intent.putExtra("setId", setId);
            startActivity(intent);
        });
        getTime();
        totalText.setText((learned.size()) + "/" + total);
    }

    private void getTime(){
        int seconds = getIntent().getIntExtra("seconds", 0);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        String timeString = String.format("%02d:%02d", minutes, seconds);

        timeText.setText(timeString);
    }
}