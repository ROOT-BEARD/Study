package com.example.study.Database;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.sql.Struct;

public class Database {
    public static class Card {
        @DocumentId
        public String id;
        @PropertyName("Front")
        public String front;
        @PropertyName("Back")
        public String back;
        @PropertyName("Set_id")
        public String setId;

        // firebase needs a no argument constructor
        public Card(){}

        public Card(String id, String front, String back, String setId){
            this.id = id;
            this.front = front;
            this.back = back;
            this.setId = setId;
        }

        // used for when making a card without a known id
        public Card(String front, String back, String setId){
            this.front = front;
            this.back = back;
            this.setId = setId;
        }
    }

    public static class Set {
        @DocumentId
        public String id;
        @PropertyName("Set_name")
        public String set_name;
        @PropertyName("UID")
        public int uid;

        public Set(String set_name, int uid){
            this.set_name = set_name;
            this.uid = uid;
        }

        public Set(String id, String set_name, int uid){
            this.id = id;
            this.set_name = set_name;
            this.uid = uid;
        }
    }

    public static class Learned {
        @DocumentId
        public String id;
        @PropertyName("UID")
        public String user_id;
        @PropertyName("Card_id")
        public String card_id;

        public Learned() {}

        public Learned(String user_id, String card_id){
            this.card_id = card_id;
            this.user_id = user_id;
        }

        public Learned(String id, String user_id, String card_id){
            this.id = id;
            this.card_id = card_id;
            this.user_id = user_id;
        }
    }
}