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

    private GoogleApiClient googleApiClient;

    private TextView messageText;
    private TextView statusText;
    private DelayedConfirmationView delayedConfirmationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                messageText = (TextView) stub.findViewById(R.id.message_text);

                statusText = (TextView) stub.findViewById(R.id.status_text);
                statusText.setVisibility(View.INVISIBLE);

                delayedConfirmationView = (DelayedConfirmationView) stub.findViewById(R.id.delayed_confirmation);
                delayedConfirmationView.setTotalTimeMs(NUM_SECONDS * 1000);
                delayedConfirmationView.setListener(MainActivity.this);
                delayedConfirmationView.setVisibility(View.INVISIBLE);
            }
        });

        // Setup the google api client that will be used to send the message back to the mobile
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    private void getUserSpeech() {
        if (googleApiClient.isConnected()) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                List<String> results = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                String spokenText = results.get(0);

                messageText.setText(spokenText);

                startConfirmationTimer();
            } else {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startConfirmationTimer() {
        statusText.setVisibility(View.VISIBLE);

        delayedConfirmationView.setVisibility(View.VISIBLE);
        delayedConfirmationView.start();
    }

    @Override
    public void onTimerFinished(View view) {
        new SendMessageToDataLayer(WEARABLE_DATA_PATH, messageText.getText().toString()).start();

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
        delayedConfirmationView.reset();
        delayedConfirmationView.setVisibility(View.INVISIBLE);

        statusText.setVisibility(View.INVISIBLE);
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
                    Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
            for (Node node : nodesList.getNodes()) {
                MessageApi.SendMessageResult messageResult =
                        Wearable.MessageApi
                                .sendMessage(googleApiClient, node.getId(), mPath, mMessage.getBytes())
                                .await();

                // There was an error posting to Facebook
                if (!messageResult.getStatus().isSuccess()) {
                    Toast.makeText(MainActivity.this, getString(R.string.message_failure), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
