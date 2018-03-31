package com.pithadia.trackmyride.trackmyride.fragment;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.pithadia.trackmyride.trackmyride.R;
import com.pithadia.trackmyride.trackmyride.api.ApiUtils;
import com.pithadia.trackmyride.trackmyride.data.Data;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rakshitpithadia on 3/29/18.
 */

public class LocationFragment extends Fragment {

    private static final String TAG = "LocationFragment";
    private static final String FRACTIONAL_FORMAT = "%.4f";
    private static final String SLACK_CHANNEL_KEY = "T042FMKHH/B1LF1T12L/HoTvYOUSovFpfl9LI59GBKnq";

    private TextView latitudeValue;
    private TextView longitudeValue;

    private String mLatitudeLabel;
    private String mLongitudeLabel;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    private FusedLocationProviderClient fusedLocationProviderClient = null;

    @Override
    public void onStart() {
        super.onStart();

        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);

        createLocationCallback();
        createLocationRequest();
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

    void updatePosition() {
        String latitudeString = createFractionString(mCurrentLocation.getLatitude());
        String longitudeString = createFractionString(mCurrentLocation.getLongitude());

        latitudeValue.setText(String.format(Locale.ENGLISH, "%s: %s", mLatitudeLabel,
                latitudeString));
        longitudeValue.setText(String.format(Locale.ENGLISH, "%s: %s", mLongitudeLabel,
                longitudeString));
    }

    private String createFractionString(double fraction) {
        return String.format(Locale.getDefault(), FRACTIONAL_FORMAT, fraction);
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(6000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    void registerForLocationUpdates() {
        FusedLocationProviderClient locationProviderClient = getFusedLocationProviderClient();
        Looper looper = Looper.myLooper();
        locationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, looper);
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
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                updatePosition();
                sendLocationToSlack();
            }
        };
    }

    private void sendLocationToSlack() {

        String locationString = String.format(Locale.ENGLISH, "Name: Rakshit - Latitude: %f, Longitude: %f.",
                mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        ApiUtils.getAPIService().sendLocation(SLACK_CHANNEL_KEY,
                new Data("#android", "Rakshit", locationString, ":ghost:")).enqueue(new Callback<Data>() {

            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {

                if (response != null) {
                    Log.i("TAG", "Response: " + response.toString());
                }

                if (response.isSuccessful()) {
                    Log.i(TAG, "POST submitted to Slack Channel #android." + response.body().toString());
                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.e(TAG, "Unable to submit post to Slack: " + t.getMessage());
            }
        });
    }
}
