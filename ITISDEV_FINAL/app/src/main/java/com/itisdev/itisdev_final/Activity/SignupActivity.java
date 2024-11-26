package com.itisdev.itisdev_final.Activity;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.itisdev.itisdev_final.databinding.ActivitySignupBinding;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends BaseActivity {

    ActivitySignupBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
    }

    private void setVariable() {
        binding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.emailEdt.getText().toString();
                String password = binding.passwordEdt.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // get user ID
                                String uid = task.getResult().getUser().getUid();

                                // create more field
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("role", 1);    // 1 -> user, 2 -> business

                                // save to realtime DB
                                database.getReference("users").child(uid).setValue(userData)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                                Log.e("Firebase", "User additional data saved successfully");
                                            } else {
                                                Log.e("Firebase", "Failed to save additional data", dbTask.getException());
                                            }
                                        });
                            } else {
                                Log.e("Firebase", "User registration failed", task.getException());
                            }
                        });
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });
    }
}