package com.itisdev.itisdev_final.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itisdev.itisdev_final.Activity.VoucherInfosActivity;
import com.itisdev.itisdev_final.Domain.ClaimedVoucher;
import com.itisdev.itisdev_final.Domain.Voucher;
import com.itisdev.itisdev_final.Domain.VoucherShareListener;
import com.itisdev.itisdev_final.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    public static final int TYPE_PROFILE = 1;       // USER PROFILE
    public static final int TYPE_RESTAURANT = 2;    // RESTAURANT DETAILS
    public static final int TYPE_INFO = 3;          // VOUCHER INFO
    private Map<String, String> restaurantNames = new HashMap<>();
    private Context context;
    private List<Voucher> vouchers;
    private int viewType;
    private String currentUserId;
    private final VoucherShareListener shareListener;

    public void setRestaurantNames(Map<String, String> restaurantNames) {
        this.restaurantNames = restaurantNames;
    }

    public VoucherAdapter(Context context, List<Voucher> vouchers, int viewType, String currentUserId, VoucherShareListener shareListener) {
        this.context = context;
        this.vouchers = vouchers;
        this.viewType = viewType;
        this.currentUserId = currentUserId;
        this.shareListener = shareListener;

        Log.d("VoucherAdapter", "Context received: " + context.getClass().getSimpleName());

    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    private void showVoucherClaimedDialog(Context context, String voucherCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Voucher Claimed!")
                .setMessage("Your voucher code is: " + voucherCode + "\n\nShow this code to the restaurant when using the voucher.")
                .setPositiveButton("OK", null)
                .show();
    }

    private String getRestaurantPrefix(String restaurantId) {
        // Get restaurant name from database and return first letters
        // For now, return a default prefix
        return "VC";
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = vouchers.get(position);

        if (voucher.getType() == 1) {
            holder.discountText.setText("₱" + voucher.getAmount() + " off");
            holder.conditionText.setText("Min. Spend ₱" + voucher.getMinSpend());
        } else if (voucher.getType() == 2) {
            holder.discountText.setText("Up to ₱" + voucher.getAmount() + " discount");
        }

        Log.d("VoucherAdapter", "Attaching click listener for voucher ID: " + voucher.getId());

        holder.shareVoucherBtn.setOnClickListener(v -> {
            if (shareListener != null) {
                shareListener.onShareVoucher(voucher.getId());
            } else {
                Log.e("VoucherAdapter", "ShareListener is null.");
            }
    });

        Log.d("VoucherAdapter", "voucher: " + voucher.getRestaurantId());
        switch (viewType) {
            case TYPE_PROFILE:
                holder.restaurantText.setVisibility(View.VISIBLE);
                String restaurantName = restaurantNames.get(voucher.getRestaurantId());
                holder.restaurantText.setText(restaurantName != null ? restaurantName : "Restaurant Not Found");

                // Type 2 voucher
                if (voucher.getType() == 2) {
                    loadAvailedAmount(voucher, holder);
                }
                break;

            case TYPE_RESTAURANT:
            case TYPE_INFO:
                holder.restaurantText.setVisibility(View.GONE);
                holder.availedDiscountText.setVisibility(View.GONE);
                break;
        }

        // Claim Voucher Button Click Listener
        holder.claimVoucherBtn.setOnClickListener(v -> {
            // Generate a unique voucher code
            String voucherCode = getRestaurantPrefix(voucher.getRestaurantId()) +
                    String.format("%04d", (int)(Math.random() * 10000));

            // Create claimed voucher entry
            ClaimedVoucher claimedVoucher = new ClaimedVoucher();
            claimedVoucher.setId(voucherCode);
            claimedVoucher.setUserId(currentUserId);
            claimedVoucher.setVoucherId(voucher.getId());
            claimedVoucher.setUsed(false);

            // Save to Firebase
            FirebaseDatabase.getInstance().getReference("claimedVouchers")
                    .child(voucherCode)
                    .setValue(claimedVoucher)
                    .addOnSuccessListener(aVoid -> {
                        showVoucherClaimedDialog(context, voucherCode);
                        holder.claimVoucherBtn.setEnabled(false);
                        holder.claimVoucherBtn.setText("Claimed");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to claim voucher", Toast.LENGTH_SHORT).show();
                    });
        });

        // Disable button if already claimed
        FirebaseDatabase.getInstance().getReference("claimedVouchers")
                .child(currentUserId)
                .child(voucher.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            holder.claimVoucherBtn.setEnabled(false);
                            holder.claimVoucherBtn.setText("Claimed");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("VoucherAdapter", "Error checking claimed vouchers", error.toException());
                    }
                });
    }

    private void loadAvailedAmount(Voucher voucher, VoucherViewHolder holder) {
        FirebaseDatabase.getInstance().getReference("friendHelpDiscount")
                .orderByChild("voucherId").equalTo(voucher.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double availedAmount = 0;

                        for (DataSnapshot discountSnapshot : snapshot.getChildren()) {
                            String userId = discountSnapshot.child("userId").getValue(String.class);
                            if (userId != null && userId.equals(currentUserId)) {
                                Double amount = discountSnapshot.child("availedAmount").getValue(Double.class);
                                if (amount != null) {
                                    availedAmount = amount;
                                    break;
                                }
                            }
                        }
                        holder.availedDiscountText.setText("Availed: ₱" + availedAmount);
                        holder.availedDiscountText.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        holder.availedDiscountText.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView restaurantText, discountText, conditionText, availedDiscountText;
        Button claimVoucherBtn, shareVoucherBtn;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantText = itemView.findViewById(R.id.voucherRestaurantTxt);
            discountText = itemView.findViewById(R.id.voucherDiscountTxt);
            conditionText = itemView.findViewById(R.id.voucherConditionTxt);
            availedDiscountText = itemView.findViewById(R.id.voucherAvailedTxt);
            claimVoucherBtn = itemView.findViewById(R.id.claimVoucherBtn);
            shareVoucherBtn = itemView.findViewById(R.id.shareVoucherBtn);
        }
    }
}