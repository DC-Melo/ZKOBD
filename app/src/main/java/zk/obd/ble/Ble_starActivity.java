package zk.obd.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import zk.obd.R;

//test main
//second
public class Ble_starActivity extends Activity {
    int index=0;
    int messagecount=5;
   int messagenumber=0;
    Thread autoSendThread = null;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_CONNECTED = 20;
    private int mState = UART_PROFILE_DISCONNECTED;
    private ArrayAdapter<String> listAdapter;
    private BluetoothDevice mDevice = null;
    private  static UartService mService = null;
    private long sendValueNum = 0;
    private long recValueNum = 0;
    List<Signal> messageList=new ArrayList<>();
    Signal message1=new Signal();
    Signal message2=new Signal();
    Signal message3=new Signal();
    Signal message4=new Signal();
    Signal message5=new Signal();
    private Spinner spinnerInterval;
    // 页面
    TextView tv_car_start_check;
    RelativeLayout rl_score_info;
    FrameLayout frameLayout;
    ImageView rightCheckLight;
    ImageView leftCheckLight;
    TranslateAnimation right_translateAnimation;
    TranslateAnimation translateAnimationLeft;
    ImageView carCheckLightBody;
    AnimationDrawable animationDrawableBody;
    ImageView dialog_wharp;
    ImageView dialog_semi_circle;
    ImageView dialog_semi_circle_smill;
    Animation animation_rotate_dialog;
    Animation animation_rotate2_dialog;
    Animation animation_rotate3_dialog;
    FrameLayout dialog_fragment;

    TextView mileage_number;
    TextView oil_number;
    TextView car_number;
    TextView voltage_number;
    TextView vin_number;
    TextView ble_State;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestpermission();
        setContentView(R.layout.car_check);
        initView();
        initDate();
        initAnimation();
        initClick();
        listAdapter = new ArrayAdapter<String>(this, R.layout.ble_message_detail);
        Init_service();// 初始化后台服务
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        dialog_fragment.setVisibility(View.GONE);
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                // 如果选择搜索到的蓝牙设备页面操作成功（即选择远程设备成功，并返回所选择的远程设备地址信息）
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    boolean isconnected = mService.connect(deviceAddress);
                }
                break;
            case REQUEST_ENABLE_BT:
                // 如果请求打开蓝牙页面操作成功（蓝牙成功打开）
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "蓝牙已经成功打开", Toast.LENGTH_SHORT).show();
                } else {
                    // 请求打开蓝牙页面操作不成功（蓝牙为打开或者打开错误）
                    // Log.d(TAG, "蓝牙未打开");
                    System.out.println("蓝牙未打开");
                    Toast.makeText(this, "打开蓝牙时发生错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                System.out.println("wrong request code");
                break;
        }
    }
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e("---------","------msg");
            if(msg.what==1)
            {
                sendBleMessage(index);
                index++;
            }
        }
    };


    private void Init_service() {
        System.out.println("Init_service");
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver,
                makeGattUpdateIntentFilter());
    }

    // UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        // 与UART服务的连接建立
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            System.out.println("uart服务对象：" + mService);
            if (!mService.initialize()) {
                System.out.println("创建蓝牙适配器失败");
                // 因为创建蓝牙适配器失败，导致下面的工作无法进展，所以需要关闭当前uart服务
                finish();
            }
        }

        // 与UART服务的连接失去
        public void onServiceDisconnected(ComponentName classname) {
            // mService.disconnect(mDevice);
            mService = null;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Intent mIntent = intent;
            // 建立连接
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                System.out.println("BroadcastReceiver:ACTION_GATT_CONNECTED");
                ble_State.setText("已建立连接");
                tv_car_start_check.setText("读取");
                toastMessage("设备"+ mDevice.getName()+"连接成功");
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                //btnStart.setEnabled(true);
                listAdapter.add("[" + currentDateTimeString + "] 建立连接: " + mDevice.getName());
                mState = UART_PROFILE_CONNECTED;
            }
            // 断开连接
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                System.out.println("BroadcastReceiver:ACTION_GATT_DISCONNECTED");
                ble_State.setText("已断开连接");
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                ble_State.setText("搜索");
                tv_car_start_check.setText("连接");
                toastMessage("未连接上"+ mDevice.getName()+"，请重新连接");
                listAdapter.add("[" + currentDateTimeString + "] 取消连接: " + mDevice.getName());
                mState = UART_PROFILE_DISCONNECTED;
                mService.close();
            }
            // 有数据可以接收
            if ((action.equals(UartService.ACTION_DATA_AVAILABLE))) {
                byte[] rxValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                String Rx_str =Utils.bytesToHexString(rxValue) ;
                messageList.get(index).setRx_Len(rxValue.length);
                messageList.get(index).setCanRX(rxValue);
                String Rx_value;
                if (messageList.get(index).isASC()==true){
                    Rx_value=messageList.get(index).getVIN();
                }else{
                    double value=messageList.get(index).getSignalvalue();
                    Rx_value=String.valueOf(value)+messageList.get(index).getUnit();
                }


                // 收到发送
                switch (index)
                {

//                    TextView mileage_number;
//                    TextView oil_number;
//                    TextView car_number;
//                    TextView voltage_number;
//                    TextView vin_number;
//                    TextView ble_State;
                    case 0:

                        settingDrawableTop(getApplicationContext(),vin_number,R.mipmap.car_check_scan_right,Rx_value,null);
                    break;
                    case 1:
                        settingDrawableTop(getApplicationContext(),mileage_number,R.mipmap.car_check_scan_right,Rx_value,null);
                        break;
                    case 2:
                        settingDrawableTop(getApplicationContext(),oil_number,R.mipmap.car_check_scan_right,Rx_value,null);
                        break;
                    case 3:
                        settingDrawableTop(getApplicationContext(),voltage_number,R.mipmap.car_check_scan_right,Rx_value,null);
                        break;
                    case 4:
                        settingDrawableTop(getApplicationContext(),car_number,R.mipmap.car_check_scan_right,Rx_value,null);
                        break;

                }
                if(index>4 )  {
                    stopAnimation();
                }else{
                    //handler.sendEmptyMessageAtTime(1,5000);
                    index++;
                    sendBleMessage(index);
                }
                Log.e("----------",""+Rx_str);
            }
            // 未知功能1Rx_str
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            // 未知功能2
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                toastMessage("连接错误设备，请重新连接");
                mService.disconnect();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");
        try {
            // 解注册广播过滤器
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            System.out.println(ignore.toString());
        }
        // 解绑定服务
        unbindService(mServiceConnection);
        // 关闭服务对象
        mService.stopSelf();
        mService = null;
    }

    private void toastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("在MainActivity下按下了back键");
    }

    //检测蓝牙是否开启并打开
    public void checkBleIsConnectPermission()
    {
                if(null==mService)
                    return;
        byte[] bytes=message1.getCanTX();
        //messagenumber=(int) bytes[5];
//        String s1=Utils.bytesToHexString(bytes);
        mService.writeRXCharacteristic(bytes);

//          if (mService.mConnectionState==2){
//
//                byte[] bytes=message1.getCanTX();
//                String s1=Utils.bytesToHexString(bytes);
//                mService.writeRXCharacteristic(bytes);
//                try {
//                    listAdapter.add("[" + DateFormat.getTimeInstance().format(new Date()) + "] 发送0x: " +s1);
//                } catch (Exception e) {
//                    System.out.println(e.toString());
//                }
//        }

    }
    //
    //检测蓝牙是否开启并打开
    public void sendBleMessage(int i)
    {

        if(null==mService){
            toastMessage("连接服务丢失");
        }else{
            byte[] bytes=messageList.get(i).getCanTX();
            mService.writeRXCharacteristic(bytes);
        }

//String s1=Utils.bytesToHexString(bytes);
//          if (mService.mConnectionState==2){
//
//                byte[] bytes=message1.getCanTX();
//                String s1=Utils.bytesToHexString(bytes);
//                mService.writeRXCharacteristic(bytes);
//                try {
//                    listAdapter.add("[" + DateFormat.getTimeInstance().format(new Date()) + "] 发送0x: " +s1);
//                } catch (Exception e) {
//                    System.out.println(e.toString());
//                }
//        }

    }
private void requestpermission(){

    if(ContextCompat.checkSelfPermission(Ble_starActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){//未开启定位权限
        //开启定位权限,200是标识码
        Toast.makeText(Ble_starActivity.this,"请为app开启定位权限",Toast.LENGTH_LONG).show();

        ActivityCompat.requestPermissions(Ble_starActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
    }else{
        Toast.makeText(Ble_starActivity.this,"已开启定位权限",Toast.LENGTH_LONG).show();
    }

}
    //页面初始化代码
    private void initView() {
        tv_car_start_check=findViewById(R.id.tv_car_start_check);
        rl_score_info = findViewById(R.id.rl_score_info);
        frameLayout = findViewById(R.id.dialog);
        carCheckLightBody = findViewById(R.id.iv_control_normal_bg);
        leftCheckLight = (ImageView) findViewById(R.id.iv_scan_left);
        rightCheckLight = (ImageView) findViewById(R.id.iv_scan_right);
        //dialog
        dialog_fragment = findViewById(R.id.dialog);
        dialog_wharp = (ImageView) findViewById(R.id.loding_wharp);
        dialog_semi_circle = (ImageView) findViewById(R.id.loding_b);
        dialog_semi_circle_smill = (ImageView) findViewById(R.id.loding_1);
        //数据
        mileage_number= findViewById(R.id.mileage_txt_number);
        oil_number= findViewById(R.id.car_oil_txt_number);
        car_number=findViewById(R.id.car_txt_modle);
        voltage_number=findViewById(R.id.voltage_txt_number);
        vin_number=findViewById(R.id.vin_txt_number);
        ble_State=findViewById(R.id.lanya);
    }

    public void initAnimation() {

        //左车刷
        translateAnimationLeft = (TranslateAnimation) AnimationUtils.loadAnimation(this, R.anim.anim_guang_shua_left);
        //右车刷
        right_translateAnimation = (TranslateAnimation) AnimationUtils.loadAnimation(this, R.anim.anim_guang_shua_right);
        //dialog
        animation_rotate_dialog = AnimationUtils.loadAnimation(this, R.anim.zheng_rotate_anim);
        animation_rotate2_dialog = AnimationUtils.loadAnimation(this, R.anim.fan_rotate_anim);
        animation_rotate3_dialog = AnimationUtils.loadAnimation(this, R.anim.da_fan_rotate_anim);
    }

    public void startAnimation() {
        leftCheckLight.setVisibility(View.VISIBLE);
        //车身扫描
        carCheckLightBody.setImageResource(R.drawable.car_check_light_body);
        animationDrawableBody = (AnimationDrawable) carCheckLightBody.getDrawable();
        animationDrawableBody.start();
        //车摆动
        leftCheckLight.startAnimation(translateAnimationLeft);
    }
    public void stopAnimation() {
        leftCheckLight.setVisibility(View.GONE);
        //车身扫描
        //carCheckLightBody.setImageResource(R.drawable.car_check_light_body);
        //animationDrawableBody = (AnimationDrawable) carCheckLightBody.getDrawable();
        //animationDrawableBody.start();
        //车摆动
        leftCheckLight.startAnimation(translateAnimationLeft);
    }

    public void initClick() {
        right_translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {


                rightCheckLight.clearAnimation();
                rightCheckLight.setVisibility(View.GONE);

                leftCheckLight.setVisibility(View.VISIBLE);
                leftCheckLight.setAnimation(translateAnimationLeft);
            }
        });

        translateAnimationLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {


            }

            @Override
            public void onAnimationEnd(Animation animation) {


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

                leftCheckLight.clearAnimation();
                leftCheckLight.setVisibility(View.GONE);

                rightCheckLight.setVisibility(View.VISIBLE);
                rightCheckLight.setAnimation(right_translateAnimation);
            }
        });

        //检测
        rl_score_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //首先弹出转圈圈
                dialog_fragment.setVisibility(View.VISIBLE);
                //检车
                showDialog();
                cheCkAndStart();
            }
        });
    }

    public void showDialog() {
        dialog_fragment.setVisibility(View.VISIBLE);
        //开启动画
        dialog_wharp.setAnimation(animation_rotate3_dialog);
        dialog_semi_circle.setAnimation(animation_rotate2_dialog);
        dialog_semi_circle_smill.setAnimation(animation_rotate_dialog);


    }

    public void initDate()
    {
        message1.setCanTX(new byte[] {0x11});
        //message1.setCanTX(new byte[] {0x41,0x50,0x50,0x06,(byte) 0xb4,0x4,(byte) 0xcc,(byte) 0xcc,0xa,0xd});
        message1.setASC(true);
        message1.setStartbyte(2);
        message1.setLength(7);
        message2.setCanTX(new byte[] {0x12});
        //message2.setCanTX(new byte[] {0x41,0x50,0x50,0x06,(byte) 0xb7,0x1,(byte) 0xcc,(byte) 0xcc,0xa,0xd});
        message2.setStartbyte(2);
        message2.setStartbit(0);
        message2.setLength(20);
        message2.setUnit("km");
        message2.setOffset(0);
        message2.setFactor(1);
        message3.setCanTX(new byte[] {0x13});
        //message3.setCanTX(new byte[] {0x41,0x50,0x50,0x03,(byte) 0xb7,0x1,(byte) 0xcc,(byte) 0xcc,0xa,0xd});
        message3.setStartbyte(6);
        message3.setStartbit(0);
        message3.setLength(8);
        message3.setUnit("L");
        message3.setOffset(5);
        message3.setFactor(0.05);
        message4.setCanTX(new byte[] {0x14});
        //message4.setCanTX(new byte[] {0x41,0x50,0x50,0x04,(byte) 0xb7,0x1,(byte) 0xcc,(byte) 0xcc,0xa,0xd});
        message4.setStartbyte(6);
        message4.setStartbit(0);
        message4.setLength(8);
        message4.setUnit("V");
        message4.setOffset(5);
        message4.setFactor(0.05);
        message5.setCanTX(new byte[] {0x15});
        //message5.setCanTX(new byte[] {0x41,0x50,0x50,0x05,(byte) 0xb7,0x1,(byte) 0xcc,(byte) 0xcc,0xa,0xd});
        message5.setStartbyte(6);
        message5.setStartbit(0);
        message5.setLength(8);
        message5.setUnit("km");
        message5.setOffset(5);
        message5.setFactor(0.05);
        messageList.add(message1);
        messageList.add(message2);
        messageList.add(message3);
        messageList.add(message4);
        messageList.add(message5);
        mileage_number.setText("");
        oil_number.setText("");
        car_number.setText("");
        voltage_number.setText("");
        vin_number.setText("");
        //初始话数据扫描动态图
        //settingDrawableTop(getApplicationContext(),vin_number,R.mipmap.car_check_scan_right,"",new Object());


    }

    public  void settingDrawableTop(Context context, TextView view , int resourcesDrawable, String  resourcesString,Object o) {
        view.setVisibility(View.VISIBLE);
        Drawable drawable = null;
        drawable = context.getResources().getDrawable(resourcesDrawable);
        if (o != null) {
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); //设置边界
            view.setCompoundDrawables(null, null, drawable, null);//图在上边
            view.setText(resourcesString);
        }else
        {

            view.setCompoundDrawables(null, null, null, null);//图在上边
            view.setText(resourcesString);

        }
    }

    //跳转页面
    public void cheCkAndStart()
    {
        // 创建一个蓝牙适配器对象
        BluetoothAdapter  mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // 如果未打开蓝牙就弹出提示对话框提示用户打开蓝牙
        if (!mBtAdapter.isEnabled()) {
            toastMessage("对不起，蓝牙还没有打开");
            System.out.println("蓝牙还没有打开");
            // 弹出请求打开蓝牙对话框
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            // 如果已经打开蓝牙则与远程蓝牙设备进行连接
            if(tv_car_start_check.getText().equals("连接")) {
                Intent newIntent = new Intent(Ble_starActivity.this, DeviceListActivity.class);
                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
            }else if(tv_car_start_check.getText().equals("读取"))
            {
                dialog_fragment.setVisibility(View.GONE);
                //checkBleIsConnectPermission();
                if(null==mService) {
                    toastMessage("service丢失");
                }else{
                    index=0;
                    //handler.sendEmptyMessageAtTime(1,5000);
                    sendBleMessage(index);
                    startAnimation();
                }
            }

        }
    }
}
