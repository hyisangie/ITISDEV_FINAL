package com.itisdev.itisdev_final.Activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.itisdev.itisdev_final.Adapter.ReviewAdapter;
import com.itisdev.itisdev_final.Adapter.VoucherAdapter;
import com.itisdev.itisdev_final.Domain.Review;
import com.itisdev.itisdev_final.Domain.Voucher;
import com.itisdev.itisdev_final.Domain.VoucherShareListener;
import com.itisdev.itisdev_final.R;
import com.itisdev.itisdev_final.databinding.ActivityProfileBinding;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileActivity extends BaseActivity implements VoucherShareListener {

    private ActivityProfileBinding binding;
    private List<Review> reviews;
    private List<Voucher> vouchers;
    private ReviewAdapter reviewAdapter;
    private VoucherAdapter voucherAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            setupRecyclerViews(user.getUid());
            loadUserData(user);
            loadVouchers(user.getUid());
            loadReviews(user.getUid());

        } else {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        }

    }

    private void loadReviews(String userId) {
        database.getReference("reviews")
                .orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviews.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Review review = dataSnapshot.getValue(Review.class);
                            if (review != null) {
                                reviews.add(review);
                            }
                        }
                        reviewAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, "Failed to fetch reviews",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void loadVouchers(String userId) {
        DatabaseReference voucherRef = database.getReference("claimedVouchers");

        // create a Map to store restaurant name cache
        Map<String, String> restaurantNames = new HashMap<>();
        voucherRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> voucherIds = new ArrayList<>();
                        // collect unused voucher IDs
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Boolean isUsed = dataSnapshot.child("used").getValue(Boolean.class);
                            if (isUsed != null && !isUsed) {
                                String voucherId = dataSnapshot.child("voucherId").getValue(String.class);
                                if (voucherId != null) {
                                    voucherIds.add(voucherId);
                                }
                            }
                        }

                        // batch getting voucher details
                        database.getReference("vouchers")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        vouchers.clear();
                                        Set<String> restaurantIds = new HashSet<>();

                                        // collect restaurant ID
                                        for (DataSnapshot voucherSnapshot : snapshot.getChildren()) {
                                            String id = voucherSnapshot.child("id").getValue(String.class);
                                            if (voucherIds.contains(id)) {
                                                Voucher voucher = voucherSnapshot.getValue(Voucher.class);
                                                if (voucher != null) {
                                                    vouchers.add(voucher);
                                                    restaurantIds.add(voucher.getRestaurantId());
                                                }
                                            }
                                        }

                                        // collect restaurant info
                                        database.getReference("restaurants")
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot restaurantSnapshot : snapshot.getChildren()) {
                                                            String restaurantId = restaurantSnapshot.child("id").getValue(String.class);
                                                            if (restaurantIds.contains(restaurantId)) {
                                                                String name = restaurantSnapshot.child("name").getValue(String.class);
                                                                restaurantNames.put(restaurantId, name);
                                                                Log.d("RestaurantDebug", "All restaurants: " + restaurantNames.toString());
                                                            }
                                                        }

                                                        // update adapter
                                                        voucherAdapter.setRestaurantNames(restaurantNames);
                                                        voucherAdapter.notifyDataSetChanged();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.e("Profile", "Failed to load restaurant names", error.toException());
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("Profile", "Failed to load vouchers", error.toException());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Profile", "Failed to load claimed vouchers", error.toException());
                    }

                });
    }

    private void loadVoucherDetails(String voucherId) {
        database.getReference("vouchers").orderByChild("id").equalTo(voucherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot voucherSnapshot : snapshot.getChildren()) {
                            Voucher voucher = voucherSnapshot.getValue(Voucher.class);
                            if (voucher != null) {
                                loadRestaurantName(voucher);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Profile", "Failed to load voucher details", error.toException());
                    }
                });
    }


    public void onShareVoucher(String voucherId) {
        String voucherLink = "https://itisdev-final-default-rtdb.firebaseio.com/vouchers/" + voucherId;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_share_voucher, null);
        builder.setView(dialogView);

        // TextView and Button
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView messageTextView = dialogView.findViewById(R.id.voucher_message);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button copyButton = dialogView.findViewById(R.id.copy_button);

        // voucher link in the message TextView
        messageTextView.setText("Voucher Link:\n" + voucherLink);

        // "Copy to Clipboard"
        copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Voucher Link", voucherLink);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }




    private void loadRestaurantName(Voucher voucher) {
        database.getReference("restaurants")
                .orderByChild("id").equalTo(voucher.getRestaurantId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot restaurantSnapshot : snapshot.getChildren()) {
                            String restaurantName = restaurantSnapshot.child("name").getValue(String.class);
                            vouchers.add(voucher);
                            voucherAdapter.notifyDataSetChanged();
                            break;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Profile", "Failed to load restaurant name", error.toException());
                    }
                });
    }

    private void loadUserData(FirebaseUser user) {
        binding.profileName.setText(user.getEmail());
        database.getReference("users").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String bio = snapshot.child("description").getValue(String.class);
                            String profileImage = snapshot.child("profileImage").getValue(String.class);
                            binding.profileBio.setText(bio);

                            if (profileImage != null && !profileImage.isEmpty()) {
                                Glide.with(ProfileActivity.this)
                                        .load(profileImage)
                                        .placeholder(R.drawable.baseline_person_24)
                                        .into(binding.profileImage);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Profile", "Failed to load user data", error.toException());
                    }
                });
    }

    private void setupRecyclerViews(String userId) {

        // Reviews RecyclerView
        reviews = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviews);
        binding.reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.reviewRecyclerView.setAdapter(reviewAdapter);

        // Vouchers RecyclerView
        vouchers = new ArrayList<>();
        voucherAdapter = new VoucherAdapter(this, vouchers, 1, userId, this);
        binding.voucherScrollView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.voucherScrollView.setAdapter(voucherAdapter);

    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = binding.bottomNavigation;
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                if (!MainActivity.class.isInstance(this)) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                if (!ProfileActivity.class.isInstance(this)) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    finish();
                }
                return true;
            }
            return false;
        });

        bottomNav.setSelectedItemId(
                MainActivity.class.isInstance(this) ? R.id.nav_home : R.id.nav_profile
        );
    }

}