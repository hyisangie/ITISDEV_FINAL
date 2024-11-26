package com.itisdev.itisdev_final.Adapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itisdev.itisdev_final.Activity.MainActivity;
import com.itisdev.itisdev_final.Activity.RestaurantActivity;
import com.itisdev.itisdev_final.Domain.Restaurant;
import com.itisdev.itisdev_final.R;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    private Context context;
    private final List<Restaurant> data;

    public RestaurantAdapter(Context context, List<Restaurant> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.restaurantName.setText(data.get(position).getName());
        holder.rating.setText(data.get(position).getRating().toString());
        DatabaseReference cuisineRef = FirebaseDatabase.getInstance().getReference("cuisineRef");
        Query query = cuisineRef.orderByChild("id").equalTo(data.get(position).getCuisineType());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    holder.cuisineType.setText(dataSnapshot.child("type").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
            }
        });

        String openingHours = data.get(position).getOpeningHours();
        if (openingHours == null || openingHours.isEmpty()) {
            openingHours = "Unavailable";
        }
        holder.openingHours.setText(openingHours);

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RestaurantActivity.class);
            intent.putExtra("restaurantId", data.get(position).getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView restaurantName, rating, cuisineType;
        Chip openingHours;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantName = itemView.findViewById(R.id.restaurantNameTxt);
            rating = itemView.findViewById(R.id.ratingTxt);
            cuisineType = itemView.findViewById(R.id.cuisineTxt);
            openingHours = itemView.findViewById(R.id.openingHoursChip);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
