package com.example.ss;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.ss.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
import com.google.maps.android.PolyUtil;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {
    private double latitude;
    private double longitude;
    private GoogleMap mMap;

    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    private static final String TAG = "MapsActivity";
    private static final String API_KEY = "AIzaSyCWWHbeSGV0u-W2CkcPjrfI1LBt3A7kCOM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "MapFragment is null", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if mapFragment is null
        }

        // Get latitude and longitude from intent extras
        Intent intent = getIntent();
        if (intent != null) {
            latitude = intent.getDoubleExtra("LATITUDE", 0);
            longitude = intent.getDoubleExtra("LONGITUDE", 0);
        } else {
            // Handle null intent
            Toast.makeText(this, "Intent is null", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if intent is null
        }

        // Find route from New York to Los Angeles
        findRoute("New York, NY", "Los Angeles, CA");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker at the specified location and move the camera
        LatLng location = new LatLng(latitude, longitude);
        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 11));

        // Add circle with 5km radius
        CircleOptions circleOptions = new CircleOptions()
                .center(location)
                .radius(5000) // Radius is in meters, so 5000 meters = 5 kilometers
                .strokeWidth(2)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(70, 255, 0, 0)); // Adjust transparency as needed
        mMap.addCircle(circleOptions);
    }



    private void findRoute(String origin, String destination) {
        try {
            GeoApiContext context = new GeoApiContext.Builder().apiKey(API_KEY).build();
            DirectionsResult result = DirectionsApi.newRequest(context)
                    .origin(origin)
                    .destination(destination)
                    .mode(TravelMode.DRIVING) // You can change the mode as needed (e.g., TravelMode.WALKING)
                    .units(Unit.IMPERIAL)
                    .await();

            // Draw polyline on the map using the first route
            if (result.routes != null && result.routes.length > 0) {
                DirectionsRoute route = result.routes[0];
                List<LatLng> decodedPath = PolyUtil.decode(route.overviewPolyline.getEncodedPath());
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(decodedPath)
                        .color(Color.BLUE)
                        .width(10); // You can adjust the width as needed
                mMap.addPolyline(polylineOptions);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception occurred while finding route: " + e.getMessage());
        }
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
