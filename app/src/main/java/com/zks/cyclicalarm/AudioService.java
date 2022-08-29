package com.zks.cyclicalarm;

import static java.lang.Thread.sleep;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    private final IBinder binder = new AudioBinder();
    private MediaPlayer mediaPlayer;
    private boolean flag = false;

    private TextView nextTimeView;

    public AudioService(){
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(AudioService.this.getClass().getName(),"执行onStartCommand.");
        return Service.START_STICKY_COMPATIBILITY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(AudioService.this.getClass().getName(),"onCreate");
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(this, R.raw.dididi);
            mediaPlayer.setOnCompletionListener(this);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        stopForeground(true);
        Log.d("class", "onDestroy");
    }

    public void pause(){
        if(mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }

    public void play(){
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
    }

    //为了与activity 交互，定义一个Binder对象
    class AudioBinder extends Binder {
        AudioService getService(){
            return AudioService.this;
        }
    }
}
