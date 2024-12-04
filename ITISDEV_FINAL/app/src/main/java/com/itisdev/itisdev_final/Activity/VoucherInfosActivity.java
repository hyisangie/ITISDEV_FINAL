package com.itisdev.itisdev_final.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.itisdev.itisdev_final.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;


import com.itisdev.itisdev_final.Adapter.ClaimedVoucherAdapter;
import com.itisdev.itisdev_final.Adapter.VoucherAdapter;
import com.itisdev.itisdev_final.Domain.Voucher;
import com.itisdev.itisdev_final.Domain.ClaimedVoucher;
import com.itisdev.itisdev_final.databinding.ActivityVoucherInfosBinding;
import com.itisdev.itisdev_final.Domain.VoucherShareListener;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VoucherInfosActivity extends AppCompatActivity implements VoucherShareListener{
    private ActivityVoucherInfosBinding binding;
    private DatabaseReference vouchersRef, claimedVouchersRef;
    private String restaurantId;
    private VoucherAdapter voucherAdapter;
    private List<Voucher> voucherList;
    private List<ClaimedVoucher> claimedVoucherList;
    private List<ClaimedVoucher> filteredClaimedVoucherList;
    private ClaimedVoucherAdapter claimedVoucherAdapter;

    private String generateVoucherLink(String voucherId) {
        return "https://itisdev-final-default-rtdb.firebaseio.com/vouchers/" + voucherId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVoucherInfosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFirebase();
        initializeViews();
        setupListeners();
    }

    private void initializeFirebase() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    restaurantId = snapshot.child("restaurantId").getValue(String.class);
                    if (restaurantId != null) {
                        Log.d("RestaurantDebug", "User's Restaurant ID: " + restaurantId);
//                        loadData();
                    } else {
                        Log.e("RestaurantDebug", "No restaurantId found for this user.");
                    }
                } else {
                    Log.e("RestaurantDebug", "User does not exist.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RestaurantDebug", "Failed to fetch user data", error.toException());
            }
        });
        vouchersRef = FirebaseDatabase.getInstance().getReference("vouchers");
        claimedVouchersRef = FirebaseDatabase.getInstance().getReference("claimedVouchers");
    }

    private void initializeViews() {
        // Initialize Available Vouchers RecyclerView
        voucherList = new ArrayList<>();
        voucherAdapter = new VoucherAdapter(this, voucherList, VoucherAdapter.TYPE_INFO, restaurantId, this);


        binding.voucherScrollView.setLayoutManager(new LinearLayoutManager(this));
        binding.voucherScrollView.setAdapter(voucherAdapter);
        Log.d("VoucherInfosActivity", "RecyclerView adapter set. Adapter item count: " + voucherAdapter.getItemCount());

        // Initialize Claimed Vouchers RecyclerView
        claimedVoucherList = new ArrayList<>();
        filteredClaimedVoucherList = new ArrayList<>();
        claimedVoucherAdapter = new ClaimedVoucherAdapter(this, filteredClaimedVoucherList);
        binding.claimedVouchersList.setLayoutManager(new LinearLayoutManager(this));
        binding.claimedVouchersList.setAdapter(claimedVoucherAdapter);

        // Initially hide claimed vouchers section
        binding.claimedVouchersSection.setVisibility(View.GONE);
        binding.availableVouchersSection.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        // Back button
        binding.backButton.setOnClickListener(v -> finish());

        // Tab selection
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    binding.availableVouchersSection.setVisibility(View.VISIBLE);
                    binding.claimedVouchersSection.setVisibility(View.GONE);
                } else {
                    binding.availableVouchersSection.setVisibility(View.GONE);
                    binding.claimedVouchersSection.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Search functionality
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClaimedVouchers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add voucher button
        binding.addVoucherButton.setOnClickListener(v -> showAddVoucherDialog());

        // Use voucher button
        binding.useButton.setOnClickListener(v -> {
            String voucherCode = binding.voucherInput.getText().toString().trim();
            if (!voucherCode.isEmpty()) {
                checkAndUseVoucher(voucherCode);
            } else {
                Toast.makeText(this, "Please enter a voucher code", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddVoucherDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_voucher);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextInputEditText amountInput = dialog.findViewById(R.id.amount_input);
        TextInputEditText minSpendInput = dialog.findViewById(R.id.min_spend_input);
        RadioGroup typeGroup = dialog.findViewById(R.id.voucher_type_group);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancel_button);
        MaterialButton addButton = dialog.findViewById(R.id.add_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        addButton.setOnClickListener(v -> {
            String amountStr = amountInput.getText().toString();
            String minSpendStr = minSpendInput.getText().toString();

            if (amountStr.isEmpty() || minSpendStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int amount = Integer.parseInt(amountStr);
            int minSpend = Integer.parseInt(minSpendStr);
            int type = typeGroup.getCheckedRadioButtonId() == R.id.type_fixed ? 1 : 2;

            String voucherId = vouchersRef.push().getKey();
            if (voucherId != null) {
                Voucher newVoucher = new Voucher();
                newVoucher.setId(voucherId);
                newVoucher.setRestaurantId(restaurantId);
                newVoucher.setType(type);
                newVoucher.setAmount(amount);
                newVoucher.setMinSpend(minSpend);
                newVoucher.setActive(true);

                vouchersRef.child(voucherId).setValue(newVoucher)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Voucher added successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to add voucher", Toast.LENGTH_SHORT).show());
            }
        });

        dialog.show();
    }

    public void showSharePopup(String voucherId) {
        Log.d("VoucherInfosActivity", "showSharePopup called for voucher ID: " + voucherId);
        String voucherLink = generateVoucherLink(voucherId);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Share Voucher")
                .setMessage("Voucher Link: \n" + voucherLink)
                .setPositiveButton("Copy to Clipboard", (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Voucher Link", voucherLink);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Share via Messenger", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.setPackage("com.facebook.orca");
                    intent.putExtra(Intent.EXTRA_TEXT, "Check out this voucher: " + voucherLink);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Messenger is not installed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Share via Instagram", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.setPackage("com.instagram.android");
                    intent.putExtra(Intent.EXTRA_TEXT, "Check out this voucher: " + voucherLink);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Instagram is not installed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(true)
                .show();
    }

    @Override
    public void onShareVoucher(String voucherId) {
        showSharePopup(voucherId);
    }

    private void loadData() {
        loadAvailableVouchers();
        loadClaimedVouchers();
    }

    private void loadAvailableVouchers() {
        Log.d("VoucherInfosActivity", "restaurantId: " + restaurantId);
        vouchersRef.orderByChild("restaurantId").equalTo(restaurantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("VoucherInfosActivity", snapshot.toString());
                        voucherList.clear();
                        for (DataSnapshot voucherSnapshot : snapshot.getChildren()) {
                            Voucher voucher = voucherSnapshot.getValue(Voucher.class);
                            if (voucher != null && voucher.isActive()) {
                                voucherList.add(voucher);
                            }
                        }
                        voucherAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(VoucherInfosActivity.this, "Failed to load vouchers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadClaimedVouchers() {
        claimedVouchersRef.orderByChild("restaurantId").equalTo(restaurantId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        claimedVoucherList.clear();
                        for (DataSnapshot claimedSnapshot : snapshot.getChildren()) {
                            ClaimedVoucher claimedVoucher = claimedSnapshot.getValue(ClaimedVoucher.class);
                            if (claimedVoucher != null) {
                                loadVoucherDetails(claimedVoucher);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(VoucherInfosActivity.this, "Failed to load claimed vouchers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadVoucherDetails(ClaimedVoucher claimedVoucher) {
        vouchersRef.child(claimedVoucher.getVoucherId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Voucher voucher = snapshot.getValue(Voucher.class);
                        if (voucher != null && voucher.getRestaurantId().equals(restaurantId)) {
                            claimedVoucherList.add(claimedVoucher);
                            filterClaimedVouchers(binding.searchInput.getText().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(VoucherInfosActivity.this, "Error loading voucher details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterClaimedVouchers(String query) {
        if (query.isEmpty()) {
            filteredClaimedVoucherList.clear();
            filteredClaimedVoucherList.addAll(claimedVoucherList);
        } else {
            filteredClaimedVoucherList.clear();
            filteredClaimedVoucherList.addAll(
                    claimedVoucherList.stream()
                            .filter(voucher -> voucher.getId().toLowerCase().contains(query.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
        claimedVoucherAdapter.notifyDataSetChanged();
    }

    private void checkAndUseVoucher(String voucherId) {
        claimedVouchersRef.child(voucherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            ClaimedVoucher claimedVoucher = snapshot.getValue(ClaimedVoucher.class);
                            if (claimedVoucher != null && !claimedVoucher.getUsed()) {
                                markVoucherAsUsed(voucherId);
                            } else {
                                Toast.makeText(VoucherInfosActivity.this, "Voucher has already been used", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(VoucherInfosActivity.this, "Invalid voucher code", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(VoucherInfosActivity.this, "Error checking voucher", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void markVoucherAsUsed(String voucherId) {
        claimedVouchersRef.child(voucherId).child("used").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(VoucherInfosActivity.this, "Voucher used successfully", Toast.LENGTH_SHORT).show();
                    binding.voucherInput.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(VoucherInfosActivity.this, "Failed to use voucher", Toast.LENGTH_SHORT).show());
    }
}