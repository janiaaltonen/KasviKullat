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
        String content = uAction[1];
        String sId = uAction[2] + uAction[0];
        // sets unique id for every notification (otherwise only one per day with only one flower that depends on FLAG -settings)
        int id = Integer.parseInt(sId);
        int action = Integer.parseInt(uAction[0]);
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(content, action);
        notificationHelper.getManager().notify(id, nb.build());
    }
}
