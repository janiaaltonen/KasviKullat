package com.example.kasvikullat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String uniqueAction = intent.getAction();
        String [] uAction = uniqueAction.split(",");
        String content = uAction[0];
        int Id = Integer.parseInt(uAction[1]); // sets unique id for every notification (otherwise only one per day with only one flower that depends on FLAG -settings)
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(content);
        notificationHelper.getManager().notify(Id, nb.build());
    }
}
