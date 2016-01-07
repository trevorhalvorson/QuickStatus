package com.trevorhalvorson.quickstatus;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Trevor Halvorson on 1/5/2016.
 */
public class MobileListenerService extends WearableListenerService {

    private static final String TAG = MobileListenerService.class.getSimpleName();
    private static final String WEARABLE_DATA_PATH = "/wearable/data/path";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(WEARABLE_DATA_PATH) && isLoggedIn()) {
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
                            String title = getString(R.string.post_success_notification_title);

                            if (response.getError() != null) {
                                title = getString(R.string.post_error_notification_title);
                            }

                            int notificationId = 0;
                            String facebookUrl = "https://www.facebook.com/" + Profile.getCurrentProfile().getId();
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl));
                            PendingIntent pendingIntent =
                                    PendingIntent.getActivity(MobileListenerService.this, 0, intent, 0);

                            NotificationCompat.Builder builder =
                                    new NotificationCompat.Builder(MobileListenerService.this)
                                            .setSmallIcon(R.drawable.ic_watch)
                                            .setContentTitle(title)
                                            .addAction(R.drawable.ic_person, getString(R.string.notification_action_text), pendingIntent);

                            NotificationManagerCompat manager =
                                    NotificationManagerCompat.from(MobileListenerService.this);
                            manager.notify(notificationId, builder.build());
                        }
                    }
            ).executeAsync();
        } else {
            int notificationId = 1;
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(MobileListenerService.this)
                            .setSmallIcon(R.drawable.ic_watch)
                            .setContentTitle(getString(R.string.post_error_notification_title))
                            .setContentText(getString(R.string.post_error_notification_content))
                            .setVibrate(new long[]{1000, 1000});

            NotificationManagerCompat manager =
                    NotificationManagerCompat.from(MobileListenerService.this);
            manager.notify(notificationId, builder.build());
        }
        super.onMessageReceived(messageEvent);
    }

    private boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }
}
