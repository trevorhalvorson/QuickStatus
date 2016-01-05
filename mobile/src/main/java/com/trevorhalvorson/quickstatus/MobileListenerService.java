package com.trevorhalvorson.quickstatus;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Trevor Halvorson on 1/5/2016.
 */
public class MobileListenerService extends WearableListenerService {

    private static final String TAG = MobileListenerService.class.getSimpleName();
    private static final String WEARABLE_DATA_PATH = "/wearable/data/path";
    public static final String MESSAGE_EXTRA = "message_extra";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(WEARABLE_DATA_PATH)) {
            final String message = new String(messageEvent.getData());
            Bundle params = new Bundle();
            params.putString("message", message);
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/feed",
                    params,
                    HttpMethod.POST,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            Log.i(TAG, "onCompleted: " + response.getRawResponse());
                            // Make notification with status of post
                        }
                    }
            ).executeAsync();
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}
