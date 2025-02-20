package com.example.ss;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeADapter extends RecyclerView.Adapter<HomeADapter.UploadViewHolder> {
    private Context mContext;
    private List<homeModel> mUploads;

    public HomeADapter(Context context, List<homeModel> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public UploadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.homeitemslayout, parent, false);
        return new UploadViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadViewHolder holder, int position) {
        homeModel uploadCurrent = mUploads.get(position);

        // Log the current item being processed
        Log.d("HomeAdapter", "Processing item at position: " + position);

        // Retrieve and set image URLs
        List<String> imageUrls = uploadCurrent.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Log.d("HomeAdapter", "Image URLs: " + imageUrls);
            retrived_Images_Recycleviewss imageAdapter = new retrived_Images_Recycleviewss(mContext, imageUrls);
            holder.recyclerViewImages.setAdapter(imageAdapter);
            holder.recyclerViewImages.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        } else {
            Log.d("HomeAdapter", "No Image URLs found");
        }

        // Setting the price
        String priceText = uploadCurrent.getPrice();
        if (priceText != null && !priceText.isEmpty()) {
            Log.d("HomeAdapter", "Price: " + priceText);
            try {
                double price = Double.parseDouble(priceText);
                holder.textViewPrice.setText(String.format("$%.2f", price));
            } catch (NumberFormatException e) {
                holder.textViewPrice.setText("Price not available");
                Log.e("HomeAdapter", "Error parsing price", e);
            }
        } else {
            holder.textViewPrice.setText("Price not available");
        }

        // Setting the location
        try {
            double latitude = Double.parseDouble(uploadCurrent.getLatitude());
            double longitude = Double.parseDouble(uploadCurrent.getLongitude());
            holder.textViewLocationName.setText(String.format("Lat: %.2f, Long: %.2f", latitude, longitude));
        } catch (NumberFormatException e) {
            holder.textViewLocationName.setText("Location not available");
            Log.e("HomeAdapter", "Error parsing location", e);
        }

        // Setting the brand and description
        holder.textViewBrand.setText(uploadCurrent.getBrand());
        holder.textViewDescription.setText(uploadCurrent.getDescription());

        // Additional button functionality
        holder.buttonLocation.setOnClickListener(v -> {
            try {
                double latitude = Double.parseDouble(uploadCurrent.getLatitude());
                double longitude = Double.parseDouble(uploadCurrent.getLongitude());
                String googleMapsUrl = "https://www.google.com/maps/dir/?api=1&destination=" + latitude + "," + longitude;

                // Open Google Maps with the route displayed
                Uri gmmIntentUri = Uri.parse(googleMapsUrl);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(mapIntent);
                } else {
                    // Handle case where Google Maps app is not installed
                    Toast.makeText(mContext, "Google Maps app is not installed", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(mContext, "Invalid location coordinates", Toast.LENGTH_SHORT).show();
                Log.e("HomeAdapter", "Error parsing location for maps", e);
            }
        });

        // Open RequestForBuy_exchange activity when Buy button is clicked
        holder.buttonBuy.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, RequestForBuy_exchange.class);
            intent.putExtra("email", uploadCurrent.getEmail());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public void updateData(List<homeModel> newUploads) {
        mUploads.clear();
        mUploads.addAll(newUploads);
        notifyDataSetChanged();
    }

    public static class UploadViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewPrice, textViewLocationName, textViewBrand, textViewDescription;
        public RecyclerView recyclerViewImages;
        public Button buttonLocation, buttonBuy; // Add buttonBuy

        public UploadViewHolder(View itemView) {
            super(itemView);
            recyclerViewImages = itemView.findViewById(R.id.recycler_view_images);
            textViewPrice = itemView.findViewById(R.id.text_view_price);
            textViewLocationName = itemView.findViewById(R.id.text_view_location_name);
            textViewBrand = itemView.findViewById(R.id.text_view_brand);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            buttonLocation = itemView.findViewById(R.id.button_location);
            buttonBuy = itemView.findViewById(R.id.button_buy); // Initialize buttonBuy
        }
    }
}
