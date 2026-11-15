package com.example.study;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
    private Button skipButton;
    private List<Database.Card> setCards = new ArrayList<>();
    private CardService cardService = new CardService();
    private int currentCard = 0;
    boolean front = true;

    float x = 0.0f;
    float y = 0.0f;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.study_page);
        setId = getIntent().getStringExtra("setId");

        Toast.makeText(this, "Set Id: " + setId, Toast.LENGTH_SHORT).show();
        frontText = findViewById(R.id.textViewFront);
        skipButton = findViewById(R.id.buttonSkip);

        cardService.getCards(setId, new CardService.onCardCallback() {
            @Override
            public void onLoaded(List<Database.Card> cards) {
                setCards = cards;
                frontText.setText(setCards.get(currentCard).front);

                skipButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(currentCard < setCards.size() - 1) currentCard++;
                        else currentCard = 0;

                        front = true;
                        frontText.setText(setCards.get(currentCard).front);
                    }
                });
            }
        });


        frontText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x = motionEvent.getRawX();
                        y = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        frontText.setTranslationX(motionEvent.getRawX() - x);
                        frontText.setTranslationY(motionEvent.getRawY() - y);
                        frontText.setRotation((motionEvent.getRawX() - x) * 0.05f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(motionEvent.getRawX() - 200 > x){
                            swipeRight();
                            return true;
                        }
                        else if(motionEvent.getRawX() + 200 < x){
                            swipeLeft();
                            return true;
                        }
                        else{
                            frontText.animate()
                            .translationX(0.0f)
                            .translationY(0.0f)
                            .rotation(0.0f)
                            .setDuration(100)
                            .start();
                        }
                        front = !front;
                        if(front){
                            frontText.setText(setCards.get(currentCard).front);
                        } else {
                            frontText.setText(setCards.get(currentCard).back);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void swipeRight(){
        swipeCard(true);
    }

    private void swipeLeft(){
        swipeCard(false);
    }

    private void restockCard(){
        frontText.setAlpha(0.0f);
        frontText.setRotation(0.0f);
        frontText.setTranslationX(0.0f);
        frontText.setTranslationY(0.0f);

        frontText.animate()
                .alpha(1.0f)
                .setDuration(50)
                .start();
    }

    private void swipeCard(boolean right){
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        frontText.animate()
                .translationX(right?screenWidth:-screenWidth)
                .setDuration(100)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        frontText.postDelayed(()->{
                            restockCard();
                        }, 400);
                    }
                })
                .start();
    }
}
