package com.itisdev.itisdev_final.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itisdev.itisdev_final.Domain.ClaimedVoucher;
import com.itisdev.itisdev_final.R;

import java.util.List;

public class ClaimedVoucherAdapter extends RecyclerView.Adapter<ClaimedVoucherAdapter.ViewHolder> {
    private Context context;
    private List<ClaimedVoucher> claimedVouchers;

    public ClaimedVoucherAdapter(Context context, List<ClaimedVoucher> claimedVouchers) {
        this.context = context;
        this.claimedVouchers = claimedVouchers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClaimedVoucher voucher = claimedVouchers.get(position);
        // Bind your data here
    }

    @Override
    public int getItemCount() {
        return claimedVouchers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView voucherIdText;
        TextView statusText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize your views here
        }
    }
}