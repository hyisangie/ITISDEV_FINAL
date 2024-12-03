package com.itisdev.itisdev_final.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itisdev.itisdev_final.Adapter.ImageSliderAdapter;
import com.itisdev.itisdev_final.Adapter.ReviewAdapter;
import com.itisdev.itisdev_final.Adapter.VoucherAdapter;
import com.itisdev.itisdev_final.Domain.Restaurant;
import com.itisdev.itisdev_final.Domain.Review;
import com.itisdev.itisdev_final.Domain.Voucher;
import com.itisdev.itisdev_final.R;
import com.itisdev.itisdev_final.databinding.ActivityRestaurantBinding;

import java.util.ArrayList;
import java.util.List;

public class RestaurantActivity extends AppCompatActivity {
    private ActivityRestaurantBinding binding;
    private String restaurantId;
    private DatabaseReference databaseReference;
    private VoucherAdapter voucherAdapter;
    private ReviewAdapter reviewAdapter;
    private List<Voucher> voucherList;
    private List<Review> reviewList;
    private String currentUserId;
    private ImageSliderAdapter imageSliderAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get restaurant ID from intent
        restaurantId = getIntent().getStringExtra("restaurantId");

        // Initialize UI components
        initializeToolbar();
        initializeImageSlider();
        initializeVoucherRecyclerView();
        initializeReviewRecyclerView();

        // Load data
        loadRestaurantDetails();
        loadVouchers();
        loadReviews();
    }

    private void initializeToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeImageSlider() {
        List<String> emptyList = new ArrayList<>();
        imageSliderAdapter = new ImageSliderAdapter(this, emptyList);
        binding.imageViewPager.setAdapter(imageSliderAdapter);
    }

    private void initializeVoucherRecyclerView() {
        voucherList = new ArrayList<>();
        voucherAdapter = new VoucherAdapter(this, voucherList, VoucherAdapter.TYPE_RESTAURANT, currentUserId, null);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.vouchersRecycler.setLayoutManager(layoutManager);
        binding.vouchersRecycler.setAdapter(voucherAdapter);
    }


    private void initializeReviewRecyclerView() {
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);

        binding.reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    private void loadRestaurantDetails() {
        databaseReference.child("restaurants").child(restaurantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Restaurant restaurant = snapshot.getValue(Restaurant.class);
                        if (restaurant != null) {
                            updateUI(restaurant);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RestaurantActivity.this,
                                "Failed to load restaurant details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(Restaurant restaurant) {
        binding.restaurantName.setText(restaurant.getName());
        binding.openingHoursTxt.setText(restaurant.getOpeningHours());
        binding.descriptionTxt.setText(restaurant.getDescription());

        // Update rating - Handle null check
        Float rating = restaurant.getRating();
        if (rating != null && rating > 0) {
            binding.restaurantRating.setText(String.format("%.1f", rating));
        } else {
            binding.restaurantRating.setText("New");
        }

        // Update tags/features
        binding.tagsContainer.removeAllViews();
        if (restaurant.getTags() != null && !restaurant.getTags().isEmpty()) {
            String[] tagArray = restaurant.getTags().split(",");
            for (String tag : tagArray) {
                Chip chip = new Chip(this);
                chip.setText(tag.trim());
                binding.tagsContainer.addView(chip);
            }
        }

        // Update image slider
        if (restaurant.getImages() != null && !restaurant.getImages().isEmpty()) {
            imageSliderAdapter.setImages(restaurant.getImages());
        }
    }

    private void loadVouchers() {
        databaseReference.child("vouchers").orderByChild("restaurantId").equalTo(restaurantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        voucherList.clear();
                        for (DataSnapshot voucherSnapshot : snapshot.getChildren()) {
                            Voucher voucher = voucherSnapshot.getValue(Voucher.class);
                            if (voucher != null && voucher.isActive()) {
                                checkIfVoucherClaimed(voucher);
                            }
                        }

                        // Show/hide vouchers section based on availability
                        if (voucherList.isEmpty()) {
                            binding.vouchersRecycler.setVisibility(View.GONE);
                        } else {
                            binding.vouchersRecycler.setVisibility(View.VISIBLE);
                            voucherAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RestaurantActivity.this,
                                "Failed to load vouchers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfVoucherClaimed(Voucher voucher) {
        databaseReference.child("claimedVouchers")
                .orderByChild("voucherId").equalTo(voucher.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean alreadyClaimed = false;
                        for (DataSnapshot claimedVoucherSnapshot : snapshot.getChildren()) {
                            String userId = claimedVoucherSnapshot.child("userId").getValue(String.class);
                            if (currentUserId.equals(userId)) {
                                alreadyClaimed = true;
                                break;
                            }
                        }

                        // If not claimed, add to voucherList
                        if (!alreadyClaimed) {
                            voucherList.add(voucher);
                        }

                        // Update UI based on vouchers availability
                        if (voucherList.isEmpty()) {
                            binding.vouchersRecycler.setVisibility(View.GONE);
                        } else {
                            binding.vouchersRecycler.setVisibility(View.VISIBLE);
                            voucherAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RestaurantActivity.this,
                                "Failed to check claimed vouchers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadReviews() {
        databaseReference.child("reviews").orderByChild("restaurantId").equalTo(restaurantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviewList.clear();
                        for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                            Review review = reviewSnapshot.getValue(Review.class);
                            if (review != null) {
                                reviewList.add(review);
                            }
                        }
                        reviewAdapter.notifyDataSetChanged();

                        // Show/hide reviews section based on availability
                        if (reviewList.isEmpty()) {
                            binding.reviewsRecyclerView.setVisibility(View.GONE);
                        } else {
                            binding.reviewsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RestaurantActivity.this,
                                "Failed to load reviews", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}