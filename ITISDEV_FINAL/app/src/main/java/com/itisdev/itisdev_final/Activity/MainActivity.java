package com.itisdev.itisdev_final.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.itisdev.itisdev_final.R;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itisdev.itisdev_final.Adapter.RestaurantAdapter;
import com.itisdev.itisdev_final.Domain.Restaurant;
import com.itisdev.itisdev_final.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private RestaurantAdapter restaurantAdapter;
    private List<Restaurant> restaurantList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
        setupRecyclerView();
        setupSearch();
        loadRestaurant();
    }

    private void loadRestaurant() {
        database.getReference("restaurants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                restaurantList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Restaurant restaurant = dataSnapshot.getValue(Restaurant.class);
                    if (restaurant != null) {
                        restaurantList.add(restaurant);
                        Log.d("Restaurant", "Loaded: " + restaurant.getName());
                    }
                }
                // notify
                restaurantAdapter.notifyDataSetChanged();
                Log.d("Restaurant", "Total restaurants loaded: " + restaurantList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupRecyclerView() {
        binding.restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        restaurantList = new ArrayList<>();
        restaurantAdapter = new RestaurantAdapter(this, restaurantList);
        binding.restaurantRecyclerView.setAdapter(restaurantAdapter);
    }

    private void setupSearch() {
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterRestaurants(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRestaurants(newText);
                return false;
            }
        });
    }

    private void filterRestaurants(String query) {
        List<Restaurant> filteredList = new ArrayList<>();
        for(Restaurant restaurant : restaurantList) {
            if(restaurant.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(restaurant);
            }
        }
        restaurantAdapter = new RestaurantAdapter(this, filteredList);
        binding.restaurantRecyclerView.setAdapter(restaurantAdapter);
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
                this instanceof MainActivity ? R.id.nav_home : R.id.nav_profile
        );
    }


}