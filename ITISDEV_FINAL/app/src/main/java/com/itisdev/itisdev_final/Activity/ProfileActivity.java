package com.itisdev.itisdev_final.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itisdev.itisdev_final.Adapter.ReviewAdapter;
import com.itisdev.itisdev_final.Domain.Review;
import com.itisdev.itisdev_final.R;
import com.itisdev.itisdev_final.databinding.ActivityProfileBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ProfileActivity extends BaseActivity {

    private ActivityProfileBinding binding;
    private List<Review> reviews;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // initialize User Info
            String userId = user.getUid();
            String email = user.getEmail();
            binding.profileName.setText(email);

            // get from realtime DB
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String bio = snapshot.child("description").getValue(String.class);
                        binding.profileBio.setText(bio);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            // initialize vouchers
            loadVouchers(userId);

            // initialize reviews
            RecyclerView recyclerView = binding.reviewRecyclerView;
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            reviews = new ArrayList<>();
            reviewAdapter = new ReviewAdapter(this, reviews);
            recyclerView.setAdapter(reviewAdapter);
            Query query = database.getReference("reviews").orderByChild("userId").equalTo(userId);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    reviews.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Review review = dataSnapshot.getValue(Review.class);
                        if (review != null) {
                            reviews.add(review);
                            Log.d("Review", "Loaded: " + review.getDescription());
                        }
                    }
                    // notify
                    reviewAdapter.notifyDataSetChanged();
                    Log.d("Review", "Total reviews loaded: " + reviews.size());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to fetch data", Toast.LENGTH_LONG).show();
                }
            });


        } else {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        }

    }

    private void loadVouchers(String userId) {
        DatabaseReference voucherRef = database.getReference("claimedVouchers");
        Query query = voucherRef.orderByChild("userId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LinearLayout voucherContainer = findViewById(R.id.voucher_container);
                voucherContainer.removeAllViews();

                // iterate all vouchers owned by the user
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String voucherId = dataSnapshot.child("voucherId").getValue(String.class);
                    if (voucherId != null) {
                        Log.d("Profile", "voucher id: " + voucherId);
                        Query voucherquery = database.getReference("vouchers").orderByChild("id").equalTo(voucherId);
                        voucherquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot voucherSnapshot) {
                                if (voucherSnapshot.exists()) {
                                    createVoucherCard(voucherSnapshot, voucherContainer);
                                } else {
                                    Log.e("Voucher", "Voucher ID " + voucherId + " not found.");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("Voucher", "Failed to load voucher details for ID " + voucherId, error.toException());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createVoucherCard(DataSnapshot voucherSnapshot, LinearLayout voucherContainer) {
        // 遍历 voucherSnapshot 的所有子节点
        for (DataSnapshot childSnapshot : voucherSnapshot.getChildren()) {
            LinearLayout voucherCard = new LinearLayout(ProfileActivity.this);
            voucherCard.setOrientation(LinearLayout.VERTICAL);
            voucherCard.setPadding(32, 16, 16, 16);
            voucherCard.setBackgroundResource(R.drawable.card_background);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    600,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            voucherCard.setLayoutParams(params);

            // 获取动态子节点的值
            Integer amount = childSnapshot.child("amount").getValue(Integer.class);
            Integer minSpending = childSnapshot.child("minSpend").getValue(Integer.class);
            Integer type = childSnapshot.child("type").getValue(Integer.class);
            String restaurantId = childSnapshot.child("restaurantId").getValue(String.class);

            Log.d("Profile", "amount: " + amount + " minSpend: " + minSpending + " type: " + type);

            TextView restaurantText = new TextView(ProfileActivity.this);

            // 查询 restaurantId 获取餐厅名称
            DatabaseReference restaurantRef = FirebaseDatabase.getInstance().getReference("restaurants");
            Query query = restaurantRef.orderByChild("id").equalTo(restaurantId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String restaurantName = null;
                        for (DataSnapshot restaurantSnapshot : snapshot.getChildren()) {
                            restaurantName = restaurantSnapshot.child("name").getValue(String.class);
                            break;
                        }
                        restaurantText.setText("Restaurant: " + (restaurantName != null ? restaurantName : "N/A"));
                    } else {
                        restaurantText.setText("Unknown");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Failed to fetch restaurant name", error.toException());
                }
            });

            restaurantText.setTextSize(14);
            restaurantText.setTypeface(null, Typeface.BOLD);

            TextView discountText = new TextView(ProfileActivity.this);
            if (type != null && type == 1) {
                discountText.setText("₱" + amount + " off");
            } else if (type != null && type == 2) {
                discountText.setText("Up to ₱" + amount + " discount");
            } else {
                discountText.setText("Discount type unknown");
            }
            discountText.setTextSize(14);

            TextView conditionText = new TextView(ProfileActivity.this);
            if (type != null && type == 1) {
                conditionText.setText("Min. Spend ₱" + minSpending);
            } else if (type != null && type == 2) {
                conditionText.setText("Share to friends");
            }
            conditionText.setTextSize(14);

            // 将文本添加到卡片
            voucherCard.addView(restaurantText);
            voucherCard.addView(discountText);
            voucherCard.addView(conditionText);

            // 将卡片添加到容器
            voucherContainer.addView(voucherCard);
        }
    }


}