package com.example.ss;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyHome extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private HomeADapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_home_acitivty);

        initializeRecyclerView();
        fetchUploadsFromFirebase();
    }

    private void initializeRecyclerView() {
        mRecyclerView = findViewById(R.id.home2);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1)); // Adjust span count as needed
        mAdapter = new HomeADapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(mAdapter);
    }

    private void fetchUploadsFromFirebase() {
        FirebaseDatabase.getInstance().getReference("uploads")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<homeModel> uploads = new ArrayList<>();
                        for (DataSnapshot uploadSnapshot : dataSnapshot.getChildren()) {
                            for (DataSnapshot innerSnapshot : uploadSnapshot.getChildren()) {
                                try {
                                    String latitude = innerSnapshot.child("latitude").getValue(String.class);
                                    String longitude = innerSnapshot.child("longitude").getValue(String.class);
                                    String price = innerSnapshot.child("price").getValue(String.class);
                                    String sellOrExchange = innerSnapshot.child("sellOrExchange").getValue(String.class);
                                    String brand = innerSnapshot.child("brand").getValue(String.class);
                                    String description = innerSnapshot.child("description").getValue(String.class);
                                    String email = innerSnapshot.child("email").getValue(String.class);

                                    List<String> imageUrls = new ArrayList<>();
                                    DataSnapshot imagesSnapshot = innerSnapshot.child("images");
                                    for (DataSnapshot imageSnapshot : imagesSnapshot.getChildren()) {
                                        String imageUrl = imageSnapshot.getValue(String.class);
                                        if (imageUrl != null) {
                                            imageUrls.add(imageUrl);
                                        }
                                    }

                                    homeModel upload = new homeModel(imageUrls, latitude, longitude, price, sellOrExchange, brand, description, email);
                                    uploads.add(upload);

                                } catch (Exception e) {
                                    Log.e("MyHome", "Error parsing snapshot", e);
                                }
                            }
                        }
                        Log.d("MyHome", "Total uploads found: " + uploads.size());
                        mAdapter.updateData(uploads);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MyHome.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
