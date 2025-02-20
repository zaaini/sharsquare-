package com.example.ss;

import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class take_pics extends AppCompatActivity implements LocationListener {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int PERMISSION_REQUEST_CODE = 3;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 4;

    private ImageView imageView;
    private Button buttonChooseImage, buttonUploadPdf;
    private RecyclerView recyclerView;
    private EditText editTextPrice, editbrand, description;
    private TextView textViewLatitude, textViewLongitude;
    private RadioGroup radioGroup;
    private ImageAdapter imageAdapter;

    private List<Uri> imageUriList = new ArrayList<>();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private LocationManager locationManager;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private String userEmail;
    private String userId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takes_pics);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        storageRef = storage.getReference().child("uploads");

        recyclerView = findViewById(R.id.recycler_view_images);
        imageView = findViewById(R.id.image_view_mul);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        buttonUploadPdf = findViewById(R.id.buttonUploadPdf);
        editTextPrice = findViewById(R.id.edit_text_price);
        textViewLatitude = findViewById(R.id.edit_text_latitude);
        textViewLongitude = findViewById(R.id.edit_text_longitude);

        radioGroup = findViewById(R.id.radio_group);
        editbrand = findViewById(R.id.edit_brand);
        description = findViewById(R.id.description);

        progressBar = findViewById(R.id.progress_bar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        imageAdapter = new ImageAdapter(imageUriList);
        recyclerView.setAdapter(imageAdapter);


        buttonChooseImage.setOnClickListener(v -> {
            Log.d("ImagePicker", "Choose image button clicked");
            if (checkAndRequestPermissions()) {
                Log.d("ImagePicker", "Permissions granted");
                showImagePickerOptions();
            } else {
                Log.d("ImagePicker", "Permissions not granted");
            }
        });

        buttonUploadPdf.setOnClickListener(v -> {
            if (!imageUriList.isEmpty()) {
                if (validateDetails()) {
                    uploadImagesWithDetails(imageUriList);
                }
            } else {
                Toast.makeText(this, "Please select some images first.", Toast.LENGTH_SHORT).show();
            }
        });

        requestLocationPermission();
    }

    private boolean checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(READ_MEDIA_IMAGES);
            }

        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        // Add camera permission for all versions

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);

        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 5000, 5, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("LocationUpdate", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
        textViewLatitude.setText(String.valueOf(location.getLatitude()));
        textViewLongitude.setText(String.valueOf(location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerOptions();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission is required to get the current location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateDetails() {
        String price = editTextPrice.getText().toString().trim();
        String latitude = textViewLatitude.getText().toString().trim();
        String longitude = textViewLongitude.getText().toString().trim();
        String brand = editbrand.getText().toString().trim();
        String desc = description.getText().toString().trim();

        int selectedRadioId = radioGroup.getCheckedRadioButtonId();


        if (price.isEmpty() || latitude.isEmpty() || longitude.isEmpty() || brand.isEmpty() || desc.isEmpty() || selectedRadioId == -1) {
            Toast.makeText(this, "Please fill all the details.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showImagePickerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                handleImageSelection(data);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                handleCameraImage(data);
            }
        }
    }

    private void handleImageSelection(Intent data) {
        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                imageAdapter.addImageUri(imageUri);
            }
        } else if (data.getData() != null) {
            Uri imageUri = data.getData();
            imageAdapter.addImageUri(imageUri);
        }
    }

    private void handleCameraImage(Intent data) {
        Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
        if (imageBitmap != null) {
            Uri imageUri = getImageUri(imageBitmap);
            imageAdapter.addImageUri(imageUri);
        }
    }

    private Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void uploadImagesWithDetails(List<Uri> imageUris) {
        showProgressBar();
        String price = editTextPrice.getText().toString().trim();
        String latitude = textViewLatitude.getText().toString().trim();
        String longitude = textViewLongitude.getText().toString().trim();
        String brand = editbrand.getText().toString().trim();
        String desc = description.getText().toString().trim();

        int selectedRadioId = radioGroup.getCheckedRadioButtonId();

        RadioButton selectedRadioButton = findViewById(selectedRadioId);
        String sellOrExchange = selectedRadioButton != null ? selectedRadioButton.getText().toString() : "";

        Map<String, Object> details = new HashMap<>();
        details.put("price", price);
        details.put("latitude", latitude);
        details.put("longitude", longitude);
        details.put("sellOrExchange", sellOrExchange);
        details.put("brand", brand);
        details.put("description", desc);
        details.put("email", userEmail);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        DatabaseReference uploadsRef = dbRef.child("uploads").child(userId).push();
        uploadsRef.setValue(details)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseDB", "Details saved to database successfully.");
                    for (Uri uri : imageUris) {
                        String imageName = uri.getLastPathSegment();
                        StorageReference imageRef = storageRef.child(userId).child(uploadsRef.getKey()).child(imageName);
                        imageRef.putFile(uri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                        Log.d("ImageUploader", "Image uploaded successfully: " + downloadUri.toString());
                                        dbRef.child("uploads").child(userId).child(uploadsRef.getKey()).child("images").push().setValue(downloadUri.toString())
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Log.d("FirebaseDB", "Image URL saved to database successfully.");
                                                    hideProgressBar();
                                                    Toast.makeText(this, "Data uploaded successfully!", Toast.LENGTH_SHORT).show();
                                                    resetFields();
                                                    navigateToHome();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("FirebaseDB", "Failed to save image URL to database.", e);
                                                    hideProgressBar();
                                                });
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ImageUploader", "Error uploading image: " + uri.toString(), e);
                                    hideProgressBar();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Failed to save details to database.", e);
                    hideProgressBar();
                });
    }

    private void resetFields() {
        editTextPrice.setText("");
        textViewLatitude.setText("");
        textViewLongitude.setText("");
        editbrand.setText("");
        description.setText("");
        radioGroup.clearCheck();
        imageUriList.clear();
        imageAdapter.notifyDataSetChanged();
    }

    private void navigateToHome() {
        Intent homeIntent = new Intent(this, MyHome.class);
        startActivity(homeIntent);
        finish();
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
