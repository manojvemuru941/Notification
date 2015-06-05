package com.vemuru.manoj.androidnotificationwithwebsocket;

/**
 * Created by manoj on 5/29/15.
 */

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.PopupWindow;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;

public class GcmMessageHandler extends IntentService {


    private Handler handler;

    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();



        if(!isRunning(getApplication())) {
            if(extras.containsKey("mesage")) {
                    String mes = extras.getString("mesage");
                    try {
                        JSONObject jsonObject = new JSONObject(mes);
                        String msg = jsonObject.get("msgdata").toString();
                        String from = jsonObject.get("from").toString();
                        createNotification(from, msg);
                    } catch (Throwable e) {
                    }
            }else if(extras.containsKey("call")){
                String callData = extras.getString("call");
                try {
                    JSONObject jsonObject = new JSONObject(callData);
                    String msg = jsonObject.get("calldata").toString();
                    String from = jsonObject.get("from").toString();
                    createNotification(from, msg);
                } catch (Throwable e) {
                }
                createCallView();

            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);

    }
    public void createCallView(){
//        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        PopupWindow pw = new PopupWindow(inflater.inflate(R.layout.call_view,null,false),500,500,true);
//        pw.show
        Intent intent = new Intent(this,CallView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        getApplicationContext().startActivity(intent);
//        showToast();
    }
    public void createNotification(final String messageType,final String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.green_video_icon)
                .setContentTitle(messageType).setContentText(message);

        Intent resultIntent = new Intent(this, MainActivity.class);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        mBuilder.setLights(Color.BLUE, 500, 500);
        long[] pattern = {500,500,500,500,500,500,500,500,500};
        mBuilder.setVibrate(pattern);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());

    }
    public void showToast() {
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "dhfsjdgfj", Toast.LENGTH_LONG).show();
            }
        });

    }
    public boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if(task.baseActivity.getClassName().equalsIgnoreCase("com.vemuru.manoj.androidnotificationwithwebsocket.MainActivity")){
                return true;
            }


        }

        return false;
    }
}
