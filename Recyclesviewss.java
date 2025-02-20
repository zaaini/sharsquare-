package com.example.ss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class  Recyclesviewss extends AppCompatActivity {
    public RecyclerView recyclerView;
    private listAdapters llistAdapters;
    private ArrayList<Upload> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclesviewss);

        // Initialize RecyclerView and ArrayList
        recyclerView = findViewById(R.id.res1);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();

        Collections.reverse(list);

        llistAdapters = new listAdapters(this, list);
        recyclerView.setAdapter(llistAdapters);

        // Retrieve data from Firebase Database
        FirebaseDatabase.getInstance().getReference("uploads")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // Retrieve latitude, longitude, and image URL from Firebase
                                Double latitudeDouble = snapshot.child("latitude").getValue(Double.class);
                                Double longitudeDouble = snapshot.child("longitude").getValue(Double.class);
                                String imageUrl = snapshot.child("mImageUri").getValue(String.class);
                                



                                // Check if latitudeDouble and longitudeDouble are not null
                                if (latitudeDouble != null && longitudeDouble != null) {
                                    double latitude = latitudeDouble;
                                    double longitude = longitudeDouble;

                                    // Create an Upload object with the retrieved data
                                    Upload upload = new Upload(imageUrl, latitude, longitude);

                                    // Add the Upload object to the RecyclerView
                                    addDataToRecyclerView(upload);



                                } else {

                                    // Handle the case where latitude or longitude is null

                                }
                            }
                        } else {
                            // Handle the case where there are no uploads in the database
                            Toast.makeText(Recyclesviewss.this, "No uploads found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    // Method to add data to RecyclerView
                    private void addDataToRecyclerView(Upload upload) {
                        // Add the new data to the list
                        list.add(upload);
                        // Notify the adapter about the data change
                        llistAdapters.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Toast.makeText(Recyclesviewss.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

}