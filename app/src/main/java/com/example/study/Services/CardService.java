package com.example.study.Services;

import com.example.study.Database.Database;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CardService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

}
