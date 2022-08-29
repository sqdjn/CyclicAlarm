package com.zks.cyclicalarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/**
 * 通过receiver 进行通知，但是三星手机好像不行啊，就很难用。
 * （弃用）
 * */
public class AlarmReceiver extends BroadcastReceiver {
    private static int NOTIFICATION_ID = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        //接收到消息之后就弹出通知
        if(intent.getAction().equals("NOTIFICATION")){
            NotificationManager manager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent2 = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent2, 0);
            //新版本必须有channel
            NotificationChannel channel = new NotificationChannel("channel_1","124",NotificationManager.IMPORTANCE_LOW);
            //添加channel
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                manager.createNotificationChannel(channel);
            }
            NotificationCompat.Builder notice = new NotificationCompat.Builder(context, "channel_1")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)//必须设置小图标？？？
                    .setContentTitle("起飞！")
                    .setContentText("无规矩不成方圆。")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setChannelId("channel_1");;
            manager.notify(NOTIFICATION_ID+=1, notice.build());
        }
    }
}
