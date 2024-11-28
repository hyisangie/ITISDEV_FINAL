package com.itisdev.itisdev_final.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itisdev.itisdev_final.Adapter.ImageSliderAdapter;
import com.itisdev.itisdev_final.Adapter.RestaurantAdapter;
import com.itisdev.itisdev_final.Adapter.ReviewAdapter;
import com.itisdev.itisdev_final.Adapter.VoucherAdapter;
import com.itisdev.itisdev_final.Domain.Review;
import com.itisdev.itisdev_final.Domain.Voucher;
import com.itisdev.itisdev_final.R;
import com.itisdev.itisdev_final.databinding.ActivityRestaurantBinding;

import java.util.ArrayList;
import java.util.List;

public class RestaurantActivity extends BaseActivity {
    private ActivityRestaurantBinding binding;
    private List<Review> reviews;
    private ReviewAdapter reviewAdapter;
    private ImageSliderAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // set Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // set go back action
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        RecyclerView recyclerView = binding.reviewsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupReviewsRecyclerView();
        setupVouchersRecyclerView();
        setupImageViewPager();

        String restaurantId = getIntent().getStringExtra("restaurantId");
        if (restaurantId != null) {
            // initialize Realtime DB
            loadRestaurantData(restaurantId);
            loadReviews(restaurantId);
        } else {
            Log.e("RestaurantActivity", "Invalid restaurantId");
        }
    }

    private void loadReviews(String restaurantId) {
        database.getReference().child("reviews")
                .orderByChild("restaurantId").equalTo(restaurantId)
                .addValueEventListener(new ValueEventListener() {
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
    }

    private void loadRestaurantData(String restaurantId) {
        database.getReference("restaurants").child(restaurantId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve restaurant information
                String name = snapshot.child("name").getValue(String.class);
                String openingHours = snapshot.child("openingHours").getValue(String.class);
                Double rating = snapshot.child("rating").getValue(Double.class);
                String tags = snapshot.child("tags").getValue(String.class);
                String description = snapshot.child("description").getValue(String.class);

                binding.restaurantName.setText(name);
                if (rating != null) {
                    binding.restaurantRating.setVisibility(View.VISIBLE);
                    binding.restaurantRating.setText(String.format("★ %.1f", rating));
                } else {
                    binding.restaurantRating.setVisibility(View.GONE);
                }
                binding.openingHoursTxt.setText("Open: " + openingHours);
                binding.descriptionTxt.setText(description);

                if (tags != null && !tags.isEmpty()) {
                    String[] tagArray = tags.split(",");
                    for (int i = 0; i < tagArray.length; i++) {
                        tagArray[i] = tagArray[i].trim();
                    }
                    addTagsToFlexbox(tagArray);
                }

                // get image list
                DataSnapshot imagesSnapshot = snapshot.child("images");
                List<String> images = new ArrayList<>();
                for (DataSnapshot imageSnapshot : imagesSnapshot.getChildren()) {
                    String imageUrl = imageSnapshot.getValue(String.class);
                    if (imageUrl != null) {
                        images.add(imageUrl);
                    }
                }

                if (!images.isEmpty()) {
                    imageAdapter = new ImageSliderAdapter(RestaurantActivity.this, images);
                    binding.imageViewPager.setAdapter(imageAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
            }
        });

    }

    private void setupImageViewPager() {
        imageAdapter = new ImageSliderAdapter(this, new ArrayList<>());
        binding.imageViewPager.setAdapter(imageAdapter);
    }

    private void setupVouchersRecyclerView() {
        List<Voucher> vouchers = new ArrayList<>();
        VoucherAdapter voucherAdapter = new VoucherAdapter(this, vouchers, VoucherAdapter.TYPE_RESTAURANT, null);
        binding.vouchersRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.vouchersRecycler.setAdapter(voucherAdapter);

        // loading vouchers from current restaurants
        String restaurantId = getIntent().getStringExtra("restaurantId");
        database.getReference("vouchers")
                .orderByChild("restaurantId")
                .equalTo(restaurantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        vouchers.clear();
                        for (DataSnapshot voucherSnapshot : snapshot.getChildren()) {
                            Voucher voucher = voucherSnapshot.getValue(Voucher.class);
                            if (voucher != null) {
                                vouchers.add(voucher);
                            }
                        }
                        voucherAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", error.getMessage());
                    }
                });
    }

    private void setupReviewsRecyclerView() {
        reviews = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviews);
        binding.reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.reviewsRecyclerView.setAdapter(reviewAdapter);
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