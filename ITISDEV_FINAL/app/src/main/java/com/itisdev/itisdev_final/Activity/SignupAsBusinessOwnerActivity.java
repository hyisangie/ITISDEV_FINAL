package com.itisdev.itisdev_final.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.itisdev.itisdev_final.Domain.Restaurant;
import com.itisdev.itisdev_final.R;
import com.itisdev.itisdev_final.databinding.ActivityRestaurantBinding;
import com.itisdev.itisdev_final.databinding.ActivitySignupAsBusinessOwnerBinding;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class SignupAsBusinessOwnerActivity extends BaseActivity {

    private boolean isEditMode = false;
    private String originalRestaurantId;
    private ArrayList<String> existingImageUrls = new ArrayList<>();
    private ActivitySignupAsBusinessOwnerBinding binding;
    private TextInputEditText restaurantNameEdt, addressEdt, tagsEdt, mobileNumberEdt, descriptionEdt;
    private AutoCompleteTextView cuisineTypeDropdown;
    private MaterialButton addImagesBtn, openTimeBtn, closeTimeBtn, submitBtn;
    private String openingTime = "", closingTime = "";
    private ArrayList<Uri> selectedImages = new ArrayList<>();
    private static final int PICK_IMAGES_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupAsBusinessOwnerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();

        // Check if in edit mode
        if (getIntent().hasExtra("restaurantId")) {
            isEditMode = true;
            originalRestaurantId = getIntent().getStringExtra("restaurantId");
            loadExistingData();

            // Disable Restaurant Name editing
            restaurantNameEdt.setEnabled(false);
        }

        setupCuisineTypeDropdown();
        setupTimeButtons();
        setupImagePicker();
        setupSubmitButton();
    }

    private void loadExistingData() {

        DatabaseReference restaurantRef = database.getReference("restaurants").child(originalRestaurantId);
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Restaurant restaurant = snapshot.getValue(Restaurant.class);
                if (restaurant != null) {
                    // Fill in existing data
                    restaurantNameEdt.setText(restaurant.getName());
                    addressEdt.setText(restaurant.getAddress());
                    cuisineTypeDropdown.setText(restaurant.getCuisineType());
                    tagsEdt.setText(restaurant.getTags());
                    mobileNumberEdt.setText(restaurant.getContactDetails());
                    descriptionEdt.setText(restaurant.getDescription());

                    // Handle opening hours
                    String[] hours = restaurant.getOpeningHours().split(" - ");
                    if (hours.length == 2) {
                        openingTime = hours[0];
                        closingTime = hours[1];
                        openTimeBtn.setText(openingTime);
                        closeTimeBtn.setText(closingTime);
                    }

                    // Handle existing images
                    existingImageUrls = restaurant.getImages();
                    if (existingImageUrls != null && !existingImageUrls.isEmpty()) {
                        addImagesBtn.setText("Selected " + existingImageUrls.size() + "images");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignupAsBusinessOwnerActivity.this,
                        "Failed to load restaurant data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {

            if (isEditMode) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        selectedImages.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    selectedImages.add(imageUri);
                }
            } else {
                selectedImages.clear();

                if (data.getClipData() != null) {
                    // multiple images
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        selectedImages.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    // single image
                    Uri imageUri = data.getData();
                    selectedImages.add(imageUri);
                }

            }

            int totalImages = selectedImages.size() + existingImageUrls.size();
            addImagesBtn.setText("Seleted " + selectedImages.size() + " images");
        }
    }

    private void setupSubmitButton() {
        submitBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading");
                progressDialog.show();

                if (selectedImages.isEmpty()) {
                    saveRestaurantInfo(new ArrayList<>(), progressDialog);
                } else {
                    uploadImages(progressDialog);
                }
            }
        });
    }

    private void uploadImages(ProgressDialog progressDialog) {
        ArrayList<String> imageUrls = new ArrayList<>();
        AtomicInteger uploadCount = new AtomicInteger(0);

        for (Uri imageUri : selectedImages) {
            String fileName = UUID.randomUUID().toString();
            StorageReference imageRef = storage
                    .getReference("restaurantImages")
                    .child(fileName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            imageUrls.add(uri.toString());

                            if (uploadCount.incrementAndGet() == selectedImages.size()) {
                                saveRestaurantInfo(imageUrls, progressDialog);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(SignupAsBusinessOwnerActivity.this,
                                "Failed to upload images", Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void saveRestaurantInfo(ArrayList<String> newImageUrls, ProgressDialog progressDialog) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantNameEdt.getText().toString());
        restaurant.setAddress(addressEdt.getText().toString());
        restaurant.setCuisineType(cuisineTypeDropdown.getText().toString());
        restaurant.setTags(tagsEdt.getText().toString());

        // combine old and new images
        ArrayList<String> allImages = new ArrayList<>(existingImageUrls);
        allImages.addAll(newImageUrls);
        restaurant.setImages(allImages);

        restaurant.setOpeningHours(openingTime + " - " + closingTime);
        restaurant.setContactDetails(mobileNumberEdt.getText().toString());
        restaurant.setDescription(descriptionEdt.getText().toString());

        // save to firebase
        DatabaseReference restaurantRef;
        if (isEditMode) {
            restaurant.setId(originalRestaurantId);
            restaurantRef = database.getReference("restaurants").child(originalRestaurantId);
        } else {
            restaurantRef = database.getReference("restaurants").push();
            restaurant.setId(restaurantRef.getKey());
        }


        restaurantRef.setValue(restaurant)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(SignupAsBusinessOwnerActivity.this, isEditMode ?
                            "Restaurant information updated successfully" : "Restaurant information saved successfully",
                            Toast.LENGTH_LONG).show();

                    if (isEditMode) {
                        finish();
                    } else {
                        Intent intent = new Intent(SignupAsBusinessOwnerActivity.this, RestaurantProfileActivity.class);
                        intent.putExtra("restaurantName", restaurant.getName());
                        intent.putExtra("restaurantId", restaurant.getId());
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(SignupAsBusinessOwnerActivity.this,
                            "Failed to save restaurant information", Toast.LENGTH_LONG).show();
                });
    }



    private boolean validateInputs() {
        if (TextUtils.isEmpty(restaurantNameEdt.getText())) {
            restaurantNameEdt.setError("Restaurant name is required");
            return false;
        }

        if (TextUtils.isEmpty(addressEdt.getText())) {
            addressEdt.setError("Address is required");
            return false;
        }

        if (TextUtils.isEmpty(cuisineTypeDropdown.getText())) {
            cuisineTypeDropdown.setError("Cuisine type is required");
            return false;
        }

        if (TextUtils.isEmpty(mobileNumberEdt.getText())) {
            mobileNumberEdt.setError("Mobile number is required");
            return false;
        }

        if (openingTime.isEmpty() || closingTime.isEmpty()) {
            Toast.makeText(this, "Please select opening and closing time",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (selectedImages.isEmpty() && existingImageUrls.isEmpty()) {
            Toast.makeText(this, "Please select at least one image",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void setupImagePicker() {
        addImagesBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGES_REQUEST);
        });
    }

    private void setupTimeButtons() {
        View.OnClickListener timePickerListener = v -> {
            final boolean isOpenTime = v.getId() == R.id.openTimeBtn;

            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText(isOpenTime ? "Select Opening Time" : "Select Closing Time")
                    .build();

            timePicker.addOnPositiveButtonClickListener(dialog -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d",
                        timePicker.getHour(), timePicker.getMinute());
                if (isOpenTime) {
                    openingTime = time;
                    openTimeBtn.setText(time);
                } else {
                    closingTime = time;
                    closeTimeBtn.setText(time);
                }
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        };

        openTimeBtn.setOnClickListener(timePickerListener);
        closeTimeBtn.setOnClickListener(timePickerListener);
    }

    private void initViews() {
        // Initialize EditTexts
        restaurantNameEdt = binding.restaurantNameEdt;
        addressEdt = binding.addressEdt;
        tagsEdt = binding.tagsEdt;
        mobileNumberEdt = binding.mobileNumberEdt;
        descriptionEdt = binding.descriptionEdt;

        // Initialize dropdown
        cuisineTypeDropdown = binding.cuisineTypeDropdown;

        // Initialize buttons
        addImagesBtn = binding.addImagesBtn;
        openTimeBtn = binding.openTimeBtn;
        closeTimeBtn = binding.closeTimeBtn;
        submitBtn = binding.submitBtn;
    }

    private void setupCuisineTypeDropdown() {
        DatabaseReference cuisineRef = database.getReference("cuisineRef");
        cuisineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> cuisineTypes = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String cuisineType = dataSnapshot.child("type").getValue(String.class);
                    if (cuisineType != null) {
                        cuisineTypes.add(cuisineType);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        SignupAsBusinessOwnerActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        cuisineTypes
                );
                cuisineTypeDropdown.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignupAsBusinessOwnerActivity.this,
                        "Failed to load cuisine types", Toast.LENGTH_SHORT).show();
            }
        });
    }


}