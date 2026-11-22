package com.example.study.Pages;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.study.Database.Database;
import com.example.study.R;
import com.example.study.Services.CardService;

import java.util.ArrayList;
import java.util.List;

public class StudyActivity extends AppCompatActivity {

    private String setId;
    private TextView frontText, progressText;
    private Button skipButton;
    private FrameLayout loadingFrame;
    private List<Database.Card> setCards = new ArrayList<>();
    private List<Database.Card> learnedCards = new ArrayList<>();
    private CardService cardService = new CardService();
    private int currentCard = 0;
    boolean front = true;
    private Handler handler = new Handler(Looper.getMainLooper());

    int complete = 0;
    int total = 0;
    int seconds = 0;
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
        progressText = findViewById(R.id.textProgress);
        skipButton = findViewById(R.id.buttonSkip);
        loadingFrame = findViewById(R.id.frameLoadingScreen);
        loadingFrame.setVisibility(View.VISIBLE);

        cardService.getCards(setId, new CardService.onCardCallback() {
            @Override
            public void onLoaded(List<Database.Card> cards) {
                setCards = cards;
                frontText.setText(setCards.get(currentCard).front);
                total = setCards.size();
                progressText.setText((complete + 1) + "/" + total);
                loadingFrame.setVisibility(View.GONE);

                skipButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(setCards.isEmpty() || setCards == null) return;

                        currentCard = (currentCard + 1) % setCards.size();

                        front = true;
                        frontText.setText(setCards.get(currentCard).front);
                    }
                });
            }
        });

        startStopwatch();


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
        deleteCard(true);
    }

    private void swipeLeft(){
        swipeCard(false);
        deleteCard(false);
    }

    private void deleteCard(boolean learned){
        if(learned) learnedCards.add(setCards.get(currentCard));
        if(setCards.size() > 1) setCards.remove(currentCard);
        else {
            setComplete();
            return;
        }

        if(currentCard >= setCards.size()) currentCard = 0;

        front = true;
        frontText.setText(setCards.get(currentCard).front);
        complete++;
        progressText.setText((complete + 1)+ "/" + total);
    }

    private void setComplete(){
        ArrayList<String> learned_ids = new ArrayList<>();
        for(Database.Card card: learnedCards){
            learned_ids.add(card.id);
        }
        Intent intent = new Intent(StudyActivity.this, CompleteActivity.class);
        intent.putExtra("learned", learned_ids);
        intent.putExtra("setId", setId);
        intent.putExtra("seconds", seconds);
        intent.putExtra("totalCards", total);
        startActivity(intent);
        finish();
    }

    private void restockCard(){
        frontText.setAlpha(0.0f);
        frontText.setRotation(0.0f);
        frontText.setTranslationX(0.0f);
        frontText.setTranslationY(0.0f);
        front = true;
        frontText.setText(setCards.get(currentCard).front);

        frontText.animate()
                .alpha(1.0f)
                .setDuration(10)
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

    private void startStopwatch() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                seconds++;
                handler.postDelayed(this, 1000);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
