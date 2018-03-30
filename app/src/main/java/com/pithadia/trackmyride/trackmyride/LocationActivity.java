package com.pithadia.trackmyride.trackmyride;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class LocationActivity extends AppCompatActivity {

    private static final String TAG = "LocationActivity";

    private SectionsPageAdapter mSectionsPageAdapter;

    private ViewPager mViewPager;

    private PermissionsRequester permissionsRequester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        permissionsRequester = PermissionsRequester.newInstance(this);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter pageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        pageAdapter.addFragment(new LocationFragment(), "Current Location");
        viewPager.setAdapter(pageAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!permissionsRequester.hasPermissions()) {
            permissionsRequester.requestPermissions();
        } else {
            setupViewPager(mViewPager);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(R.id.container), R.string.no_permissions, Snackbar.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        setupViewPager(mViewPager);
    }
}
