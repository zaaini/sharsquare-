package com.example.ss;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.trusted.TokenStore;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;


import java.util.ArrayList;

public class listAdapters extends RecyclerView.Adapter<listAdapters.viewholder> {
    Context context;
    ArrayList<Upload> list;

    public listAdapters(Context context, ArrayList<Upload> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.sampleitemslists, parent, false);
        return new viewholder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull viewholder holder, @SuppressLint("RecyclerView") int position) {
        Upload upload = list.get(position);


        String imagePath; // Assuming getImageUrl() returns the image path as a String
        imagePath = upload.getmImageUri();
        holder.imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Construct Google Maps URL with route information
                double destinationLatitude = upload.getLatitude();
                double destinationLongitude = upload.getLongitude();
                String googleMapsUrl = "https://www.google.com/maps/dir/?api=1&destination=" + destinationLatitude + "," + destinationLongitude;

                // Open Google Maps with the route displayed
                Uri gmmIntentUri = Uri.parse(googleMapsUrl);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                } else {
                    // Handle case where Google Maps app is not installed
                    Toast.makeText(context, "Google Maps app is not installed", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Glide.with(context)
                .load(upload.getmImageUri()) // Assuming getmImageUri() returns the image URL
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Optional: Adjust caching strategy
                .into(holder.imageView1);

        holder.textView1.setText("Latitude: " + upload.getLatitude());
        holder.textView2.setText("Longitude: " + upload.getLongitude());






    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        ImageView imageView1,imageView2;
        TextView textView1;
        TextView textView2;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            imageView1 = itemView.findViewById(R.id.sampleimage1);
            imageView2=itemView.findViewById(R.id.seller_and_buyer_locaton_route);
            textView1 = itemView.findViewById(R.id.sampletext1);
            textView2 = itemView.findViewById(R.id.sampletext2);
        }
    }

}
