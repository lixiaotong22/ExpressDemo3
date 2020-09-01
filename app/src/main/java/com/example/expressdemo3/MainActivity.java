package com.example.expressdemo3;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoBeautifyFeature;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
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

    public static ArrayList<String> listLog = new ArrayList<>();
    private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** 申请权限 */
        /** Request permission */
        checkOrRequestPermission();

        /** 二维码扫描库 */
        ZXingLibrary.initDisplayOpinion(MainActivity.this);

        tvInitDomain = findViewById(R.id.tv_init_domain);
        tvAppID = findViewById(R.id.tv_appID);
        tvTestEnv = findViewById(R.id.tv_env);
        cb_play_from = findViewById(R.id.cb_play_from);

        ib_local_mic = findViewById(R.id.ib_local_mic);
        ib_remote_stream_audio = findViewById(R.id.ib_remote_mic);

        userID = "uID" + System.currentTimeMillis();
        userName = "uName" + System.currentTimeMillis();

        Log.i("ExpressDemo", "ZEGO SDK Version： " + ZegoExpressEngine.getVersion());
        listLog.add(format.format(new Date()) + "  ZEGO SDK Version： " + ZegoExpressEngine.getVersion() + "\n");
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
        if (!hasData()) {
            Toast.makeText(MainActivity.this, "please scan QR code to settting", Toast.LENGTH_SHORT).show();
            return;
        }
        Boolean isOpenAdvanced = false;
        Button button = (Button) view;
        if (button.getText().equals("InitSDK")) {
            if (!initDomain.equals("")) {
                Log.i("ExpressDemo", "set init_domain_name: " + initDomain);
                listLog.add(format.format(new Date()) + " : set init_domain_name\n");
                engineConfig.advancedConfig.put("init_domain_name", initDomain);//设置隔离域名
                isOpenAdvanced = true;
            }
            if (cb_play_from.isChecked()) {
                Log.i("ExpressDemo", "setting from UDP Server to PlayingStream");
                listLog.add(format.format(new Date()) + " : set from UDP Server to PlayingStream \n");
                engineConfig.advancedConfig.put("prefer_play_ultra_source", "1");//设置优先从zego udp服务器拉流
                isOpenAdvanced = true;
            }
            if (isOpenAdvanced) {
                ZegoExpressEngine.setEngineConfig(engineConfig);
            }

            engine = ZegoExpressEngine.createEngine(appID, appSign, isTestEnv, ZegoScenario.COMMUNICATION, getApplication(), null);
            engine.setEventHandler(new IZegoEventHandler() {
                @Override
                public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                    super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
                    Log.i("ExpressDemo", "onRoomStateUpdate >>>>>  roomID：" + roomID + " state：" + state.toString() + " errorCode：" + errorCode + " extendedData：" + extendedData.toString());
                    listLog.add(format.format(new Date()) + " : onRoomStateUpdate > roomID：" + roomID + " state：" + state.toString() + " errorCode：" + errorCode + " extendedData：" + extendedData.toString() + "\n");
                }

                @Override
                public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                    super.onRoomUserUpdate(roomID, updateType, userList);
                    Log.i("ExpressDemo", "onRoomUserUpdate >>>>>  roomID：" + roomID + " updateType：" + updateType.toString() + " userList：" + JSON.toJSONString(userList));
                    listLog.add(format.format(new Date()) + " : onRoomUserUpdate > roomID：" + roomID + " updateType：" + updateType.toString() + " userList：" + JSON.toJSONString(userList) + "\n");
                }

                @Override
                public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList) {
                    super.onRoomStreamUpdate(roomID, updateType, streamList);
                    Log.i("ExpressDemo", "onRoomStreamUpdate >>>>>  roomID：" + roomID + " updateType：" + updateType.toString() + " streamList：" + JSON.toJSONString(streamList));
                    listLog.add(format.format(new Date()) + " : onRoomStreamUpdate > roomID：" + roomID + " updateType：" + updateType.toString() + " streamList：" + JSON.toJSONString(streamList) + "\n");
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
                    Log.i("ExpressDemo", "onPublisherQualityUpdate >>>>>  streamID：" + streamID + " quality：" + JSON.toJSONString(quality));
                    listLog.add(format.format(new Date()) + " : onPublisherQualityUpdate > streamID：" + streamID + " quality：" + JSON.toJSONString(quality) + "\n");
                }

                @Override
                public void onPlayerQualityUpdate(String streamID, ZegoPlayStreamQuality quality) {
                    super.onPlayerQualityUpdate(streamID, quality);
                    Log.i("ExpressDemo", "onPlayerQualityUpdate >>>>>  streamID：" + streamID + " quality：" + JSON.toJSONString(quality));
                    listLog.add(format.format(new Date()) + " : onPlayerQualityUpdate > streamID：" + streamID + " quality：" + JSON.toJSONString(quality) + "\n");
                }

                @Override
                public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                    super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                    Log.i("ExpressDemo", "onPlayerStateUpdate >>>>>  streamID：" + streamID + " state：" + state + " errorCode：" + errorCode + " extendedData：" + JSON.toJSONString(extendedData));
                    listLog.add(format.format(new Date()) + " : onPlayerStateUpdate > streamID：" + streamID + " state：" + state + " errorCode：" + errorCode + " extendedData：" + JSON.toJSONString(extendedData) + "\n");
                }

                @Override
                public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                    super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                    Log.i("ExpressDemo", "onPublisherStateUpdate >>>>>  streamID：" + streamID + " state：" + state + " errorCode：" + errorCode + " extendedData：" + JSON.toJSONString(extendedData));
                    listLog.add(format.format(new Date()) + " : onPublisherStateUpdate > streamID：" + streamID + " state：" + state + " errorCode：" + errorCode + " extendedData：" + JSON.toJSONString(extendedData) + "\n");
                }
            });
            button.setText("release SDK");
        } else {
            /** 销毁引擎 */
            /** Destroy Engine */
            ZegoExpressEngine.destroyEngine(null);
            engine = null;
            isOpenAdvanced = false;
            button.setText("InitSDK");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void ClickLogin(View view) {
        if (engine == null) {
            Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
            return;
        }
        Button button = (Button) view;
        if (button.getText().equals("loginRoom")) {
            EditText ed1 = findViewById(R.id.ed_room_id);
            roomID = ed1.getText().toString();
            if (roomID.equals("")) {
                roomID = String.valueOf((int) (Math.random() * 988 + 11));
                ed1.setText(roomID);
            }

            /** 创建用户对象 */
            /** Create user */
            ZegoUser user = new ZegoUser(userID, userName);

            Log.i("ExpressDemo", "loginRoom： roomID:" + roomID + "  userID:" + userID + "  userName:" + userName);//打印信息
            listLog.add(format.format(new Date()) + ":loginRoom:roomID:" + roomID + "  userID:" + userID + "  userName:" + userName + "\n");

            try {
                Long timestamp = System.currentTimeMillis() / 1000 + 3600;
                byte[] mServerSecret = "033aa2093156a2d20f5812a56f50f37a".getBytes(); // Secret联系zego技术支持

                JSONObject encryptResult = new JSONObject();
                encryptResult.put("app_id", 1753546899); // 数值型, appid联系zego技术支持
                encryptResult.put("timeout", timestamp); // 数值型, 注意必须是当前时间戳(秒)加超时时间(秒)
                encryptResult.put("nonce", 11111111); // 随机数,须为数值型
                encryptResult.put("id_name", user.userID);// 字符串,id_name必须跟setUser的userid相同
                byte[] encryptByte = BuildThirdToken.encrypt(encryptResult.toString(), mServerSecret);

                String encryptContent = "01" + new String(encryptByte, "utf-8");//最后结果须加version("01")为前缀
                System.out.println(encryptContent);

                /** 开始登录房间 */
                /** Begin to login room */
                ZegoRoomConfig roomConfig = new ZegoRoomConfig();
                roomConfig.token = encryptContent;
                engine.loginRoom(roomID, user, roomConfig);
                button.setText("logoutRoom");
            } catch (Exception e) {
                System.out.println(e);
            }

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
            EditText et2 = findViewById(R.id.ed_publish_stream_id);
            publishStreamID = et2.getText().toString();
            if (publishStreamID.equals("")) {
                publishStreamID = String.valueOf((int) (Math.random() * 999 + 1000));
                et2.setText(publishStreamID);
            }

            engine.enableBeautify(ZegoBeautifyFeature.POLISH.value() | ZegoBeautifyFeature.WHITEN.value() | ZegoBeautifyFeature.SHARPEN.value());//开启美颜
            /** 开始预览并设置本地预览视图 */
            /** Start preview and set the local preview view. */
            View local_view = findViewById(R.id.local_view);
            engine.startPreview(new ZegoCanvas(local_view));

            Log.i("ExpressDemo", "startPublishingStream >  publishStreamID:" + publishStreamID);
            listLog.add(format.format(new Date()) + ":startPublishingStream > publishStreamID:" + publishStreamID + "\n");

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
        /** 处理二维码扫描结果 */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    try {
                        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result).getJSONObject("data");
                        initDomain = jsonObject.getString("title");
                        appID = jsonObject.getLong("appid");
                        appSign = jsonObject.getString("appkey");
                        isTestEnv = jsonObject.getBoolean("testEnvironment");

                        tvInitDomain.setText("init_domain: " + initDomain);
                        tvAppID.setText("appID: " + appID);
                        tvTestEnv.setText("TestEnv: " + isTestEnv);
                        Toast.makeText(MainActivity.this, "Scan the QR code successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("ExpressDemo", "JSON parse failure : " + e.toString());
                        Toast.makeText(MainActivity.this, "JSON parse failure", Toast.LENGTH_SHORT).show();
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(MainActivity.this, "Scan the QR code failure", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public boolean hasData() {
        if (appID != 0 && !appSign.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    public void ClickLog(View view) {
        //ArrayList<String> list = new ArrayList<>(listLog);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("list", listLog);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setClass(MainActivity.this, LogActivity.class);    //设置Intent属性
        MainActivity.this.startActivity(intent);    //跳转
    }


}

