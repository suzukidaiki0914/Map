package com.example.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.map.databinding.FragmentMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MapsFragment extends Fragment implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    private FragmentMapsBinding binding;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double latitude;
    private double longitude;
    private double positionLatitude;
    private double positionLongitude;
    private MarkerOptions position;
    private Polyline currentPolyline;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        binding.run.setOnClickListener(view -> randomRun());
        binding.yesBtn.setOnClickListener(view -> yesBtn());
        binding.noBtn.setOnClickListener(view -> noBtn());

        mapInitialize();
        return binding.getRoot();
    }

    private void noBtn() {
        binding.runPermission.setVisibility(LinearLayout.INVISIBLE);
    }

    private void yesBtn() {
        binding.runPermission.setVisibility(LinearLayout.INVISIBLE);
        new FetchURL(MapsFragment.this).execute(getUrl(position.getPosition(), "walking"), "walking");
        mapFragment.getMapAsync(this);

        LatLng latLng = new LatLng(latitude, longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

    }

    private void randomRun() {
        positionLatitude = 35.6905;
        positionLongitude = 139.6995;
        LatLng latLng = new LatLng(positionLatitude,positionLongitude);
        position = new MarkerOptions().position(latLng);

        mMap.addMarker(position);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        binding.runPermission.setVisibility(LinearLayout.VISIBLE);
    }

    private String getUrl(LatLng dest, String directionMode) {
        String str_origin = "origin=" + latitude + "," + longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=AIzaSyDyP3BNznVL3wVwMSUulsBi4vtKimX6ugo";
        return url;
    }

    private void mapInitialize() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(16);
        locationRequest.setInterval(3000);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
    }

    private void locationGPS(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Dexter.withContext(getContext())
                .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        fusedLocationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        }).addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                locationGPS(location);
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

}