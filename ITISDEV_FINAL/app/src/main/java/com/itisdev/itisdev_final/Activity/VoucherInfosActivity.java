package com.itisdev.itisdev_final.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.itisdev.itisdev_final.R;
import com.itisdev.itisdev_final.databinding.ActivityRestaurantProfileBinding;
import com.itisdev.itisdev_final.databinding.ActivityVoucherInfosBinding;

public class VoucherInfosActivity extends AppCompatActivity {
    private ActivityVoucherInfosBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVoucherInfosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}