package com.trevorhalvorson.quickstatus;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class LoginActivity extends AppCompatActivity {

    private static final String PERMISSIONS = "publish_actions";

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setPublishPermissions(PERMISSIONS);
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // TODO: Check if permission was granted before starting MainActivity
                        Intent successIntent = new Intent(LoginActivity.this, MainActivity.class);
                        successIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(successIntent);
                    }

                    @Override
                    public void onCancel() {
                        // TODO: Switch to DialogFragment to show longer message explaining permissions
                        Snackbar.make(findViewById(R.id.login_layout), R.string.login_cancel_text, Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Snackbar.make(findViewById(R.id.login_layout), R.string.login_error_text, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}