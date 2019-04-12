package zk.obd;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import zk.obd.adapter.DividerGridItemDecoration;
import zk.obd.adapter.InformationCarAdapter;
import zk.obd.been.CarInformationBean;

public class MainActivity extends Activity {
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
    private RecyclerView mRecycleview;

    List carDataList = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_check);
        initView();
        initDate();
        initAnimation();
        initClick();
//        showDialog();

    }

    private void initView() {
        mRecycleview=findViewById(R.id.car_list_information);
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
//         voltage_number=findViewById(R.id.voltage_txt_number);
//         vin_number=findViewById(R.id.vin_txt_number);
//         ble_State=findViewById(R.id.lanya);
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
                dialog_fragment.setVisibility(View.GONE);
                //检车
                startAnimation();
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
        Toast.makeText(getApplicationContext()," kakak",Toast.LENGTH_SHORT).show();
        //模擬車輛信息數據
        carDataList.add(new CarInformationBean("特斯拉","42143321432"));
        carDataList.add(new CarInformationBean("勞斯萊斯","8943284932"));
        carDataList.add(new CarInformationBean("蘭博基尼","4523543545"));

//         mileage_number.setText("");
//         oil_number.setText("");
//         car_number.setText("");
//         voltage_number.setText("");
//         vin_number.setText("");
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
        mRecycleview.setAdapter(new InformationCarAdapter(carDataList));
        //设置分隔线
        mRecycleview.addItemDecoration( new DividerGridItemDecoration(this ));
//设置增加或删除条目的动画
        mRecycleview.setItemAnimator( new DefaultItemAnimator());


    }

    public  void settingDrawableTop(Context context, TextView view , int resourcesDrawable, String  resourcesString) {
        view.setVisibility(View.VISIBLE);
        Drawable drawable = null;
        drawable = context.getResources().getDrawable(resourcesDrawable);
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); //设置边界
            view.setCompoundDrawables(null, null, drawable, null);//图在上边
            view.setText(resourcesString);
        }
    }
}
