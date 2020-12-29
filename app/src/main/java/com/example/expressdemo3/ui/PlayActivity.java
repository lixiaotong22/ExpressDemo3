package com.example.expressdemo3.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.expressdemo3.AppConfig;
import com.example.expressdemo3.R;
import com.example.expressdemo3.util.BuildThirdToken;

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
import im.zego.zegoexpress.constants.ZegoStreamResourceMode;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCDNConfig;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class PlayActivity extends AppCompatActivity {
    private AppConfig appConfig;

    private ZegoExpressEngine engine;
    private String userID;
    private String userName;
    private String roomID;
    private String remoteStreamID;
    private String playCdnURL;

    private boolean playStreamMute;
    private ImageButton ib_play_mic;

    private ArrayList<String> listLog;
    private SimpleDateFormat format;

    private Button btn_log;
    private Button btn_init;
    private Button btn_login;
    private Button btn_play;
    private Button btn_play_url_cdn;
    private TextView tv_p2p_Delay;
    private EditText ed_stream_id;
    private EditText ed_play_cdn_url;

    private ZegoStreamResourceMode resourceMode;
    private ZegoPlayerConfig zegoPlayerConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_play);

        setView();
        setData();
        setEventListen();

        Log.i("ExpressDemo", "ZEGO SDK Version： " + ZegoExpressEngine.getVersion());
        listLog.add(format.format(new Date()) + "  ZEGO SDK Version： " + ZegoExpressEngine.getVersion() + "\n");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoExpressEngine.destroyEngine(null);
    }

    private void setView() {
        ib_play_mic = findViewById(R.id.ib_play_mic);

        btn_log = findViewById(R.id.btn_log);
        btn_init = findViewById(R.id.btn_init);
        btn_login = findViewById(R.id.btn_login);
        btn_play = findViewById(R.id.btn_play);
        btn_play_url_cdn = findViewById(R.id.btn_play_url_cdn);
        tv_p2p_Delay = findViewById(R.id.tv_p2p_Delay);
        ed_stream_id = findViewById(R.id.ed_stream_id);
        ed_play_cdn_url = findViewById(R.id.ed_play_cdn_url);
    }

    private void setData() {
        appConfig = AppConfig.getInstance();
        playStreamMute = false;
        listLog = new ArrayList<>();
        format = new SimpleDateFormat("HH:mm:ss");

        userID = "uID" + System.currentTimeMillis();
        userName = "uName" + System.currentTimeMillis();

        zegoPlayerConfig = new ZegoPlayerConfig();
        resourceMode = ZegoStreamResourceMode.DEFAULT;
    }

    private void setEventListen() {
        ib_play_mic.setOnClickListener(view -> {
            if (engine == null) {
                Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
                return;
            }
            playStreamMute = !playStreamMute;
            if (playStreamMute)
                ib_play_mic.setBackgroundResource(R.drawable.ic_bottom_microphone_off);
            else
                ib_play_mic.setBackgroundResource(R.drawable.ic_bottom_microphone_on);
            /* Enable Mic*/
            engine.muteSpeaker(playStreamMute);
        });
        btn_log.setOnClickListener(view -> {  //跳转
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("list", listLog);
            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClass(PlayActivity.this, LogActivity.class);
            PlayActivity.this.startActivity(intent);
        });
        btn_init.setOnClickListener(view -> {
            Button button = (Button) view;
            if (button.getText().equals("初始化SDK")) {
                if (appConfig.isOpenAdvanced()) {
                    ZegoEngineConfig engineConfig = new ZegoEngineConfig();
                    if (!appConfig.getInitDomain().equals(""))
                        engineConfig.advancedConfig.put("init_domain_name", appConfig.getInitDomain());//设置隔离域名
                    if (appConfig.isPlayUltra())
                        engineConfig.advancedConfig.put("prefer_play_ultra_source", "1");//设置优先从zego udp服务器拉流
                    engineConfig.advancedConfig.put("play_clear_last_frame", "true");
                    ZegoExpressEngine.setEngineConfig(engineConfig);
                }
                engine = ZegoExpressEngine.createEngine(appConfig.getAppID(), appConfig.getAppSign(), appConfig.isTestEnv(),
                        ZegoScenario.COMMUNICATION, getApplication(), new PlayActivity.MyZegoEventHandler());
                button.setText("释放SDK");
            } else {//销毁引擎
                ZegoExpressEngine.destroyEngine(null);
                engine = null;
                button.setText("初始化SDK");
            }
        });
        btn_login.setOnClickListener(view -> {
            if (engine == null) {
                Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
                return;
            }
            Button button = (Button) view;
            if (button.getText().equals("登录房间")) {
                EditText ed1 = findViewById(R.id.ed_room_id);
                roomID = ed1.getText().toString();
                if (roomID.equals("")) {
                    roomID = String.valueOf((int) (Math.random() * 988 + 11));
                    ed1.setText(roomID);
                }
                Log.i("ExpressDemo", "loginRoom： roomID:" + roomID + "  userID:" + userID + "  userName:" + userName);//打印信息
                listLog.add(format.format(new Date()) + ":loginRoom:roomID:" + roomID + "  userID:" + userID + "  userName:" + userName + "\n");

                ZegoRoomConfig roomConfig = new ZegoRoomConfig();
                if (appConfig.isLoginAuth()) {
                    roomConfig.token = BuildThirdToken.getAuthToken(3600, appConfig.getServerSecret(),
                            appConfig.getAppID(), 123456789, userID);
                }
                ZegoUser user = new ZegoUser(userID, userName);
                engine.loginRoom(roomID, user, roomConfig);//登录房间
                button.setText("退出房间");
            } else {
                engine.logoutRoom(roomID);//退出房间
                button.setText("登录房间");
            }
        });
        btn_play.setOnClickListener(view -> {
            if (engine == null) {
                Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
                return;
            }
            Button button = (Button) view;
            if (button.getText().equals("拉流")) {
                remoteStreamID = ed_stream_id.getText().toString();
                if (remoteStreamID.equals("")) {
                    Toast.makeText(this, " remoteStreamID is empty ", Toast.LENGTH_SHORT).show();
                    return;
                }

                zegoPlayerConfig.resourceMode = resourceMode;

                View remote_view = findViewById(R.id.remote_view);
                engine.startPlayingStream(remoteStreamID, new ZegoCanvas(remote_view), zegoPlayerConfig);
                button.setText("停止拉流");
            } else {
                engine.stopPlayingStream(remoteStreamID);
                button.setText("拉流");
            }
        });
        btn_play_url_cdn.setOnClickListener(view -> {
            if (engine == null) {
                Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
                return;
            }
            Button button = (Button) view;
            if (button.getText().equals("自定义拉流")) {
                remoteStreamID = ed_stream_id.getText().toString();
                playCdnURL = ed_play_cdn_url.getText().toString();

                if (playCdnURL.equals("")) {
                    Toast.makeText(this, "playCdnURL is empty！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (remoteStreamID.equals("")) {
                    remoteStreamID = String.valueOf((int) (Math.random() * 999 + 1000));//随机生成streamID
                    ed_stream_id.setText(remoteStreamID);
                }

                ZegoCDNConfig config = new ZegoCDNConfig();
                config.url = playCdnURL;
                zegoPlayerConfig.cdnConfig = config;
                zegoPlayerConfig.resourceMode = resourceMode;

                View remote_view = findViewById(R.id.remote_view);
                engine.startPlayingStream(remoteStreamID, new ZegoCanvas(remote_view), zegoPlayerConfig);
                button.setText("停止自定义拉流");
            } else {
                engine.stopPlayingStream(remoteStreamID);
                button.setText("自定义拉流");
            }
        });
    }

    /*
     * 设置radio的点击事件
     */
    public void onRadioButtonClicked(View view) {
        RadioButton button = (RadioButton) view;
        boolean isChecked = button.isChecked();
        switch (view.getId()) {
            case R.id.radio0:
                if (isChecked) {
                    resourceMode = ZegoStreamResourceMode.DEFAULT;
                }
                break;
            case R.id.radio1:
                if (isChecked) {
                    resourceMode = ZegoStreamResourceMode.ONLY_CDN;
                }
                break;
            case R.id.radio2:
                if (isChecked) {
                    resourceMode = ZegoStreamResourceMode.ONLY_L3;
                }
                break;
            case R.id.radio3:
                if (isChecked) {
                    resourceMode = ZegoStreamResourceMode.ONLY_RTC;
                }
                break;
            default:
                break;
        }
    }

    class MyZegoEventHandler extends IZegoEventHandler {
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
            tv_p2p_Delay.setText("端到端延迟：" + quality.peerToPeerDelay + " ms");
            Log.i("ExpressDemo", "onPlayerQualityUpdate >>>>>  streamID：" + streamID + " quality：" + JSON.toJSONString(quality));
            listLog.add(format.format(new Date()) + " : onPlayerQualityUpdate > streamID：" + streamID + " quality：" + JSON.toJSONString(quality) + "\n");
        }

        @Override
        public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
            super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
            if (state == ZegoPlayerState.NO_PLAY) {
                tv_p2p_Delay.setText("端到端延迟：");
            }
            Log.i("ExpressDemo", "onPlayerStateUpdate >>>>>  streamID：" + streamID + " state：" + state + " errorCode：" + errorCode + " extendedData：" + JSON.toJSONString(extendedData));
            listLog.add(format.format(new Date()) + " : onPlayerStateUpdate > streamID：" + streamID + " state：" + state + " errorCode：" + errorCode + " extendedData：" + JSON.toJSONString(extendedData) + "\n");
        }

        @Override
        public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
            super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
            Log.i("ExpressDemo", "onPublisherStateUpdate >>>>>  streamID：" + streamID + " state：" + state + " errorCode：" + errorCode + " extendedData：" + JSON.toJSONString(extendedData));
            listLog.add(format.format(new Date()) + " : onPublisherStateUpdate > streamID：" + streamID + " state：" + state + " errorCode：" + errorCode + " extendedData：" + JSON.toJSONString(extendedData) + "\n");
        }
    }
}