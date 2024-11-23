package com.itisdev.itisdev_final.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itisdev.itisdev_final.Adapter.BestRestaurantAdapter;
import com.itisdev.itisdev_final.Domain.Location;
import com.itisdev.itisdev_final.Domain.PriceRange;
import com.itisdev.itisdev_final.Domain.RatingRange;
import com.itisdev.itisdev_final.Domain.Restaurant;
import com.itisdev.itisdev_final.R;
import com.itisdev.itisdev_final.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initLocation();
        initRatingRange();
        initPriceRange();

        initBestRestaurant();
    }

    private void initBestRestaurant() {
        DatabaseReference myRef = database.getReference("Restaurant");
        binding.pbBestRestaurant.setVisibility(View.VISIBLE);
        ArrayList<Restaurant> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Log.d("BestRestaurant", "Data snapshot exists: " + snapshot.exists());
                    if (snapshot.exists()) {
                        for (DataSnapshot restaurantSnapshot : snapshot.getChildren()) {
                            try {
                                // 获取所有字段
                                String name = restaurantSnapshot.child("name").getValue(String.class);
                                Boolean bestRestaurant = restaurantSnapshot.child("bestRestaurant").getValue(Boolean.class);
                                Long categoryId = restaurantSnapshot.child("categoryId").getValue(Long.class);
                                String description = restaurantSnapshot.child("description").getValue(String.class);
                                Long locationId = restaurantSnapshot.child("locationId").getValue(Long.class);
                                String address = restaurantSnapshot.child("address").getValue(String.class);
                                Double rating = restaurantSnapshot.child("rating").getValue(Double.class);
                                Long priceRangeId = restaurantSnapshot.child("priceRangeId").getValue(Long.class);
                                String openingHour = restaurantSnapshot.child("openingHour").getValue(String.class);
                                String phoneNumber = restaurantSnapshot.child("phoneNumber").getValue(String.class);
                                List<String> photos = (List<String>) restaurantSnapshot.child("photos").getValue();

                                // 创建 Restaurant 对象
                                Restaurant restaurant = new Restaurant();
                                restaurant.setName(name);
                                restaurant.setBestRestaurant(bestRestaurant != null && bestRestaurant);
                                restaurant.setCategoryId(categoryId != null ? categoryId.intValue() : 0);
                                restaurant.setDescription(description);
                                restaurant.setLocationId(locationId != null ? locationId.intValue() : 0);
                                restaurant.setAddress(address);
                                restaurant.setRating(rating != null ? rating : 0.0);
                                restaurant.setPriceRangeId(priceRangeId != null ? priceRangeId.intValue() : 0);
                                restaurant.setOpeningHour(openingHour);
                                restaurant.setPhoneNumber(phoneNumber);
                                restaurant.setPhotos(photos);

                                if (restaurant.getBestRestaurant()) {
                                    list.add(restaurant);
                                    Log.d("BestRestaurant", "Added restaurant: " + restaurant.getName());
                                }
                            } catch (Exception e) {
                                Log.e("BestRestaurant", "Error parsing restaurant: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }

                        if (!list.isEmpty()) {
                            binding.bestRestaurantView.setLayoutManager(
                                    new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false)
                            );
                            RecyclerView.Adapter adapter = new BestRestaurantAdapter(list);
                            binding.bestRestaurantView.setAdapter(adapter);
                            Log.d("BestRestaurant", "Adapter set with " + list.size() + " restaurants");
                        }
                    }
                } catch (Exception e) {
                    Log.e("BestRestaurant", "Error parsing data: " + e.getMessage());
                    e.printStackTrace();
                }
                binding.pbBestRestaurant.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("BestRestaurant", "Database error: " + error.getMessage());
                binding.pbBestRestaurant.setVisibility(View.GONE);
            }
        });
    }
    private void initLocation() {

        DatabaseReference myRef = database.getReference("Location");
        ArrayList<Location> items = new ArrayList<>();


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Location item = issue.getValue(Location.class);
                        if (item != null) {
                            items.add(item);
                        }

                    }

                    if (items.size() > 0) {
                        ArrayAdapter<Location> adapter = new ArrayAdapter<>(
                                MainActivity.this,
                                android.R.layout.simple_spinner_item,
                                items);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.locationSp.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void initPriceRange() {
        DatabaseReference myRef = database.getReference("PriceRange");
        ArrayList<PriceRange> items = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        PriceRange item = issue.getValue(PriceRange.class);
                        if (item != null) {
                            items.add(item);
                        }

                    }

                    if (items.size() > 0) {
                        ArrayAdapter<PriceRange> adapter = new ArrayAdapter<>(
                                MainActivity.this,
                                android.R.layout.simple_spinner_item,
                                items);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.priceSp.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void initRatingRange() {
        DatabaseReference myRef = database.getReference("RatingRange");
        ArrayList<RatingRange> items = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        RatingRange item = issue.getValue(RatingRange.class);
                        if (item != null) {
                            items.add(item);
                        }

                    }

                    if (items.size() > 0) {
                        ArrayAdapter<RatingRange> adapter = new ArrayAdapter<>(
                                MainActivity.this,
                                android.R.layout.simple_spinner_item,
                                items);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.ratingSp.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}