package com.trevorhalvorson.quickstatus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.FacebookSdkNotInitializedException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Profile.getCurrentProfile();
        } catch (FacebookSdkNotInitializedException e) {
            FacebookSdk.sdkInitialize(getApplicationContext());
        }

        if (Profile.getCurrentProfile() == null) {
            logout();
        } else {
            // User is logged in to Facebook
            setContentView(R.layout.activity_main);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            ImageView profileImage = (ImageView) findViewById(R.id.facebook_profile_image);
            TextView profileName = (TextView) findViewById(R.id.facebook_name);

            profileName.setText(Profile.getCurrentProfile().getName());
            Glide.with(this)
                    .load(Profile.getCurrentProfile().getProfilePictureUri(50, 50))
                    .into(profileImage);

            if (getIntent() != null && getIntent().getStringExtra(MobileListenerService.MESSAGE_EXTRA) != null) {
                String post = getIntent().getStringExtra(MobileListenerService.MESSAGE_EXTRA);

                Bundle params = new Bundle();
                params.putString("message", post);
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/feed",
                        params,
                        HttpMethod.POST,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Log.i(TAG, "onCompleted: " + response.getRawResponse());
                            }
                        }
                ).executeAsync();

            }
        }
    }

    private void logout() {
        LoginManager.getInstance().logOut();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }
}
