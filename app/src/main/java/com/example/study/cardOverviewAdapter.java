package com.example.study;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study.Database.Database;

import java.util.List;

public class cardOverviewAdapter extends RecyclerView.Adapter<cardOverviewAdapter.ViewHolder> {

    private final List<Database.Card> cardList;

    public cardOverviewAdapter(List<Database.Card> cardList) {
        this.cardList = cardList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView frontText, backText;
        public ViewHolder(@NonNull View view){
            super(view);

            frontText = view.findViewById(R.id.textViewFront);
            backText = view.findViewById(R.id.textViewBack);
        }
    }

    @NonNull
    @Override
    public cardOverviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_overview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull cardOverviewAdapter.ViewHolder holder, int position) {
        Database.Card curCard = cardList.get(position);
        holder.frontText.setText(curCard.front);
        holder.backText.setText(curCard.back);

        return;
    }

    @Override
    public int getItemCount() {
        return cardList == null ? 0 : cardList.size();
    }
}
