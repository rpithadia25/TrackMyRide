package com.pithadia.trackmyride.trackmyride;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.AuthenticationError;
import com.uber.sdk.android.core.auth.LoginCallback;
import com.uber.sdk.android.core.auth.LoginManager;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.Session;
import com.uber.sdk.core.client.SessionConfiguration;
import com.uber.sdk.rides.client.UberRidesApi;
import com.uber.sdk.rides.client.error.ApiError;
import com.uber.sdk.rides.client.error.ErrorParser;
import com.uber.sdk.rides.client.model.UserProfile;
import com.uber.sdk.rides.client.services.RidesService;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Reference : https://github.com/uber/rides-android-sdk

public class TrackerActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "Yq1Ujw2Zydu8USJa4bIR1hqurbxdJYyk";
    public static final String REDIRECT_URI = "com.pithadia.trackmyride.trackmyride.uberauth://redirect";

    private static final int CUSTOM_BUTTON_REQUEST_CODE = 1113;

    private static final String LOG_TAG = "TrackerActivity";

    private Button loginButton;
    private AccessTokenStorage accessTokenStorage;
    private LoginManager loginManager;
    private SessionConfiguration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        configuration = new SessionConfiguration.Builder()
                .setClientId(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .setScopes(Arrays.asList(Scope.PROFILE, Scope.RIDE_WIDGETS))
                .build();

        accessTokenStorage = new AccessTokenManager(this);

        loginManager = new LoginManager(accessTokenStorage,
                new SampleLoginCallback(),
                configuration,
                CUSTOM_BUTTON_REQUEST_CODE);

        loginButton = findViewById(R.id.custom_uber_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginManager.login(TrackerActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loginManager.isAuthenticated()) {
            loadProfileInfo();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LOG_TAG, String.format("onActivityResult requestCode:[%s] resultCode [%s]",
                requestCode, resultCode));

        loginManager.onActivityResult(this, requestCode, resultCode, data);
    }

    private class SampleLoginCallback implements LoginCallback {

        @Override
        public void onLoginCancel() {
            Toast.makeText(TrackerActivity.this, R.string.user_cancels_message, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLoginError(@NonNull AuthenticationError error) {
            Toast.makeText(TrackerActivity.this,
                    getString(R.string.login_error_message, error.name()), Toast.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onLoginSuccess(@NonNull AccessToken accessToken) {
            loadProfileInfo();
        }

        @Override
        public void onAuthorizationCodeReceived(@NonNull String authorizationCode) {
            Toast.makeText(TrackerActivity.this, getString(R.string.authorization_code_message, authorizationCode),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void loadProfileInfo() {
        Session session = loginManager.getSession();
        RidesService service = UberRidesApi.with(session).build().createService();

        service.getUserProfile()
                .enqueue(new Callback<UserProfile>() {
                    @Override
                    public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(TrackerActivity.this, getString(R.string.greeting, response.body().getFirstName()), Toast.LENGTH_LONG).show();
                        } else {
                            ApiError error = ErrorParser.parseError(response);
                            Toast.makeText(TrackerActivity.this, error.getClientErrors().get(0).getTitle(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfile> call, Throwable t) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        accessTokenStorage = new AccessTokenManager(this);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {
            accessTokenStorage.removeAccessToken();
            Toast.makeText(this, "AccessToken cleared", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_copy) {
            AccessToken accessToken = accessTokenStorage.getAccessToken();

            String message = accessToken == null ? "No AccessToken stored" : "AccessToken copied to clipboard";
            if (accessToken != null) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("UberSampleAccessToken", accessToken.getToken());
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
