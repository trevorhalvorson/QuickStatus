package com.trevorhalvorson.quickstatus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.FacebookSdk;
import com.facebook.FacebookSdkNotInitializedException;
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

            TextView profileName = (TextView) findViewById(R.id.facebook_name);
            profileName.setText(Profile.getCurrentProfile().getName());

            ImageView profileImage = (ImageView) findViewById(R.id.facebook_profile_image);
            Glide.with(this)
                    .load(Profile.getCurrentProfile().getProfilePictureUri(50, 50))
                    .into(profileImage);
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
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }
}
