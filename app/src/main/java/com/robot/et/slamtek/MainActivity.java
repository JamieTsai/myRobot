package com.robot.et.slamtek;

import java.util.Vector;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.robot.et.slamtek.base.BaseActivity;
import com.robot.et.slamtek.fragment.MapFragment;
import com.slamtec.slamware.action.ActionStatus;
import com.slamtec.slamware.robot.LaserPoint;
import com.slamtec.slamware.robot.LaserScan;
import com.slamtec.slamware.robot.Location;
import com.slamtec.slamware.robot.Pose;
import com.slamtec.slamware.discovery.DeviceManager;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import com.slamtec.slamware.action.IAction;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import okhttp3.Call;
import okhttp3.Response;

import static android.support.v7.widget.StaggeredGridLayoutManager.TAG;

public class MainActivity extends BaseActivity {

    @BindView(R.id.forward) Button forward;
    @BindView(R.id.backward) Button backward;
    @BindView(R.id.left) Button left;
    @BindView(R.id.right) Button right;
    @BindView(R.id.robotLocation) Button robotlocation;
    @BindView(R.id.stop) Button stop;
    @BindView(R.id.execTurn) Button execTurn;
    @BindView(R.id.moveGoal) Button moveGoal;
    @BindView(R.id.battery) TextView battery;
    @BindView(R.id.quality) TextView quality;
    @BindView(R.id.charing) TextView charing;

    @BindView(R.id.location) TextView location;
    @BindView(R.id.angle) EditText angle;
    @BindView(R.id.goalx) EditText goalX;
    @BindView(R.id.goaly) EditText goalY;

    private static final int MOVEDIRECTION_FORWARD = 1;
    private static final int MOVEDIRECTION_BACKWARD = 2;
    private static final int MOVEDIRECTION_TURN_LEFT = 3;
    private static final int MOVEDIRECTION_TURN_RIGHT = 4;
    private static final int MOVEDIRECTION_CANCEL = 5;

    static {
        Log.e("MainActivity", "load library");
        System.loadLibrary("rpsdk");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initRobotState();
        initMapFragment();

//右转
        right.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

              int   ation=event.getAction();
                switch (ation){
                    case MotionEvent.ACTION_DOWN:
                        SlamtecLoader.getInstance().execBasicMove(MOVEDIRECTION_TURN_RIGHT);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;

                }

                return false;
            }
        });

//左转
        left.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int   ation=event.getAction();
                switch (ation){
                    case MotionEvent.ACTION_DOWN:
                        SlamtecLoader.getInstance().execBasicMove(MOVEDIRECTION_TURN_LEFT);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;

                }

                return false;
            }
        });
//前进
        forward.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int   ation=event.getAction();
                switch (ation){
                    case MotionEvent.ACTION_DOWN:
                        SlamtecLoader.getInstance().execBasicMove(MOVEDIRECTION_FORWARD);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;

                }

                return false;
            }
        });
//后退
        backward.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int   ation=event.getAction();
                switch (ation){
                    case MotionEvent.ACTION_DOWN:
                        SlamtecLoader.getInstance().execBasicMove(MOVEDIRECTION_BACKWARD);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;

                }

                return false;
            }
        });





    }

    private void initRobotState(){
        quality.setText("quality:"+SlamtecLoader.getInstance().getLocalizationQuality());

        battery.setText("BatteryPercent:" + SlamtecLoader.getInstance().getBatteryPrecent());
        charing.setText("IsCharing:"+SlamtecLoader.getInstance().getCharingState());//是否充电，为布尔值


    }

    private void initMapFragment(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        MapFragment fragment = new MapFragment();
        transaction.add(R.id.container,fragment);
        transaction.commit();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    @OnClick(R.id.execTurn)
    public void execTurn() {
//        String data=angle.getText().toString().trim();
//        if (TextUtils.equals("",data)){
//            return;
//        }
//        SlamtecLoader.getInstance().execBasicRotate((int) Math.toRadians(Double.valueOf(data)));
        Pose pose = SlamtecLoader.getInstance().getCurrentRobotPose();
      if(!"".equals(angle.getText().toString()))
      {
        try {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("y", pose.getY());
        jsonObject.put("x", pose.getX());
        jsonObject.put("yaw", pose.getYaw());
        byte[] input = angle.getText().toString().getBytes("UTF-8");
        String base64 = Base64.encodeToString(input, Base64.DEFAULT);
        jsonObject.put("name", base64);
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("pose", jsonObject);
        final String sendStr = jsonObject1.toString();
        OkGo.post("http://192.168.4.54:8090/ros/add_pose")
                .tag(this)
                .upString(sendStr)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        //上传成功
                        Log.e("++++", sendStr);
                    }

                    @Override
                    public void upProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
                        //这里回调上传进度(该回调在主线程,可以直接更新ui)
                    }
                });
    } catch (Exception e) {
        Log.e("TAG", "Error in Writing: " + e.getLocalizedMessage());
       }
    }else{
          Toast.makeText(MainActivity.this,"请先输入相应位置",Toast.LENGTH_SHORT).show();
      }

    }

    @OnClick(R.id.robotLocation)
    public void showRobotLocation(){
        location.setText("机器人当前位置："+"("+slamwareCorePlatform.getLocation().getX()+","+slamwareCorePlatform.getLocation().getY()+")");
    }

    @OnClick(R.id.moveGoal)
    public void execMoveGoal(){
        String dataX = goalX.getText().toString().trim();
        String dataY = goalY.getText().toString().trim();
        if(!("".equals(dataX))&&!("".equals(dataY))) {
            SlamtecLoader.getInstance().execSetGoal(Float.valueOf(dataX), Float.valueOf(dataY));
        }else{
            Toast.makeText(MainActivity.this,"请先输入X和Y坐标值",Toast.LENGTH_SHORT).show();
        }
    }
//    @OnClick({R.id.forward,R.id.backward,R.id.left})
//    public void execForward(View view){
//        switch (view.getId()) {
//            case R.id.forward:
//                SlamtecLoader.getInstance().execBasicMove(MOVEDIRECTION_FORWARD);
//                break;
//            case R.id.backward:
//                SlamtecLoader.getInstance().execBasicMove(MOVEDIRECTION_BACKWARD);
//                break;
//            case R.id.left:
//                SlamtecLoader.getInstance().execBasicMove(MOVEDIRECTION_TURN_LEFT);
//                break; 
////            case R.id.right:
////                SlamtecLoader.getInstance().execBasicMove(MOVEDIRECTION_TURN_RIGHT);
////                break;
//            default:
//                SlamtecLoader.getInstance().execBasicMove(MOVEDIRECTION_CANCEL);
//                break;
//        }
//    }



    @OnClick(R.id.stop)
    public void execTest() {

        slamwareCorePlatform.getCurrentAction().cancel();

        Log.e("test", "=====================execTest=====================");
        Log.e("RobotStatus", "===================RobotStatus==================");
        Log.e("RobotStatus", "IsCharging:" + slamwareCorePlatform.getBatteryIsCharging());
        Log.e("RobotStatus", "BatteryPercent:" + slamwareCorePlatform.getBatteryPercentage());
        Log.e("RobotStatus", "DCISConnected:" + slamwareCorePlatform.getDCIsConnected());
        Log.e("RobotStatus", "SlamwareVersion" + slamwareCorePlatform.getSlamwareVersion());
        Log.e("RobotStatus", "SDKVersion:" + slamwareCorePlatform.getSDKVersion());
        Log.e("RobotStatus", "================================================");

        Location location = slamwareCorePlatform.getLocation();
        float locationX = location.getX();
        float locationY = location.getY();
        float locationZ = location.getZ();
        Log.e("location", "location:(" + locationX + "," + locationY + "," + locationZ + ")");

        Log.e("mapLocalization", "mapLocalization:" + slamwareCorePlatform.getMapLocalization());
        Log.e("RobotStatus", "===================RobotStatus==================");
        Log.e("RobotStatus", "IsCharging:" + slamwareCorePlatform.getBatteryIsCharging());
        Log.e("RobotStatus", "BatteryPercent:" + slamwareCorePlatform.getBatteryPercentage());
        Log.e("RobotStatus", "DCISConnected:" + slamwareCorePlatform.getDCIsConnected());
        Log.e("RobotStatus", "SlamwareVersion" + slamwareCorePlatform.getSlamwareVersion());
        Log.e("RobotStatus", "SDKVersion:" + slamwareCorePlatform.getSDKVersion());
        Log.e("RobotStatus", "================================================");

        Pose robotPose = slamwareCorePlatform.getPose();
        LaserScan laserScan = slamwareCorePlatform.getLaserScan();
        Vector<LaserPoint> Vpoint = laserScan.getLaserPoints();
        for (int i = 0; i < Vpoint.size(); i++) {
            Log.e("LaserPoint", "Distance:" + Vpoint.get(i).getDistance() + ",angle:" + Vpoint.get(i).getAngle());
        }
    }
}
