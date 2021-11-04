package com.topelec.smarthome;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import android.os.Bundle;

import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.topelec.rfidcontrol.ModulesControl;
import com.topelec.zigbeecontrol.Command;
import com.topelec.zigbeecontrol.SensorControl;

import java.util.ArrayList;

import it.moondroid.coverflowdemo.R;


public class MainActivity extends Activity implements
        View.OnClickListener,SensorControl.LedListener,SensorControl.PESensorListener,SensorControl.MotorListener,SensorControl.TempHumListener,SensorControl.LightSensorListener{

    private static final String TAG = "MainActivity";
    private static final int REQ_SYSTEM_SETTINGS = 1;
    private static final int REQ_RECAHRGE_INFO = 2;
    private boolean isLed1On;
    private boolean isLed2On;
    private boolean isLed3On;
    private boolean isLed4On;

    private int Temp;
    private int Hum;

    private boolean fanStatus;
    private boolean isBright;

    private ImageButton btnLed1;
    private ImageButton btnLed2;
    private ImageButton btnLed3;
    private ImageButton btnLed4;

    private ImageButton btnRecharge;

    private TextView tempView;
    private TextView humView;

    private ImageView fanView;
    private ImageButton btnStart;

    private ImageView brightnessView;

    private Button btnSettings;
    private Button btnBack;
    private boolean isAutoTempHum;
    private boolean isAutoBrightness;
    private int settingTemperature;
    private int settingHumidity;


    SensorControl mSensorControl;
    ModulesControl mModulesControl;

    private static final int TEMPERATURE_SENSOR = 31;
    private static final int HUMIDITY_SENSOR = 32;
    private static final int BRIGHTNESS_SENSOR = 40;

    public String state = "empty"; // empty,
    public ArrayMap idmap = new ArrayMap<>();

    public void stateTransferToEmpty() {
        if(!state.equals("leaved")){
            throw new IllegalStateException("IllegalStateTransfer");
        }

        state = "empty";
    }

    public void stateTransferToEntering() {
        if(!state.equals("empty")){
            throw new IllegalStateException("IllegalStateTransfer");
        }

        state = "entering";

        openDoorOne();
    }

    public void stateTransferToEntered() {
        if(!state.equals("entering")){
            throw new IllegalStateException("IllegalStateTransfer");
        }

        state = "entered";

        closeDoorOne();
        startShowering();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000*5);
                    sendOnWaitEndMessage();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void stateTransferToShowered() {
        if(!state.equals("entered")){
            throw new IllegalStateException("IllegalStateTransfer");
        }

        state = "showered";

        stopShowering();
        openDoorTwo();
    }

    public void stateTransferToLeaved() {
        if(!state.equals("showered")){
            throw new IllegalStateException("IllegalStateTransfer");
        }
        state = "leaved";

        closeDoorTwo();

        stateTransferToEmpty();
    }

    public void openDoorOne(){

        throw new RuntimeException("Not Implemented");
    }

    public void closeDoorOne(){
        throw new RuntimeException("Not Implemented");
    }

    public void openDoorTwo(){
        throw new RuntimeException("Not Implemented");
    }

    public void closeDoorTwo(){
        throw new RuntimeException("Not Implemented");
    }

    public void startShowering(){
        mSensorControl.fanForward(true);
    }

    public void stopShowering(){
        mSensorControl.fanStop(true);
    }

    // 传感器信号接收器

    public void onRFID(Bundle data) {
        if(idmap.containsKey(data.getString("cardNo"))){
            if(state.equals("empty")) {
                stateTransferToEntering();
            }
            else{
                throw new IllegalStateException("IllegalStateTransfer");
            }
        }
        else{
            System.out.println("卡还妹开捏");

        }


    }

    public void onGuangDian() {
        if (state.equals("entering")) {
            stateTransferToEntered();
        }

        if (state.equals("showered")) {
            stateTransferToLeaved();
        }
    }

    public void onWaitEnd() {
        if (state.equals("entered")) {
            stateTransferToShowered();
        }
    }

    public void sendOnRFIDMessage() {
        Message msg = new Message();
        msg.what = 0x21;
        myHandler.sendMessage(msg);
    }

    public void sendOnGuangdianMessage() {
        Message msg = new Message();
        msg.what = 0x22;
        myHandler.sendMessage(msg);
    }

    public void sendOnWaitEndMessage() {
        Message msg = new Message();
        msg.what = 0x23;
        myHandler.sendMessage(msg);
    }


    /**
     * 用于更新UI
     */

    Handler rfhander = new Handler(){
        public void handleMessage(Message msg){
            Bundle data;
            data = msg.getData();
            switch (msg.what) {
                //判断发送的消息
                case Command.HF_TYPE:  //设置卡片类型TypeA返回结果  ,错误类型:1

                    break;
                case  Command.HF_FREQ:  //射频控制（打开或者关闭）返回结果   ,错误类型:1


                    break;
                case Command.HF_ACTIVE:       //激活卡片，寻卡，返回结果


                    break;
                case Command.HF_ID:      //防冲突（获取卡号）返回结果

                    onRFID(data);
//                    Log.v(TAG,"Result = "+ data.getString("cardNo"));

                    break;
                default:
                    break;
            }
        }
    };
    Handler myHandler = new Handler() {
        //2.重写消息处理函数
        public void handleMessage(Message msg) {
            Bundle data;
            data = msg.getData();
            switch (msg.what) {
                //判断发送的消息
//                case Command.HF_ID:      //防冲突（获取卡号）返回结果
//
//                    onRFID(data);
////                    Log.v(TAG,"Result = "+ data.getString("cardNo"));
//
//                    break;
                case 0x22:{
                    onGuangDian();
                    break;
                }
                case 0x23:{
                    onWaitEnd();
                    break;
                }

//                case 0x03:
//                    System.out.println("Here is 0x03: "+data.getByte("sensor_status"));
//                    switch (data.getByte("senser_id") ) {
//                        case 0x01:
//                            Temp = data.getInt("senser_data");
//                            tempView.setText(String.valueOf(Temp));
//                            //如下温度自动化管理代码
//                            if (isAutoTempHum) {
//                                if (Hum > 60) {
//                                    if(!isLed1On){
//                                        mSensorControl.led1_On(true);
//                                    }
//                                    //TODO 温度大于设定值，降低温度，执行打开风扇动作
//                                    //if (!fanStatus) {
//                                    //    mSensorControl.fanForward(true);
//                                    //};
//                                } else {
//                                    if(isLed1On){
//                                        mSensorControl.led1_Off(true);
//                                    }
//                                    //TODO 实时温度小于设定值，停止降低温度，如果此时风扇是运行状态，则执行停止风扇动作。
//                                    //if (fanStatus) {
//                                    //    mSensorControl.fanStop(true);
//                                    //}
//                                }
//                            }
//                            break;
//                        case 0x02:
//                            Hum = data.getInt("senser_data");
//                            humView.setText(String.valueOf(Hum));
//                            break;
//                        default:
//                            break;
//                    }
//                    break;
//                case 0x04:
//                    if (data.getByte("senser_status") == 0x01) {
//                        brightnessView.setImageDrawable(getResources().getDrawable((R.drawable.smarthome_bright)));
////                        if (!fanStatus) {
////                            mSensorControl.fanForward(true);
////                        };
//                            mSensorControl.allLeds_Off(true);
//                    } else {
//                        brightnessView.setImageDrawable(getResources().getDrawable(R.drawable.smarthome_dark));
////                        if (fanStatus) {
////                            mSensorControl.fanStop(true);
////                        }
//                            mSensorControl.allLeds_On(true);
//                    }
//                    break;
//                case 0x05:
//                    System.out.println("Sensor: "+data.getByte("sensor_status"));
//                    if(data.getByte("sensor_status") == 0x00) {
//                        System.out.println(data.getByte("sensor_status"));
//
//                        if (!fanStatus) {
//                            mSensorControl.fanForward(true);
//                        };
//                    } else {
//                        //TODO 实时温度小于设定值，停止降低温度，如果此时风扇是运行状态，则执行停止风扇动作。
//                        if (fanStatus) {
//                            mSensorControl.fanStop(true);
//                        }
//                    }
//                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    //定义发送任务定时器
    Handler timerHandler = new Handler();
    Runnable sendRunnable = new Runnable() {
        int i = 1;
        @Override
        public void run() {
            //TODO:查询温度湿度
            switch (i) {
//                case 1:
//                    mSensorControl.checkTemperature(true);
//                    i++;
//                    break;
//                case 2:
//                    mSensorControl.checkHumidity(true);
//                    i++;
//                    break;
                case 1:
                    mSensorControl.checkBrightness(true);
//                    i++;
                    break;
//                case 2:
//                    mSensorControl.checkPE(true);
//                    i = 1;
//                    break;
                default:
                    break;
            }
            timerHandler.postDelayed(this,Command.CHECK_SENSOR_DELAY);
        }
    };

    private static final boolean AUTO_HIDE = true;
    private static final int UI_ANIMATION_DELAY = 300;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private final Handler mHideHandler = new Handler();

    //UI全屏显示
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };


    //显示程序中的底部控制按钮
    private View mControlsView;
    private View mBackView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {

            mControlsView.setVisibility(View.VISIBLE);
            mBackView.setVisibility(View.VISIBLE);
        }
    };


    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_smarthome);
        initialization();
        initSettings();
        mVisible = true;
        mControlsView = findViewById(R.id.smarthome_settings);
        mContentView = findViewById(R.id.smarthome_content);
        mModulesControl = new ModulesControl(rfhander);
        mModulesControl.actionControl(true);
        mBackView = findViewById(R.id.smarthome_back);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mSensorControl = new SensorControl();
        mSensorControl.addLedListener(this);
        mSensorControl.addMotorListener(this);
        mSensorControl.addTempHumListener(this);
        mSensorControl.addPESensorListener(this);
        mSensorControl.addLightSensorListener(this);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
    }

    private void hide() {

        mControlsView.setVisibility(View.GONE);
        mBackView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        mVisible = true;
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void initialization()
    {
        isLed1On = false;
        isLed2On = false;
        isLed3On = false;
        isLed4On = false;

        Temp = 101;
        Hum = 101;

        fanStatus = false;
        isBright = true;


        btnLed1 = (ImageButton) findViewById(R.id.btnLed1);
        btnLed1.setOnClickListener(this);

        btnLed2 = (ImageButton) findViewById(R.id.btnLed2);
        btnLed2.setOnClickListener(this);

        btnLed3 = (ImageButton) findViewById(R.id.btnLed3);
        btnLed3.setOnClickListener(this);

        btnLed4 = (ImageButton) findViewById(R.id.btnLed4);
        btnLed4.setOnClickListener(this);

        btnRecharge = (ImageButton) findViewById(R.id.switch_to_recharge);
        btnRecharge.setOnClickListener(this);

        tempView = (TextView) findViewById(R.id.tempView);
        humView = (TextView) findViewById(R.id.humView);

        fanView = (ImageView) findViewById(R.id.fanView);
        btnStart = (ImageButton) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);

        brightnessView = (ImageView) findViewById(R.id.brightnessView);

        btnSettings = (Button) findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(this);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.btnLed1:
                if (isLed1On)
                {
                    mSensorControl.led1_Off(false);
                }else{
                    mSensorControl.led1_On(false);
                }
                break;
            case R.id.btnLed2:
                if (isLed2On)
                {
                    mSensorControl.led2_Off(false);
                }else{
                    mSensorControl.led2_On(false);
                }
                break;
            case R.id.btnLed3:
                if (isLed3On)
                {
                    mSensorControl.led3_Off(false);
                }else{
                    mSensorControl.led3_On(false);
                }
                break;
            case R.id.btnLed4:
                if (isLed4On)
                {
                    mSensorControl.led4_Off(false);
                }else{
                    mSensorControl.led4_On(false);
                }
                break;
            case R.id.btnStart:
                if (fanStatus)
                {
                    mSensorControl.fanStop(false);
                }else {
                    mSensorControl.fanForward(false);
                }
                break;
            case R.id.btnSettings:
                startActivityForResult(new Intent(this, Settings.class), REQ_SYSTEM_SETTINGS);
                break;
            case R.id.btnBack:
                finish();
                break;
            case R.id.switch_to_recharge:
                startActivityForResult(new Intent(this, OpenCardActivity.class), REQ_RECAHRGE_INFO);
                break;
        }
    }

    private void initSettings() {

        PreferenceManager.setDefaultValues(this,R.xml.settings_smarthome,false);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        isAutoTempHum = settings.getBoolean("auto_temp_switch",true);
        isAutoBrightness = settings.getBoolean("auto_bright_switch",true);
        settingTemperature = Integer.parseInt(settings.getString("temp_settings","27"));
        settingHumidity = Integer.parseInt(settings.getString("hum_settings","40"));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQ_SYSTEM_SETTINGS) {

            PreferenceManager.setDefaultValues(this,R.xml.settings_smarthome,false);
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            isAutoTempHum = settings.getBoolean("auto_temp_switch",true);
            isAutoBrightness = settings.getBoolean("auto_bright_switch",true);
            settingTemperature = Integer.parseInt(settings.getString("temp_settings","27"));
            settingHumidity = Integer.parseInt(settings.getString("hum_settings","40"));

        }

        else if(requestCode == REQ_RECAHRGE_INFO){

        }
    }

    /**
     * 由不可见变为可见时调用
     */
    @Override
    protected void onStart() {
        super.onStart();

        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        mSensorControl.actionControl(true);

        //TODO:每350ms发送一次数据
        timerHandler.postDelayed(sendRunnable, Command.CHECK_SENSOR_DELAY);
    }
    /**
     * 在完全不可见时调用
     */
    @Override
    protected void onStop() {

        super.onStop();
        timerHandler.removeCallbacks(sendRunnable);
        mSensorControl.actionControl(false);

    }

    /**
     * 在活动销毁时调用
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSensorControl.removeLedListener(this);
        mSensorControl.removePESensorListener(this);
        mSensorControl.removeMotorListener(this);
        mSensorControl.removeTempHumListener(this);
        mSensorControl.removeLightSensorListener(this);
        mSensorControl.closeSerialDevice();
        mModulesControl.actionControl(false);
        mModulesControl.closeSerialDevice();
    }

    @Override
    public void LedControlResult(byte led_id,byte led_status) {

        Message msg = new Message();
        msg.what = 0x01;
        Bundle data = new Bundle();
        data.putByte("led_id",led_id);
        data.putByte("led_status",led_status);
        msg.setData(data);
        myHandler.sendMessage(msg);
    }

    @Override
    public void motorControlResult(byte motor_status) {

        Message msg =  new Message();
        msg.what = 0x02;
        Bundle data = new Bundle();
        data.putByte("motor_status",motor_status);
        msg.setData(data);
        myHandler.sendMessage(msg);
    }

    @Override
    public void tempHumReceive(byte senser_id,int senser_data) {

        Message msg  = new Message();
        msg.what = 0x03;
        Bundle data = new Bundle();
        data.putByte("senser_id",senser_id);
        data.putInt("senser_data",senser_data);
        msg.setData(data);
        myHandler.sendMessage(msg);
    }

    @Override
    public void peSensorReceive(byte sensor_status){
        sendOnGuangdianMessage();
    }

    public void lightSensorReceive(byte senser_status) {

        Message msg = new Message();
        msg.what = 0x04;
        Bundle data = new Bundle();
        data.putByte("senser_status",senser_status);
        msg.setData(data);
        myHandler.sendMessage(msg);
    }
}
