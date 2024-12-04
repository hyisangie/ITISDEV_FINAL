package com.itisdev.itisdev_final.Adapter;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

        Restaurant restaurant = data.get(position);

        // restaurant name
        holder.restaurantName.setText(restaurant.getName());

        // set rating
        Float rating = restaurant.getRating();
        if (rating != null) {
            holder.rating.setVisibility(View.VISIBLE);
            holder.rating.setText(String.format("★ %.1f", rating));
        } else {
            holder.rating.setVisibility(View.GONE);
        }

        // set images
        List<String> images = restaurant.getImages();
        if (images != null && !images.isEmpty()) {
            // load the first image as preview
            Glide.with(context)
                    .load(images.get(0))
                    .error(R.drawable.placeholder_restaurant)
                    .centerCrop()
                    .into(holder.restaurantImage);
        }

        holder.cuisineType.setText(restaurant.getCuisineType());
        holder.descriptionTxt.setText(restaurant.getDescription());
        holder.openingHours.setText(restaurant.getOpeningHours());

        // setup click event
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RestaurantActivity.class);
            // sent restaurantId to Detailed page
            intent.putExtra("restaurantId", restaurant.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView restaurantName, rating, descriptionTxt;
        Chip openingHours,  cuisineType;
        CardView cardView;

        ImageView restaurantImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantName = itemView.findViewById(R.id.restaurantNameTxt);
            descriptionTxt = itemView.findViewById(R.id.descriptionTxt);
            rating = itemView.findViewById(R.id.ratingChip);
            cuisineType = itemView.findViewById(R.id.cuisineChip);
            openingHours = itemView.findViewById(R.id.openingHoursChip);
            restaurantImage = itemView.findViewById(R.id.restaurantImage);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
