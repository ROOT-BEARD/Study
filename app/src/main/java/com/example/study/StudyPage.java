package com.example.study;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.study.Database.Database;
import com.example.study.Services.CardService;

import java.util.ArrayList;
import java.util.List;

public class StudyPage extends AppCompatActivity {

    private String setId;
    private TextView frontText;
    private List<Database.Card> setCards = new ArrayList<>();
    private CardService cardService = new CardService();
    private Database.Card currentCard;
    boolean front = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.study_page);
        setId = getIntent().getStringExtra("setId");

        Toast.makeText(this, "Set Id: " + setId, Toast.LENGTH_SHORT).show();
        frontText = findViewById(R.id.textViewFront);

        cardService.getCards(setId, new CardService.onCardCallback() {
            @Override
            public void onLoaded(List<Database.Card> cards) {
                setCards = cards;
                currentCard = setCards.get(0);
                frontText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        front = !front;
                        if(front){
                            frontText.setText(currentCard.front);
                        } else {
                            frontText.setText(currentCard.back);
                        }
                    }
                });
            }
        });
    }
}
