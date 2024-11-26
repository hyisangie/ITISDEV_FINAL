package com.itisdev.itisdev_final.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


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

        RecyclerView recyclerView = binding.restaurantRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // initialize restaurant
        restaurantList = new ArrayList<>();
        restaurantAdapter = new RestaurantAdapter(this, restaurantList);
        recyclerView.setAdapter(restaurantAdapter);

        // initialize Realtime DB
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants");

        // get data Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
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

}