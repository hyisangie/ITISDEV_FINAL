package com.itisdev.itisdev_final.Activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itisdev.itisdev_final.Adapter.ImageSliderAdapter;
import com.itisdev.itisdev_final.Adapter.ReviewAdapter;
import com.itisdev.itisdev_final.Domain.Review;
import com.itisdev.itisdev_final.databinding.ActivityRestaurantProfileBinding;

import java.util.ArrayList;
import java.util.List;

public class RestaurantProfileActivity extends BaseActivity {

    private ActivityRestaurantProfileBinding binding;
    private List<Review> reviews;
    private ReviewAdapter reviewAdapter;
    private String restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();

        // get restaurantId from Intent
        restaurantId = getIntent().getStringExtra("restaurantId");

        // get from realtime DB
        DatabaseReference databaseReference = database.getReference("restaurants");

        databaseReference.child(restaurantId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String restaurantName = snapshot.child("name").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String cuisineType = snapshot.child("cuisineType").getValue(String.class);

                    // Image URLs
                    List<String> imageUrls = new ArrayList<>();
                    DataSnapshot imagesSnapshot = snapshot.child("images");
                    for (DataSnapshot imageSnapshot : imagesSnapshot.getChildren()) {
                        String imageUrl = imageSnapshot.getValue(String.class);
                        if (imageUrl != null) {
                            imageUrls.add(imageUrl);
                        }
                    }

                    ViewPager2 imageSlider = binding.imageSlider;
                    ImageSliderAdapter adapter = new ImageSliderAdapter(RestaurantProfileActivity.this, imageUrls);
                    imageSlider.setAdapter(adapter);

                    binding.restaurantName.setText(restaurantName);
                    binding.restaurantDescription.setText(description);
                    binding.restaurantCuisine.setText(cuisineType);

                }
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

    private void setupClickListeners() {

        // manage profile click listener
        LinearLayout manageProfileContainer = binding.manageProfileContainer;
        if (manageProfileContainer != null) {
            manageProfileContainer.setOnClickListener(v -> {
                Intent intent = new Intent(RestaurantProfileActivity.this, SignupAsBusinessOwnerActivity.class);
                intent.putExtra("restaurantId", restaurantId);
                startActivity(intent);
            });
        }

        // Original voucher click listener
        LinearLayout voucherLayout = binding.voucherImg.getParent() instanceof LinearLayout ?
                (LinearLayout) binding.voucherImg.getParent() : null;

        if (voucherLayout != null) {
            voucherLayout.setOnClickListener(v -> {
                Intent intent = new Intent(RestaurantProfileActivity.this, VoucherInfosActivity.class);
                intent.putExtra("restaurantId", restaurantId);
                startActivity(intent);
            });
        }

        // other services click listener
        LinearLayout otherServicesLayout = binding.otherServicesImg.getParent() instanceof LinearLayout ?
                (LinearLayout) binding.otherServicesImg.getParent() : null;

        if (otherServicesLayout != null) {
            otherServicesLayout.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Professional Services")
                        .setMessage("Enjoy professional restaurant photography services!\n\n" +
                                "Services include:\n" +
                                "• Professional food photography\n" +
                                "• Restaurant interior photography\n" +
                                "• Event documentation\n\n" +
                                "Contact Information:\n" +
                                "Phone: (02) 2345-6789\n" +
                                "Email: photo@restaurant.service.com")
                        .setPositiveButton("OK", null)
                        .show();
            });
        }

    }
}