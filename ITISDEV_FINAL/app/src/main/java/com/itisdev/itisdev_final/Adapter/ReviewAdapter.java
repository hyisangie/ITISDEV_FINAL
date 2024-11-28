package com.itisdev.itisdev_final.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itisdev.itisdev_final.Domain.Review;
import com.itisdev.itisdev_final.R;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private Context context;
    private final List<Review> data;

    public ReviewAdapter(Context context, List<Review> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = data.get(position);

        holder.reviewContent.setText(review.getDescription());
        holder.reviewRating.setRating(review.getRating());

        String reviewImage = review.getImage();
        if (reviewImage != null && !reviewImage.isEmpty()) {
            holder.reviewImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(reviewImage)
                    .into(holder.reviewImage);
        } else {
            holder.reviewImage.setVisibility(View.GONE);
        }

        // Query Firebase to get email for the userId
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(review.getUserId());  // userId

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // get user's email
                    String email = snapshot.child("email").getValue(String.class);
                    String profileImage = snapshot.child("profileImage").getValue(String.class);

                    holder.reviewUsername.setText(email != null ? email : "Anonymous User");

                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(context)
                                .load(profileImage)
                                .placeholder(R.drawable.baseline_person_24)
                                .into(holder.reviewProfileImage);
                    }

                } else {
                    holder.reviewUsername.setText("Unknown User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.reviewUsername.setText("Unknown User");
                Log.e("ReviewAdapter", "Error fetching user data", error.toException());
            }
        });


        // Query Firebase to get restaurant name for the resId
        DatabaseReference restaurantRef = FirebaseDatabase.getInstance().getReference("restaurants").child(String.valueOf(review.getRestaurantId()));
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String restaurantName = snapshot.child("name").getValue(String.class);
                    holder.reviewRestaurantName.setText(restaurantName);
                } else {
                    holder.reviewRestaurantName.setText("Unknown Restaurant Name");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to fetch restaurant name", error.toException());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView reviewProfileImage, reviewImage;
        TextView reviewUsername, reviewRestaurantName, reviewContent;
        RatingBar reviewRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            reviewProfileImage = itemView.findViewById(R.id.reviewProfileImg);
            reviewImage = itemView.findViewById(R.id.reviewImg);
            reviewUsername = itemView.findViewById(R.id.reviewUsernameTxt);
            reviewRestaurantName = itemView.findViewById(R.id.reviewRestaurantNameTxt);
            reviewContent = itemView.findViewById(R.id.reviewContentTxt);
            reviewRating = itemView.findViewById(R.id.reviewRatingRB);
        }
    }
}
