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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import zk.obd.R;
import zk.obd.adapter.DividerGridItemDecoration;
import zk.obd.adapter.InformationCarAdapter;
import zk.obd.been.CarInformationBean;

//test main
//second
public class Ble_starActivity extends Activity {
    public final static int DELAY = 2000;
    public static final int EXTERNAL_STORAGE_REQ_CODE = 10 ;
    int indexsend=0;
    int indexreceive=0;
    private static final int TASKTIMES=3;
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
   // List<Signal> messageList=new ArrayList<>();
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
    private RadioButton radioMQB,radioPQ,radioGM;
    RadioGroup Platform;
    private RecyclerView mRecycleview;
    private InformationCarAdapter informationCarAdapter;
    private Button cancle_mode;

    private Button set_mode;
    List<CarInformationBean> carDataList = new ArrayList();
//    TextView mileage_number;
//    TextView oil_number;
//    TextView car_number;
//    TextView voltage_number;
//    TextView vin_number;

    CarInformationBean SigVIN=new CarInformationBean("VIN","车辆识别号");
    CarInformationBean SigODO=new CarInformationBean("ODO","里程");
    CarInformationBean SigTank=new CarInformationBean("Tank","油量");
    CarInformationBean SigSetTRA=new CarInformationBean("TRA","运输模式");
    CarInformationBean SigCancelTRA=new CarInformationBean("UnTRA","非运输模式");

    TextView ble_State;
    private boolean isRunCloase=false;
    Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==9)
            {
                isRunCloase=false;

            }
        }
    };
    Runnable runnable;

    {
        runnable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                //要做的事情，这里再次调用此Runnable对象，以实现每两秒实现一次的定时器操作
                int i = 0;
                while (i < carDataList.size()) {
                    if (carDataList.get(i).isActive()) {
                        carDataList.get(i).setActive(false);
                        break;
                    }
                    i++;
                }
                int j = i + 1;
                int k = j % carDataList.size();
                boolean sendreturn = false;
                while (j < carDataList.size() + i + 1) {
                    k = j % carDataList.size();
                    if (!carDataList.get(k).isReceived() && carDataList.get(k).getSendtimes() < TASKTIMES) {
                        carDataList.get(k).setActive(true);
                        sendreturn = sendBleMessage(carDataList.get(k));
                        carDataList.get(k).setSendtimes(carDataList.get(k).getSendtimes() + 1);
                        handler.postDelayed(this, DELAY);
                        break;
                    }
                    j++;
                }
                if (!sendreturn || j == (carDataList.size() + i + 1)) {
                    boolean allreceived = true;
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String record = df.format(new Date());
                    for (int m = 0; m < carDataList.size(); m++) {
                        allreceived &= carDataList.get(m).isReceived();
                        record += "," + carDataList.get(m).getASCValue();
                    }
                    ;
                    if (allreceived) {
                        toastMessage("完成读取");
                        File file = new File("/sdcard/vin.csv");
                        if(!file .exists())addTxtToFileWrite(file, "读取时间,车辆识别号,车辆油量,车辆里程");
                        //addTxtToFileBuffered(file,"追加的内容");
                        addTxtToFileWrite(file, record);
                        file = new File("/sdcard/vin.txt");
                        if(!file .exists())addTxtToFileWrite(file, "读取时间,车辆识别号,车辆油量,车辆里程");
                        //addTxtToFileBuffered(file,"追加的内容");
                        addTxtToFileWrite(file, record);
                    } else {

                        toastMessage("未完全读取");
                    }
                    handler.removeCallbacks(this);
                    stopAnimation();

                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestpermission();
        setContentView(R.layout.car_check);
        initView();
        initData();
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



    private void addTxtToFileWrite(File file, String content) {
        FileWriter writer = null;
        try {
            //FileWriter(file, true),第二个参数为true是追加内容，false是覆盖
            writer = new FileWriter(file, true);
            writer.write(content);//换行.write(content);
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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
                String Rx_str =new String(rxValue) ;
                int i=0;
                if(carDataList.get(i).isActive() && Rx_str.toUpperCase().indexOf("_V")>0) {
                    carDataList.get(i).setReceived(true);
                    carDataList.get(i).setRx_Len(rxValue.length-2);
                    carDataList.get(i).setCanRX(rxValue);
                    carDataList.get(i).getASCValue();

                }
                i++;
                while(i<carDataList.size()){
                    if(carDataList.get(i).isActive() && Rx_str.toUpperCase().indexOf("_"+carDataList.get(i).getUnit().toUpperCase())>0 ) {
                        carDataList.get(i).setReceived(true);
                        carDataList.get(i).setRx_Len(rxValue.length);
                        carDataList.get(i).setCanRX(rxValue);
                        carDataList.get(i).getASCValue();
                        break;
                    }
                    i++;
                }
                informationCarAdapter.notifyDataSetChanged();
                toastMessage(Rx_str);
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
    //
    //检测蓝牙是否开启并打开
    public boolean sendBleMessage(CarInformationBean sig)
    {

        if(null!=mService && mService.mConnectionState==2){
            byte[] bytes=sig.getCanTX();
            mService.writeRXCharacteristic(bytes);
            return true;
        }else{
            //stopAnimation();
            toastMessage("请连接蓝牙，服务丢失");
            return false;
        }
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

    int permission = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE);

    if (permission != PackageManager.PERMISSION_GRANTED) {
        // 请求权限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                EXTERNAL_STORAGE_REQ_CODE);
    }

}
    //页面初始化代码
    private void initView() {
        cancle_mode=findViewById(R.id.cancle_mode);
        set_mode=findViewById(R.id.set_mode);
        mRecycleview=findViewById(R.id.car_list_information);
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
//        mileage_number= findViewById(R.id.mileage_txt_number);
//        oil_number= findViewById(R.id.car_oil_txt_number);
//        car_number=findViewById(R.id.car_txt_modle);
//        voltage_number=findViewById(R.id.voltage_txt_number);
//        vin_number=findViewById(R.id.vin_txt_number);
        ble_State=findViewById(R.id.lanya);
        radioMQB= (RadioButton) findViewById(R.id.radio_MQB);
        radioPQ = (RadioButton) findViewById(R.id.radio_PQ);
        radioGM = (RadioButton) findViewById(R.id.radio_GM);
        Platform = (RadioGroup) findViewById(R.id.platform);
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
        rightCheckLight.clearAnimation();
        leftCheckLight.clearAnimation();

        leftCheckLight.setVisibility(View.GONE);
        rightCheckLight.setVisibility(View.GONE);
        handler.sendEmptyMessage(9);
        //车身扫描
        //animationDrawableBody = (AnimationDrawable) carCheckLightBody.getDrawable();
        if(animationDrawableBody!=null) {
            animationDrawableBody.stop();
            carCheckLightBody.setImageResource(R.mipmap.control_car_normal_bg);
        }
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
        radioMQB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toastMessage("MQB 平台");
                updateData();
                //informationCarAdapter.notifyDataSetChanged();
            }
        });
        radioPQ.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toastMessage("PQ 平台");
                updateData();
                //informationCarAdapter.notifyDataSetChanged();
            }
        });
        radioGM.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toastMessage("GM 平台");
                updateData();
                //informationCarAdapter.notifyDataSetChanged();
            }
        });

        //設置運輸模式
        cancle_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendBleMessage(SigCancelTRA))toastMessage("取消运算模式");;
            }
        });
        //取消設置運輸模式
        set_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendBleMessage(SigSetTRA))toastMessage("设置运算模式");;
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

    public void initData()
    {
        //Toast.makeText(getApplicationContext()," kakak",Toast.LENGTH_SHORT).show();
        //模擬車輛信息數據
        SigVIN.setASC(true);
        SigVIN.setStartbyte(2);
        SigVIN.setLength(7);


        SigODO.setASC(true);
        SigODO.setStartbyte(2);
        SigODO.setStartbit(0);
        SigODO.setLength(20);
        SigODO.setUnit("km");
        SigODO.setOffset(0);
        SigODO.setFactor(1);

        SigTank.setASC(true);
        SigTank.setStartbyte(6);
        SigTank.setStartbit(0);
        SigTank.setLength(8);
        SigTank.setUnit("L");
        SigTank.setOffset(5);
        SigTank.setFactor(0.05);

        carDataList.add(SigVIN);
        carDataList.add(SigTank);
        carDataList.add(SigODO);
        updateData();

        //初始话数据扫描动态图
//        settingDrawableTop(getApplicationContext(),vin_number,R.mipmap.car_check_scan_right,"");

        //車輛信息列表
        LinearLayoutManager layoutManager = new LinearLayoutManager(this );
//设置布局管理器
        mRecycleview.setLayoutManager(layoutManager);
//设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper. VERTICAL);
//设置Adapter
        Log.e("-------","-----------mRecycleview");
        informationCarAdapter=new InformationCarAdapter(carDataList);
        mRecycleview.setAdapter(informationCarAdapter);
        //设置分隔线
        mRecycleview.addItemDecoration( new DividerGridItemDecoration(this ));
//设置增加或删除条目的动画
        mRecycleview.setItemAnimator( new DefaultItemAnimator());




        //初始话数据扫描动态图
        //settingDrawableTop(getApplicationContext(),vin_number,R.mipmap.car_check_scan_right,"",new Object());


    }
    public void updateData()
    {
        if (radioMQB.isChecked()){
            carDataList.get(0).setCanTX("a".getBytes());
            carDataList.get(1).setCanTX("b".getBytes());
            carDataList.get(2).setCanTX("c".getBytes());
            SigSetTRA.setCanTX("d".getBytes());
            SigCancelTRA.setCanTX("e".getBytes());
        }
        if (radioPQ.isChecked()){
            carDataList.get(0).setCanTX("f".getBytes());
            carDataList.get(1).setCanTX("g".getBytes());
            carDataList.get(2).setCanTX("h".getBytes());
            SigSetTRA.setCanTX("i".getBytes());
            SigCancelTRA.setCanTX("j".getBytes());
        }
        if (radioGM.isChecked()){
            carDataList.get(0).setCanTX("k".getBytes());
            carDataList.get(1).setCanTX("l".getBytes());
            carDataList.get(2).setCanTX("m".getBytes());
            SigSetTRA.setCanTX("n".getBytes());
            SigCancelTRA.setCanTX("o".getBytes());
        }
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
                if(isRunCloase)
                {
                    return;
                }
                isRunCloase=true;
                //checkBleIsConnectPermission();
                if(null==mService) {
                    toastMessage("蓝牙服务丢失");
                }else{
                    handler.removeCallbacks(runnable);
                    for(int i=0;i<carDataList.size();i++){
                        carDataList.get(i).setReceived(false);
                        carDataList.get(i).setActive(false);
                        carDataList.get(i).setSendtimes(0);
                        carDataList.get(i).setRx_Len(0);
                    }
                    carDataList.get(0).setActive(true);
                    sendBleMessage(carDataList.get(0));
                    carDataList.get(0).setSendtimes(1);
                    handler.postDelayed(runnable, DELAY);
                    startAnimation();
                    informationCarAdapter.notifyDataSetChanged();
                }
            }

        }
    }


}
