package com.example.expressdemo3.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import im.zego.zegoexpress.constants.ZegoUpdateType;

import im.zego.zegoexpress.entity.ZegoBarrageMessageInfo;
import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;
import im.zego.zegoexpress.entity.ZegoCDNConfig;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;

import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class PublishActivity extends AppCompatActivity {
    private AppConfig appConfig;

    private ZegoExpressEngine engine;
    private String userID;
    private String userName;
    private String roomID;
    private String localStreamID;
    private String publishCdnURL;

    private boolean publishStreamMute;
    private ImageButton ib_publish_mic;

    private ArrayList<String> listLog;
    private SimpleDateFormat format;

    private Button btn_log;
    private Button btn_init;
    private Button btn_login;
    private Button btn_publish;
    private Button btn_publish_direct_cdn;
    private Button btn_publish_indirect_cdn;
    private EditText ed_stream_id;
    private EditText ed_cdn_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_publish);

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
        ib_publish_mic = findViewById(R.id.ib_publish_mic);

        btn_log = findViewById(R.id.btn_log);
        btn_init = findViewById(R.id.btn_init);
        btn_login = findViewById(R.id.btn_login);
        btn_publish = findViewById(R.id.btn_publish);
        btn_publish_direct_cdn = findViewById(R.id.btn_publish_direct_cdn);
        btn_publish_indirect_cdn = findViewById(R.id.btn_publish_indirect_cdn);
        ed_stream_id = findViewById(R.id.ed_stream_id);
        ed_cdn_url = findViewById(R.id.ed_cdn_url);
    }

    private void setData() {
        appConfig = AppConfig.getInstance();
        publishStreamMute = false;
        listLog = new ArrayList<>();
        format = new SimpleDateFormat("HH:mm:ss");

        /*userID = "uID" + System.currentTimeMillis();
        userName = "uName" + System.currentTimeMillis();*/

        userID = "uID111";
        userName = "uName222";
    }

    private void setEventListen() {
        ib_publish_mic.setOnClickListener(view -> {
            if (engine == null) {
                Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
                return;
            }
            publishStreamMute = !publishStreamMute;
            if (publishStreamMute)
                ib_publish_mic.setBackgroundResource(R.drawable.ic_bottom_microphone_on);
            else
                ib_publish_mic.setBackgroundResource(R.drawable.ic_bottom_microphone_off);
            /* Enable Mic*/
            engine.muteMicrophone(!publishStreamMute);
        });
        btn_log.setOnClickListener(view -> {  //跳转
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("list", listLog);
            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClass(PublishActivity.this, LogActivity.class);
            PublishActivity.this.startActivity(intent);
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
                    engineConfig.advancedConfig.put("preview_clear_last_frame", "true");
                    ZegoExpressEngine.setEngineConfig(engineConfig);
                }
                engine = ZegoExpressEngine.createEngine(appConfig.getAppID(), appConfig.getAppSign(), appConfig.isTestEnv(),
                        ZegoScenario.GENERAL, getApplication(), new PublishActivity.MyZegoEventHandler());
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
        btn_publish.setOnClickListener(view -> {
            if (engine == null) {
                Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
                return;
            }
            Button button = (Button) view;
            if (button.getText().equals("推流")) {
                localStreamID = ed_stream_id.getText().toString();
                if (localStreamID.equals("")) {
                    localStreamID = String.valueOf((int) (Math.random() * 999 + 1000));//随机生成streamID
                    ed_stream_id.setText(localStreamID);
                }

                engine.enableBeautify(ZegoBeautifyFeature.POLISH.value() |
                        ZegoBeautifyFeature.WHITEN.value() | ZegoBeautifyFeature.SHARPEN.value());//开启美颜
                View local_view = findViewById(R.id.local_view);
                engine.startPreview(new ZegoCanvas(local_view));
                engine.startPublishingStream(localStreamID);
                button.setText("停止推流");
            } else {
                engine.stopPreview();
                engine.stopPublishingStream();
                button.setText("推流");
            }
        });
        btn_publish_direct_cdn.setOnClickListener(view -> {
            if (engine == null) {
                Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
                return;
            }
            Button button = (Button) view;
            if (button.getText().equals("直推CDN")) {
                localStreamID = ed_stream_id.getText().toString();
                publishCdnURL = ed_cdn_url.getText().toString();

                if (localStreamID.equals("")) {
                    localStreamID = String.valueOf((int) (Math.random() * 999 + 1000));
                    ed_stream_id.setText(localStreamID);
                }

                ZegoCDNConfig config = new ZegoCDNConfig();
                config.url = publishCdnURL;
                engine.enablePublishDirectToCDN(true, config);

                View local_view = findViewById(R.id.local_view);
                engine.startPreview(new ZegoCanvas(local_view));
                engine.startPublishingStream(localStreamID);
                button.setText("停止推流");
            } else {
                engine.stopPreview();
                engine.stopPublishingStream();
                button.setText("直推CDN");
            }
        });
        btn_publish_indirect_cdn.setOnClickListener(view -> {
            if (engine == null) {
                Toast.makeText(this, "sdk_not_init", Toast.LENGTH_SHORT).show();
                return;
            }
            Button button = (Button) view;
            if (button.getText().equals("转推CDN")) {
                localStreamID = ed_stream_id.getText().toString();
                publishCdnURL = ed_cdn_url.getText().toString();

                if (publishCdnURL.equals("")) {
                    Toast.makeText(this, "cdnURL is empty！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (localStreamID.equals("")) {
                    localStreamID = String.valueOf((int) (Math.random() * 999 + 1000));
                    ed_stream_id.setText(localStreamID);
                }

                engine.addPublishCdnUrl(localStreamID, publishCdnURL, errorCode -> {
                    if (errorCode == 0) {
                        // 转推成功
                        Log.w("ExpressDemo", "转推成功");
                    } else {
                        // 转推失败，可能由于网络原因转推请求发送失败
                        Log.w("ExpressDemo", "转推失败，errorCode：" + errorCode);
                    }
                    listLog.add(format.format(new Date()) + " : addPublishCdnUrl > errorCode：" + errorCode + "\n");
                });
                button.setText("停止转推");
            } else {
                engine.removePublishCdnUrl(localStreamID, publishCdnURL, errorCode -> {
                    if (errorCode == 0) {
                        // 停止转推成功
                        Log.w("ExpressDemo", "停止转推成功");
                    } else {
                        // 停止转推失败，可能由于网络原因停止转推请求发送失败
                        Log.w("ExpressDemo", "停止转推失败，errorCode：" + errorCode);
                    }
                    listLog.add(format.format(new Date()) + " : removePublishCdnUrl > errorCode：" + errorCode + "\n");
                });
                button.setText("转推CDN");
            }
        });
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

        @Override
        public void onIMRecvBroadcastMessage(String roomID, ArrayList<ZegoBroadcastMessageInfo> messageList) {
            super.onIMRecvBroadcastMessage(roomID, messageList);
            Log.i("ExpressDemo", "onIMRecvBroadcastMessage >>>>>  roomID：" + roomID + " messageList：" + JSON.toJSONString(messageList));
            listLog.add(format.format(new Date()) + " : onIMRecvBroadcastMessage > roomID：" + roomID + " messageList：" + JSON.toJSONString(messageList) + "\n");
        }

        @Override
        public void onIMRecvBarrageMessage(String roomID, ArrayList<ZegoBarrageMessageInfo> messageList) {
            super.onIMRecvBarrageMessage(roomID, messageList);
            Log.i("ExpressDemo", "onIMRecvBarrageMessage >>>>>  roomID：" + roomID + " messageList：" + JSON.toJSONString(messageList));
            listLog.add(format.format(new Date()) + " : onIMRecvBarrageMessage > roomID：" + roomID + " messageList：" + JSON.toJSONString(messageList) + "\n");
        }

        @Override
        public void onIMRecvCustomCommand(String roomID, ZegoUser fromUser, String command) {
            super.onIMRecvCustomCommand(roomID, fromUser, command);
            Log.i("ExpressDemo", "onIMRecvCustomCommand >>>>>  roomID：" + roomID + " fromUser：" + fromUser.userID + " command：" + JSON.toJSONString(command));
            listLog.add(format.format(new Date()) + " : onIMRecvCustomCommand > roomID：" + roomID + " fromUser：" + fromUser.userID + " command：" + JSON.toJSONString(command) + "\n");
        }
    }
}