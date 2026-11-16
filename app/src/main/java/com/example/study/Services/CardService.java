package com.example.study.Services;

import com.example.study.Database.Database;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CardService {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addCard(Database.Card card){
        db.collection("Cards")
                .add(card);
    }

    public interface onCardCallback{
        void onLoaded(List<Database.Card> cards);
        //void onError();
    }

    public void getCards(String setId, onCardCallback listener){
        db.collection("Cards")
                .whereEqualTo("Set_id", setId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Database.Card> cards = queryDocumentSnapshots.toObjects(Database.Card.class);
                    listener.onLoaded(cards);
                });
    }

    public static void markLearnedCards(List<String> card_ids){
        for(String id : card_ids){
            Database.Learned learned = new Database.Learned("1", id);
            db.collection("Learned")
                    .add(learned);
        }
    }

}
