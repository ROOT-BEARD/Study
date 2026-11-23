package com.example.study.Pages;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study.Database.Database;
import com.example.study.R;
import com.example.study.Services.CardService;
import com.example.study.cardOverviewAdapter;

import java.util.ArrayList;
import java.util.List;

public class SetOverviewActivity extends AppCompatActivity {


    private List<Database.Card> setCards = new ArrayList<>();
    private Button studyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_set_overview);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        studyButton = findViewById(R.id.buttonStudy);
        studyButton.setOnClickListener(v -> {
            Intent intent = new Intent(SetOverviewActivity.this, StudyActivity.class);
            intent.putExtra("setId", "1");
            startActivity(intent);
        });

        getCards();
    }

    private void getCards(){
        CardService cardService = new CardService();
        cardService.getCards("1", new CardService.onCardCallback() {
            @Override
            public void onLoaded(List<Database.Card> cards) {
                setCards = cards;

                RecyclerView recyclerView = findViewById(R.id.recycler_view_cards);
                recyclerView.setLayoutManager(new LinearLayoutManager(SetOverviewActivity.this));

                cardOverviewAdapter adapter = new cardOverviewAdapter(setCards);
                recyclerView.setAdapter(adapter);

            }
        });
    }
}