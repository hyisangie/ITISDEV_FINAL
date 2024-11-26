package com.itisdev.itisdev_final.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itisdev.itisdev_final.Adapter.ReviewAdapter;
import com.itisdev.itisdev_final.Domain.Review;
import com.itisdev.itisdev_final.databinding.ActivityRestaurantProfileBinding;

import java.util.ArrayList;
import java.util.List;

public class RestaurantProfileActivity extends BaseActivity {

    private ActivityRestaurantProfileBinding binding;
    private List<Review> reviews;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get from realtime DB
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants");

        // TODO: change this to dynamic
        databaseReference.orderByChild("id").equalTo("1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String cuisineType = "";
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String restaurantName = dataSnapshot.child("name").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    binding.restaurantName.setText(restaurantName);
                    binding.restaurantDescription.setText(description);
                    cuisineType = dataSnapshot.child("cuisineType").getValue(String.class);
                }
                database.getReference("cuisineRef").orderByChild("id").equalTo(cuisineType).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            binding.restaurantCuisine.setText(dataSnapshot.child("type").getValue(String.class));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // initialize reviews
        RecyclerView recyclerView = binding.reviewsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // initialize reviews
        reviews = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviews);
        recyclerView.setAdapter(reviewAdapter);

        // TODO: change this to dynamic
        String restaurantId = "1";

        if (restaurantId != null) {
            // initialize Realtime DB
            Query query = database.getReference().child("reviews").orderByChild("restaurantId").equalTo(restaurantId);
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
}