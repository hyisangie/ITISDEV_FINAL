package com.itisdev.itisdev_final.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.storage.StorageReference;
import com.itisdev.itisdev_final.databinding.ActivitySignupBinding;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends BaseActivity {

    ActivitySignupBinding binding;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setVariable();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            binding.userImageView.setImageURI(selectedImage);
            binding.addImageBtn.setText("Change Image");
        }
    }

    private void setVariable() {

        // Toggle image upload visibility based on checkbox
        binding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.imageUploadLayout.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        // Image upload button
        binding.addImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        binding.signupBtn.setOnClickListener(v -> {
            String email = binding.emailEdt.getText().toString();
            String password = binding.passwordEdt.getText().toString();
            boolean isBusinessOwner = binding.checkBox.isChecked();

            if (!isBusinessOwner && selectedImage == null) {
                Toast.makeText(SignupActivity.this, "Please select a profile image", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // get user ID
                            String uid = task.getResult().getUser().getUid();

                            if (isBusinessOwner) {
                                handleBusinessOwnerSignup(uid, email);
                            } else {
                                handleRegularUserSignup(uid, email);
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();                        }
                    });

        });

        binding.loginBtn.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }

    private void handleBusinessOwnerSignup(String uid, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("role", "owner");
        userData.put("email", email);

        database.getReference("users").child(uid).setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(SignupActivity.this, SignupAsBusinessOwnerActivity.class);
                        intent.putExtra("uid", uid);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignupActivity.this, "Failed to save user data",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleRegularUserSignup(String uid, String email) {
        StorageReference imageRef = storage.getReference("userImages").child(uid);

        imageRef.putFile(selectedImage)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("role", "user");
                        userData.put("email", email);
                        userData.put("profileImage", uri.toString());

                        database.getReference("users").child(uid).setValue(userData)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(SignupActivity.this, "Failed to save user data",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(SignupActivity.this,
                        "Failed to upload image", Toast.LENGTH_SHORT).show());
    }
}