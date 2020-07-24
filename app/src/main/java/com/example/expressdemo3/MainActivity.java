package com.example.expressdemo3;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.json.JSONObject;

import java.util.ArrayList;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoBeautifyFeature;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class MainActivity extends AppCompatActivity {
    private long appID;
    private String appSign;
    private boolean isTestEnv;
    private String initDomain;

    private ZegoExpressEngine engine;
    private ZegoEngineConfig engineConfig = new ZegoEngineConfig();
    private String userID;
    private String userName;
    private String roomID;
    private String publishStreamID;

    private static int REQUEST_CODE = 666;

    private TextView tvInitDomain;
    private TextView tvAppID;
    private TextView tvTestEnv;
    private boolean publishMicEnable = false;
    private boolean playStreamMute = false;
    private ImageButton ib_local_mic;
    private ImageButton ib_remote_stream_audio;
    private CheckBox cb_play_from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** 申请权限 */
        /** Request permission */
        checkOrRequestPermission();

        ZXingLibrary.initDisplayOpinion(MainActivity.this);

        tvInitDomain = findViewById(R.id.tv_init_domain);
        tvAppID = findViewById(R.id.tv_appID);
        tvTestEnv = findViewById(R.id.tv_env);
        cb_play_from = findViewById(R.id.cb_play_from);

        ib_local_mic = findViewById(R.id.ib_local_mic);
        ib_remote_stream_audio = findViewById(R.id.ib_remote_mic);

        userID = "uID" + System.currentTimeMillis();
        userName = "uName" + System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoExpressEngine.destroyEngine(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void ClickInit(View view) {
        Button button = (Button) view;
        if (button.getText().equals("InitSDK")) {
            if (!initDomain.equals("")) {
                engineConfig.advancedConfig.put("init_domain_name", initDomain);
                if (cb_play_from.isChecked()) {
                    engineConfig.advancedConfig.put("prefer_play_ultra_source", "1");//设置优先从zego udp服务器拉流
                }
                ZegoExpressEngine.setEngineConfig(engineConfig);
            }
            engine = ZegoExpressEngine.createEngine(appID, appSign, isTestEnv, ZegoScenario.COMMUNICATION, getApplication(), null);
            engine.setEventHandler(new IZegoEventHandler() {
                @Override
                public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                    super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
                    System.out.println("onRoomStateUpdate >>>>>  roomID：" + roomID + " state：" + state.toString() + " errorCode：" + errorCode + " extendedData：" + extendedData.toString());
                }

                @Override
                public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                    super.onRoomUserUpdate(roomID, updateType, userList);
                    System.out.println("onRoomUserUpdate >>>>>  roomID：" + roomID + " updateType：" + updateType.toString() + " userList：" + JSON.toJSONString(userList));
                }

                @Override
                public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList) {
                    super.onRoomStreamUpdate(roomID, updateType, streamList);
                    System.out.println("onRoomStreamUpdate >>>>>  roomID：" + roomID + " updateType：" + updateType.toString() + " streamList：" + JSON.toJSONString(streamList));
                    if (updateType == ZegoUpdateType.ADD) {
                        for (ZegoStream e : streamList) {
                            if (e.streamID != publishStreamID) {
                                View remote_view = findViewById(R.id.remote_view);
                                engine.startPlayingStream(e.streamID, new ZegoCanvas(remote_view));
                            }
                        }
                    }
                }

                @Override
                public void onPublisherQualityUpdate(String streamID, ZegoPublishStreamQuality quality) {
                    super.onPublisherQualityUpdate(streamID, quality);
                    System.out.println("onPublisherQualityUpdate >>>>>  streamID：" + streamID + " quality：" + JSON.toJSONString(quality));
                }

                @Override
                public void onPlayerQualityUpdate(String streamID, ZegoPlayStreamQuality quality) {
                    super.onPlayerQualityUpdate(streamID, quality);
                    System.out.println("onPlayerQualityUpdate >>>>>  streamID：" + streamID + " quality：" + JSON.toJSONString(quality));
                }
            });
            System.out.println("ZEGO SDK Version： " + ZegoExpressEngine.getVersion());
            button.setText("release SDK");
        } else {
            /** 销毁引擎 */
            /** Destroy Engine */
            ZegoExpressEngine.destroyEngine(null);
            engine = null;
            button.setText("InitSDK");
        }
    }

    public void ClickLogin(View view) {
        engine.uploadLog();
        if (engine == null) {
            Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
            return;
        }
        Button button = (Button) view;
        if (button.getText().equals("loginRoom")) {
            EditText ed1 = findViewById(R.id.ed_room_id);
            roomID = ed1.getText().toString();
            /** 创建用户对象 */
            /** Create user */
            ZegoUser user = new ZegoUser(userID, userName);
            /** 开始登录房间 */
            /** Begin to login room */
            engine.loginRoom(roomID, user, null);
            button.setText("logoutRoom");
        } else {
            /** 开始退出房间 */
            /** Begin to logout room */
            engine.logoutRoom(roomID);
            button.setText("loginRoom");
        }
    }

    public void ClickPublish(View view) {
        if (engine == null) {
            Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
            return;
        }
        Button button = (Button) view;
        if (button.getText().equals("startPublishing")) {
            EditText et = findViewById(R.id.ed_publish_stream_id);
            publishStreamID = et.getText().toString();
            engine.enableBeautify(ZegoBeautifyFeature.POLISH.value() | ZegoBeautifyFeature.WHITEN.value() | ZegoBeautifyFeature.SHARPEN.value());//开启美颜
            /** 开始预览并设置本地预览视图 */
            /** Start preview and set the local preview view. */
            View local_view = findViewById(R.id.local_view);
            engine.startPreview(new ZegoCanvas(local_view));
            /** 开始推流 */
            /** Begin to publish stream */
            engine.startPublishingStream(publishStreamID);

            button.setText("stopPublishing");
        } else {
            /** 停止本地预览 */
            /** Start stop preview */
            engine.stopPreview();
            /** 停止推流 */
            /** Begin to stop publish stream */
            engine.stopPublishingStream();
            button.setText("startPublishing");
        }
    }

    public void enableLocalMic(View view) {
        if (engine == null) {
            Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
            return;
        }

        publishMicEnable = !publishMicEnable;

        if (publishMicEnable) {
            ib_local_mic.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_on));
        } else {
            ib_local_mic.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_off));
        }

        /* Enable Mic*/
        engine.muteMicrophone(!publishMicEnable);
    }

    public void enableRemoteMic(View view) {
        if (engine == null) {
            Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
            return;
        }

        playStreamMute = !playStreamMute;

        if (playStreamMute) {
            ib_remote_stream_audio.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_off));
        } else {
            ib_remote_stream_audio.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_on));
        }

        /* Enable Mic*/
        engine.muteSpeaker(playStreamMute);
    }

    public void ClickScan(View view) {
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /** 校验并请求权限 */
    /**
     * Check and request permission
     */
    public boolean checkOrRequestPermission() {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, 101);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    Toast.makeText(this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                    try {
                        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result).getJSONObject("data");
                        initDomain = jsonObject.getString("title");
                        appID = jsonObject.getLong("appid");
                        appSign = jsonObject.getString("appkey");
                        isTestEnv = jsonObject.getBoolean("testEnvironment");

                        tvInitDomain.setText("init_domain: " + initDomain);
                        tvAppID.setText("appID: " + appID);
                        tvTestEnv.setText("TestEnv: " + isTestEnv);
                    }catch (Exception e){
                        System.out.println(e);
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}