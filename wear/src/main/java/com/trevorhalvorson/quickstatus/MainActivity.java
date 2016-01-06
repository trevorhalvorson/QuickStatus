package com.trevorhalvorson.quickstatus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DelayedConfirmationView.DelayedConfirmationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String WEARABLE_DATA_PATH = "/wearable/data/path";
    private static final int SPEECH_REQUEST_CODE = 0;
    private static final int NUM_SECONDS = 5;

    private GoogleApiClient mGoogleApiClient;

    private TextView mMessageText;
    private TextView mStatusText;
    private DelayedConfirmationView mDelayedConfirmationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mMessageText = (TextView) stub.findViewById(R.id.message_text);

                mStatusText = (TextView) stub.findViewById(R.id.status_text);
                mStatusText.setVisibility(View.INVISIBLE);

                mDelayedConfirmationView = (DelayedConfirmationView) stub.findViewById(R.id.delayed_confirmation);
                mDelayedConfirmationView.setTotalTimeMs(NUM_SECONDS * 1000);
                mDelayedConfirmationView.setListener(MainActivity.this);
                mDelayedConfirmationView.setVisibility(View.INVISIBLE);
            }
        });

        // Setup the google api client that will be used to send the message back to the mobile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void getUserSpeech() {
        if (mGoogleApiClient.isConnected()) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);

            mMessageText.setText(spokenText);

            startConfirmationTimer();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startConfirmationTimer() {
        mStatusText.setVisibility(View.VISIBLE);

        mDelayedConfirmationView.setVisibility(View.VISIBLE);
        mDelayedConfirmationView.start();
    }

    @Override
    public void onTimerFinished(View view) {
        new SendMessageToDataLayer(WEARABLE_DATA_PATH, mMessageText.getText().toString()).start();

        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.timer_finished_text));
        startActivity(intent);

        finish();
    }

    // Timer stopped by user
    @Override
    public void onTimerSelected(View view) {
        view.setPressed(true);

        resetUI();

        getUserSpeech();
    }

    private void resetUI() {
        mDelayedConfirmationView.reset();
        mDelayedConfirmationView.setVisibility(View.INVISIBLE);

        mStatusText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getUserSpeech();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class SendMessageToDataLayer extends Thread {
        String mPath;
        String mMessage;

        public SendMessageToDataLayer(String path, String message) {
            this.mPath = path;
            this.mMessage = message;
        }

        @Override
        public void run() {
            NodeApi.GetConnectedNodesResult nodesList =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodesList.getNodes()) {
                MessageApi.SendMessageResult messageResult =
                        Wearable.MessageApi
                                .sendMessage(mGoogleApiClient, node.getId(), mPath, mMessage.getBytes())
                                .await();

                // There was an error posting to Facebook
                if (!messageResult.getStatus().isSuccess()) {
                    Toast.makeText(MainActivity.this, getString(R.string.message_failure), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
