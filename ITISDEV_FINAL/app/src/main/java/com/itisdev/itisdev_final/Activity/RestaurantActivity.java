package com.itisdev.itisdev_final.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itisdev.itisdev_final.Adapter.RestaurantAdapter;
import com.itisdev.itisdev_final.Adapter.ReviewAdapter;
import com.itisdev.itisdev_final.Domain.Review;
import com.itisdev.itisdev_final.R;
import com.itisdev.itisdev_final.databinding.ActivityRestaurantBinding;

import java.util.ArrayList;
import java.util.List;

public class RestaurantActivity extends BaseActivity {


    private ActivityRestaurantBinding binding;
    private List<Review> reviews;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        RecyclerView recyclerView = binding.reviewsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // initialize reviews
        reviews = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviews);
        recyclerView.setAdapter(reviewAdapter);

        int restaurantId = getIntent().getIntExtra("restaurantId", -1);

        if (restaurantId != -1) {
            // initialize Realtime DB

            DatabaseReference databaseReference = database.getReference("restaurants");
            Query query = databaseReference.orderByChild("id").equalTo(restaurantId);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Log.e("Snapshot", dataSnapshot.toString());
                        // Retrieve restaurant information
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String openingHours = dataSnapshot.child("openingHours").getValue(String.class);
                        double rating = dataSnapshot.child("rating").getValue(Double.class);
                        String tags = dataSnapshot.child("tags").getValue(String.class);

                        binding.restaurantName.setText(name);
                        binding.restaurantRating.setText("⭐ " + rating);
                        binding.openingHoursTxt.setText(openingHours);

                        if (tags != null && !tags.isEmpty()) {
                            String[] tagArray = tags.split(",");
                            addTagsToFlexbox(tagArray);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", error.getMessage());
                }
            });

            query = database.getReference().child("reviews").orderByChild("restaurantId").equalTo(restaurantId);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    reviews.clear();
                    for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                        Review review = reviewSnapshot.getValue(Review.class);
                        reviews.add(review);
                    }
                    reviewAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", error.getMessage());
                }
            });
        } else {
            Log.e("RestaurantActivity", "Invalid restaurantId");
        }
    }

    private void addTagsToFlexbox(String[] tagArray) {
        binding.tagsContainer.removeAllViews();

        for (String tag : tagArray) {
            TextView tagView = new TextView(this);
            tagView.setText(tag.trim());
            tagView.setPadding(16, 8, 16, 8);
            tagView.setBackgroundResource(R.drawable.tag_background);
            tagView.setTextSize(14);
            tagView.setTextColor(ContextCompat.getColor(this, R.color.white));

            // Add margin dynamically
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            tagView.setLayoutParams(params);

            // Add the tag to the FlexboxLayout
            binding.tagsContainer.addView(tagView);
        }
    }
}