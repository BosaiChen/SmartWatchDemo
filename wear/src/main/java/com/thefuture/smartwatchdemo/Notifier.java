package com.thefuture.smartwatchdemo;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.SpannableString;

public class Notifier {
    public static final int NOTIFICATION_ID_DEVICE_DISCONNECTED = 1;

    public static void notifyDeviceDisconnected(Context ctx, String deviceName) {
        SpannableString title = new SpannableString(ctx.getString(R.string.app_name));
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(deviceName + " is disconnected")
//                        .setContentIntent(viewPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setLocalOnly(true)
//                        .setVibrate(new long[] {0, 50});
                        .setVibrate(new long[]{500, 500, 500, 500});

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);

        notificationManager.notify(NOTIFICATION_ID_DEVICE_DISCONNECTED, notificationBuilder.build());
    }

    public static void cancelNotification(Context ctx, int notificationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
        notificationManager.cancel(notificationId);
    }
}
