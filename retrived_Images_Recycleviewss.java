package com.example.ss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class retrived_Images_Recycleviewss extends RecyclerView.Adapter<retrived_Images_Recycleviewss.ImageViewHolder> {
    private Context mContext;
    private List<String> mImageUrls;

    public retrived_Images_Recycleviewss(Context context, List<String> imageUrls) {
        mContext = context;
        mImageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item_layout, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = mImageUrls.get(position);
        Glide.with(mContext)
                .load(imageUrl)
                .placeholder(R.drawable.cloulddownload)
                .error(R.drawable.cloulddownload)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view21);
        }
    }
}
