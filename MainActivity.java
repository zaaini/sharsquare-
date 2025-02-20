package com.example.ss;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private boolean isUploading = false;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private final int CAMERA_REQ_CODE = 100;
    private final int SELECT_PICTURE = 101;
    private List<Uri> imageUriList = new ArrayList<>();
    private ProgressDialog progressDialog;

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private ImageView imgCamera;
    public TextView textLatLong;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference rootDatabaseRef;
    private Button openGallery, camera, sendToSms, sendToDatabase, uploadImage;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textLatLong = findViewById(R.id.textLatLong);
        camera = findViewById(R.id.open_camra);
        openGallery = findViewById(R.id.open_gallry);
        uploadImage = findViewById(R.id.upload_image);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        imgCamera = findViewById(R.id.camra_img);
        Button sendToEmailButton = findViewById(R.id.sendtoemail);
        sendToSms = findViewById(R.id.sendtosms);
        Button openmaps = findViewById(R.id.openmap);
        sendToDatabase = findViewById(R.id.sendtodatabase);
        Button showroute = findViewById(R.id.showroute);

        rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Location services are disabled. Do you want to enable them?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            getCurrentLocation();
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            getCurrentLocation();
        }

        openmaps.setOnClickListener(v -> {
            String latLongText = textLatLong.getText().toString();
            String[] parts = latLongText.split("\n");
            double latitude = 0;
            double longitude = 0;

            for (String part : parts) {
                if (part.startsWith("Latitude:")) {
                    latitude = Double.parseDouble(part.substring(part.indexOf(":") + 1).trim());
                } else if (part.startsWith("Longitude:")) {
                    longitude = Double.parseDouble(part.substring(part.indexOf(":") + 1).trim());
                }
            }
            Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
            mapIntent.putExtra("LATITUDE", latitude);
            mapIntent.putExtra("LONGITUDE", longitude);
            startActivity(mapIntent);
        });

        openmaps.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this,take_pics.class);
            startActivity(i);
        });

        showroute.setOnClickListener(v -> {
            String origin = "Mirpure, NY"; // Replace with actual origin
            String destination = "dina, CA"; // Replace with actual destination

            // Create an Intent with the action ACTION_VIEW
            Intent intent = new Intent(Intent.ACTION_VIEW);

            // Set the URI to open Google Maps with the route
            Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" + origin + "&destination=" + destination);
            intent.setData(uri);

            // Set the package to Google Maps
            intent.setPackage("com.google.android.apps.maps");

            // Check if there is an activity that can handle the intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Start the activity
                startActivity(intent);
            } else {
                // Google Maps app is not installed, handle accordingly (e.g., show a message)
                Toast.makeText(MainActivity.this, "Google Maps app is not installed", Toast.LENGTH_SHORT).show();
            }
        });

        // Upload image button click listener
        uploadImage.setOnClickListener(v -> {
            Log.d(TAG, "Upload button clicked");
            if (textLatLong != null) {
                initiateUploadProcess();
            } else {
                Toast.makeText(MainActivity.this, "No location available", Toast.LENGTH_SHORT).show();
            }
        });

        // Open gallery button click listener
       openGallery.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent i= new Intent(MainActivity.this, MyHome.class);
               startActivity(i);
           }
       });

        // Send to SMS button click listener
        sendToSms.setOnClickListener(v -> openSmsMessagingApp());

        // Send to database button click listener
        sendToDatabase.setOnClickListener(v -> {
            String location = textLatLong.getText().toString();
            rootDatabaseRef.setValue(location);
        });

        // Send to email button click listener
        sendToEmailButton.setOnClickListener(v -> {
            String locationText = textLatLong.getText().toString();
            if (!locationText.isEmpty()) {
                String[] parts = locationText.split("\n");
                if (parts.length >= 2) {
                    String[] lat = parts[0].split(":");
                    String[] lon = parts[1].split(":");
                    if (lat.length > 1 && lon.length > 1) {
                        try {
                            double latitude = Double.parseDouble(lat[1].trim());
                            double longitude = Double.parseDouble(lon[1].trim());
                            sendLocationToEmail(latitude, longitude);
                            return;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Toast.makeText(MainActivity.this, "Invalid location data format.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Location data not found.", Toast.LENGTH_SHORT).show();
            }
        });

        // Get current location button click listener
        findViewById(R.id.buttonGetCurrentLocation).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
            } else {
                getCurrentLocation();
            }
        });
    }

    private void getCurrentLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                fusedLocationClient.removeLocationUpdates(this);
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    currentLatitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    currentLongitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                    textLatLong.setText(String.format("Latitude: %s\nLongitude: %s", currentLatitude, currentLongitude));
                }
            }
        }, Looper.getMainLooper());
    }

    private void initiateUploadProcess() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    double latitude = 0.0;
                    double longitude = 0.0;
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    } else {
                        Toast.makeText(this, "Location not available, using default values.", Toast.LENGTH_SHORT).show();
                    }
                    textLatLong.setText(String.format("Latitude: %s\nLongitude: %s", latitude, longitude));
                    uploadFile(latitude, longitude);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to retrieve location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    uploadFile(0.0, 0.0); // Using default values if location fails
                });
    }

    private void uploadFile(double latitude, double longitude) {
        File fileToUpload = new File(getExternalFilesDir(null), "uploadFile.jpg");
        if (!fileToUpload.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri fileUri = Uri.fromFile(fileToUpload);
        StorageReference fileRef = mStorageRef.child("uploads/" + fileToUpload.getName());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("latitude", String.valueOf(latitude))
                .setCustomMetadata("longitude", String.valueOf(longitude))
                .build();

        fileRef.putFile(fileUri, metadata)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "File uploaded with URL: " + uri.toString());
                    Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    saveUploadDetails(uri.toString(), latitude, longitude);
                }))
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUploadDetails(String imageUrl, double latitude, double longitude) {
        Upload upload = new Upload(imageUrl, latitude, longitude);
        DatabaseReference uploadRef = FirebaseDatabase.getInstance().getReference("uploads");
        uploadRef.push().setValue(upload);
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageUriList.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    imageUriList.add(data.getData());
                }
            }
            uploadImages();
        }
    }

    private void uploadImages() {
        for (Uri imageUri : imageUriList) {
            uploadFile(imageUri, currentLatitude, currentLongitude);
        }
    }

    private void uploadFile(Uri fileUri, double latitude, double longitude) {
        String fileName = fileUri.getLastPathSegment();
        StorageReference fileRef = mStorageRef.child("uploads/" + fileName);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("latitude", String.valueOf(latitude))
                .setCustomMetadata("longitude", String.valueOf(longitude))
                .build();

        fileRef.putFile(fileUri, metadata)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d(TAG, "File uploaded with URL: " + uri.toString());
                        Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        saveUploadDetails(uri.toString(), latitude, longitude);
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendLocationToEmail(double latitude, double longitude) {
        String emailContent = String.format("Latitude: %s\nLongitude: %s", latitude, longitude);
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"recipient@example.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "Location Information");
        email.putExtra(Intent.EXTRA_TEXT, emailContent);
        email.setType("message/rfc822");
        startActivity(Intent.createChooser(email, "Send email"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openSmsMessagingApp() {
        String locationText = textLatLong.getText().toString();
        String[] parts = locationText.split("\n");
        boolean isLatitudeFound = false;
        boolean isLongitudeFound = false;
        String latitude = "";
        String longitude = "";

        for (String part : parts) {
            if (part.contains("Latitude")) {
                String[] lat = part.split(":");
                if (lat.length > 1) {
                    latitude = lat[1].trim();
                    isLatitudeFound = true;
                }
            } else if (part.contains("Longitude")) {
                String[] lon = part.split(":");
                if (lon.length > 1) {
                    longitude = lon[1].trim();
                    isLongitudeFound = true;
                }
            }
        }

        if (isLatitudeFound && isLongitudeFound) {
            String smsMessage = "Latitude: " + latitude + "\nLongitude: " + longitude + "\n";
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse("smsto:"));
            smsIntent.putExtra("sms_body", smsMessage);
            startActivity(smsIntent);
        } else {
            Toast.makeText(MainActivity.this, "Location data not found.", Toast.LENGTH_LONG).show();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private File saveImageToFile(Bitmap bitmap) {
        File filesDir = getApplicationContext().getFilesDir();
        File imageFile = new File(filesDir, "camera_image.jpg");
        try {
            OutputStream os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(TAG, "Error saving bitmap to file: " + e.getMessage());
        }
        return imageFile;
    }
}
