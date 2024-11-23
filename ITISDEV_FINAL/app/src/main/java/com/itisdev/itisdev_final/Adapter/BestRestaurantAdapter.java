package com.itisdev.itisdev_final.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.itisdev.itisdev_final.Domain.Restaurant;
import com.itisdev.itisdev_final.R;

import java.util.ArrayList;

public class BestRestaurantAdapter extends RecyclerView.Adapter<BestRestaurantAdapter.viewholder> {

    ArrayList<Restaurant> restaurants;
    Context context;

    public BestRestaurantAdapter(ArrayList<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    @NonNull
    @Override
    public BestRestaurantAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_best_restaurant, parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull BestRestaurantAdapter.viewholder holder, int position) {
        holder.restaurantNameTxt.setText(restaurants.get(position).getName());
        holder.priceRangeTxt.setText(""+restaurants.get(position).getPriceRangeId());
        holder.ratingTxt.setText(""+restaurants.get(position).getRating());

//        Glide.with(context).load(restaurants.get(position).getPhotos().get(0))
//                .transform(new CenterCrop(), new RoundedCorners(30))
//                .into(holder.pic);
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView restaurantNameTxt, priceRangeTxt, ratingTxt;
        ImageView pic;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            restaurantNameTxt = itemView.findViewById(R.id.restaurantNameTxt);
            priceRangeTxt = itemView.findViewById(R.id.priceRangeTxt);
            ratingTxt = itemView.findViewById(R.id.ratingTxt);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}
