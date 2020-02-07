package com.aeropay_merchant.Utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.aeropay_merchant.R;
import com.aeropay_merchant.activity.AP_SDK_NavigationMenuActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class AP_SDK_MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM Service";
    private static int count = 0;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        AP_SDK_PrefKeeper.INSTANCE.setDeviceToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if(remoteMessage == null){
            return;
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData()!=null) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
            handleDataMessage(remoteMessage);
        }
    }


    private void handleDataMessage( RemoteMessage remoteMessage) {
        Log.e(TAG, "push json: " + remoteMessage.getData().toString());
        try {
            int ride_id = Integer.parseInt(remoteMessage.getData().get("ride_id"));
            String driverLat=remoteMessage.getData().get("driver_lat");
            String driverLang= remoteMessage.getData().get("driver_lng");
            String nType= remoteMessage.getData().get("notification_type");

            Intent intent = new Intent("ACTION_RIDE_ID");
            intent.putExtra("RIDE_ID", String.valueOf(ride_id));
            intent.putExtra("RRIVER_LAT", driverLat);
            intent.putExtra("RRIVER_LANG",driverLang);
            intent.putExtra("NOTIFICATION_TYPE", nType);
            // put your all data using put extra
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(getApplicationContext(), AP_SDK_NavigationMenuActivity.class);
        intent.putExtra("TestNotification", "yes");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 100001, intent,  PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "my_channel_01";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);// The user-visible name of the channel
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mNotifyManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        mBuilder.setContentTitle(title)
                .setContentText(messageBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setColor(Color.parseColor("#0078e2"))
                .setChannelId(CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        mNotifyManager.notify(count, mBuilder.build());
        count++;
    }
}