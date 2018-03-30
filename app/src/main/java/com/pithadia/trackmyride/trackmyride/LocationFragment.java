package com.pithadia.trackmyride.trackmyride;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

/**
 * Created by rakshitpithadia on 3/29/18.
 */

public class LocationFragment extends Fragment {

    private static final String TAG = "LocationFragment";
    private static final String FRACTIONAL_FORMAT = "%.4f";

    private TextView latitudeValue;
    private TextView longitudeValue;

    private FusedLocationProviderClient fusedLocationProviderClient = null;

    @Override
    public void onStart() {
        super.onStart();
        registerForLocationUpdates();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_fragment, container, false);
        latitudeValue = (TextView) view.findViewById(R.id.latitude_value);
        longitudeValue = (TextView) view.findViewById(R.id.longitude_value);
        return view;
    }

    @Override
    public void onStop() {
        unregisterForLocationUpdates();
        super.onStop();
    }

    void updatePosition(Location location) {
        String latitudeString = createFractionString(location.getLatitude());
        String longitudeString = createFractionString(location.getLongitude());
        latitudeValue.setText(latitudeString);
        longitudeValue.setText(longitudeString);
    }

    private String createFractionString(double fraction) {
        return String.format(Locale.getDefault(), FRACTIONAL_FORMAT, fraction);
    }

    @SuppressLint("MissingPermission")
    void registerForLocationUpdates() {
        FusedLocationProviderClient locationProviderClient = getFusedLocationProviderClient();
        LocationRequest locationRequest = LocationRequest.create();
        Looper looper = Looper.myLooper();
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, looper);
    }

    @NonNull
    private FusedLocationProviderClient getFusedLocationProviderClient() {
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        }
        return fusedLocationProviderClient;
    }

    void unregisterForLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location lastLocation = locationResult.getLastLocation();
            updatePosition(lastLocation);
        }
    };
}
