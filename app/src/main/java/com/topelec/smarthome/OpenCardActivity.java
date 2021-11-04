package com.topelec.smarthome;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.topelec.database.DatabaseHelper;
import com.topelec.rfidcontrol.ModulesControl;
import com.topelec.zigbeecontrol.Command;

import java.util.ArrayList;

import it.moondroid.coverflowdemo.R;

public class OpenCardActivity extends Activity {

    private final static String TAG = ".OpenCardActivity";
    private TextView idView;
    private EditText rechargeText;
    private ImageButton btnAuthor;
    private ImageButton btnCancelAuthor;
    private Button btnReturn;

    private String currentId = new String();

    ModulesControl mModulesControl;

    ArrayList id_list = new ArrayList();
    private String cardNo = new String();
    Context mContext;

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

                    System.out.println(data.getString("cardNo"));
                    idView.setText("识别到卡号:"+data.getString("cardNo"));
                    cardNo = data.getString("cardNo");

//                    Log.v(TAG,"Result = "+ data.getString("cardNo"));

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smartkiller_recharge);
        /**数据库相关变量初始化**/
        mContext = this;
        mContentView = findViewById(R.id.smart_layout);

        mModulesControl = new ModulesControl(rfhander);
        mModulesControl.actionControl(true);

        id_list = getIntent().getStringArrayListExtra("list");

        btnAuthor = (ImageButton) findViewById(R.id.btn_author);
        btnAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cardNo.equals("")){
                    idView.setText("请将磁卡放置于刷卡器上");
                }else{
                    id_list.add(cardNo);
                    idView.setText("注册成功");
                }
                cardNo = "";
            }
        });
        btnCancelAuthor = (ImageButton) findViewById(R.id.btn_cancel_author);
        btnCancelAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cardNo.equals("")){
                    idView.setText("请将磁卡放置于刷卡器上");
                }else{
                    id_list.remove(cardNo);
                    idView.setText("注销成功");
                }
                cardNo = "";
            }
        });
        idView = (TextView) findViewById(R.id.idView);

        // 在这里填入需要返回的内容
        btnReturn = (Button) findViewById(R.id.btn_back);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardNo = "";
                Intent returnInte = new Intent();
                returnInte.putExtra("list",id_list);
                setResult(RESULT_OK, returnInte);
                finish();
            }
        });
    }

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


    @Override
    protected void onStart() {
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        super.onStart();
        /**用于接收group发送过来的广播**/
//        IntentFilter filter = new IntentFilter(CardActivityGroup.recharge_action);
//        registerReceiver(mBroadcastReceiver,filter);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mModulesControl.actionControl(false);
        mModulesControl.closeSerialDevice();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
////        getMenuInflater().inflate(R.menu.menu_recharge, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
