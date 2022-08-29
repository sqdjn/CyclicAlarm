package com.zks.cyclicalarm;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //用两个标志位，一个标记按钮状态，一个标记循环状态。必须要分开，不能一起用。
    //直接改用两个按钮咯！！不要追求完美，怎么行怎么来。
    //private boolean running = false;
    private boolean flag = false;
    //private AlarmManager mAManager;
    //保存配置
    private SharedPreferences sp;
    private Switch aSwitch = null;
    private NumberPicker pickerMinute;

    private AudioService audioService;
    //用连接监听service状态的变化
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            audioService = ((AudioService.AudioBinder)iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            audioService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //读取设置
        sp = getSharedPreferences("mysp", Context.MODE_PRIVATE);
        String minute0 = sp.getString("minute", "5");
        String switch0 = sp.getString("switch","open");

        //设置数字选择器
        pickerMinute = (NumberPicker)findViewById(R.id.pickerMinute);
        pickerMinute.setMinValue(1);
        pickerMinute.setMaxValue(60);
        pickerMinute.setValue(Integer.parseInt(minute0));
        pickerMinute.setOnValueChangedListener((numberPicker, i, i1) -> {

        });

        //设置开关
        aSwitch = (Switch) findViewById(R.id.switchNow);
        aSwitch.setChecked(switch0.equals("open"));

        //设置按钮变化
        Button btnStart = (Button)findViewById(R.id.btnStart);
        Button btnEnd = (Button)findViewById(R.id.btnEnd);
        TextView txtNextTime = (TextView)findViewById(R.id.txtNextTime);

        /**
         * 通知的方法（弃用）

         //设置定时管理器
         mAManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
         intent.setAction("NOTIFICATION");
         PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

        btnStart.setOnClickListener(view -> {
            flag = true;
            new Thread(() -> {
                for(int i=0;;i++){
                    if(!flag) break;
                    int minute2 = pickerMinute.getValue();
                    long a = minute2*1000L*60;
                    long now = new Date().getTime();
                    long b = aSwitch.isChecked()?a-now%a:a;
                    now += b;
                    mAManager.set(AlarmManager.RTC_WAKEUP, now, pi);
                    //simpledateformat 线程不安全，不过我就一个线程。
                    //post()
                    long finalNow = now;
                    txtNextTime.post(() -> txtNextTime.setText(new SimpleDateFormat("hh:mm", Locale.CHINA).format(new Date(finalNow))));
                    try {
                        //这里犯蠢，第一次少2s，之后只要循环一样就一直差两秒。
                        //小明比小红小三岁，问几年以后，小明和小红一样大！！sb
                        if(i==0){
                            sleep(b+2000);
                        }else{
                            sleep(1000L*60*minute2);
                        }
                    }catch (InterruptedException e){
                        Toast.makeText(MainActivity.this, "出现异常", Toast.LENGTH_LONG).show();
                    }
                }
            }).start();
            btnEnd.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.GONE);
            pickerMinute.setEnabled(false);
            aSwitch.setEnabled(false);
        });

        btnEnd.setOnClickListener(view->{
            flag = false;
            mAManager.cancel(pi);
            btnStart.setVisibility(View.VISIBLE);
            btnEnd.setVisibility(View.GONE);
            pickerMinute.setEnabled(true);
            aSwitch.setEnabled(true);
        });
        */

        //播放音乐的方法
        //service和activity 都是主线程，耗时操作异步
        btnStart.setOnClickListener(view ->{
            Intent intentService = new Intent(MainActivity.this, AudioService.class);
            bindService(intentService,conn,Context.BIND_AUTO_CREATE);

            flag = true;
            new Thread(() -> {
                for(int i=0;;i++){
                    int minute2 = pickerMinute.getValue();
                    long a = minute2*1000L*60;
                    long now = new Date().getTime();
                    long b = aSwitch.isChecked()?a-now%a:a;
                    now += b;
                    //post()
                    long finalNow = now;
                    txtNextTime.post(() -> txtNextTime.setText(new SimpleDateFormat("hh:mm", Locale.CHINA).format(new Date(finalNow))));
                    try {
                        //这里犯蠢，第一次少2s，之后只要循环一样就一直差两秒。
                        //小明比小红小三岁，问几年以后，小明和小红一样大！！sb
                        if(i==0){
                            sleep(b);
                        }else{
                            sleep(1000L*60*minute2);
                        }
                    }catch (InterruptedException e){
                        Toast.makeText(MainActivity.this, "出现异常", Toast.LENGTH_LONG).show();
                    }
                    if(!flag) break;
                    audioService.play();
                }
            }).start();

            btnStart.setVisibility(View.GONE);
            btnEnd.setVisibility(View.VISIBLE);
            pickerMinute.setEnabled(false);
            aSwitch.setEnabled(false);
        });

        btnEnd.setOnClickListener(view->{
            flag = false;
            btnStart.setVisibility(View.VISIBLE);
            btnEnd.setVisibility(View.GONE);
            pickerMinute.setEnabled(true);
            aSwitch.setEnabled(true);
            unbindService(conn);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("minute",String.valueOf(pickerMinute.getValue()));
        editor.putString("switch",aSwitch.isChecked()?"open":"close");
        editor.commit();
    }
}