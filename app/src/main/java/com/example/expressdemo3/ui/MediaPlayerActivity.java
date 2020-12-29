package com.example.expressdemo3.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.expressdemo3.AppConfig;
import com.example.expressdemo3.R;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.ZegoMediaPlayer;
import im.zego.zegoexpress.callback.IZegoCustomVideoCaptureHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoMediaPlayerEventHandler;
import im.zego.zegoexpress.callback.IZegoMediaPlayerLoadResourceCallback;
import im.zego.zegoexpress.callback.IZegoMediaPlayerSeekToCallback;
import im.zego.zegoexpress.callback.IZegoMediaPlayerVideoHandler;
import im.zego.zegoexpress.constants.ZegoMediaPlayerNetworkEvent;
import im.zego.zegoexpress.constants.ZegoMediaPlayerState;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoVideoBufferType;
import im.zego.zegoexpress.constants.ZegoVideoFrameFormat;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoCustomVideoCaptureConfig;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoFrameParam;

public class MediaPlayerActivity extends AppCompatActivity {
    private AppConfig appConfig;

    private ZegoExpressEngine engine;
    private ZegoMediaPlayer mediaplayer;
    private String userID;
    private String userName;
    private String roomID;
    private String localStreamID;
    private Boolean isOpenCustomCapture;
    private Boolean isPublish;

    private SeekBar videoProgress;
    private SeekBar audioVolume;
    private CheckBox repeatButton;
    private CheckBox auxButton;

    private String path = "https://storage.zego.im/demo/201808270915.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        setData();
        Log.i("ExpressDemo", "ZEGO SDK Version： " + ZegoExpressEngine.getVersion());
        initSDK();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        engine.destroyMediaPlayer(mediaplayer);
        mediaplayer = null;
        ZegoExpressEngine.destroyEngine(null);
    }


    private void setData() {
        appConfig = AppConfig.getInstance();

        userID = "uID" + System.currentTimeMillis();
        userName = "uName" + System.currentTimeMillis();
        localStreamID = "mediaPlayer-" + System.currentTimeMillis();
        roomID = "mediaPlayer-001";
        isOpenCustomCapture = false;
        isPublish = false;
        mediaplayer = null;

        videoProgress = findViewById(R.id.video_progress);
        audioVolume = findViewById(R.id.audio_volume);

        repeatButton = findViewById(R.id.repeat_button);
        auxButton = findViewById(R.id.aux_button);

        videoProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mediaplayer != null) {
                    mediaplayer.seekTo(Long.valueOf(i), new IZegoMediaPlayerSeekToCallback() {
                        @Override
                        public void onSeekToTimeCallback(int errorCode) {
                            // 本回调在UI线程被回调, 开发者可以在此进行UI的变化
                            if (errorCode == 0) {
                                Log.d("ExpressDemo", "onSeekToTimeCallback: success");
                            } else {
                                Log.d("ExpressDemo", "onSeekToTimeCallback: errorCode = " + errorCode);
                            }
                        }
                    });
                }
                Log.d("ExpressDemo", "视频进度条 当前值：" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("ExpressDemo", "触碰 video_SeekBar");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("ExpressDemo", "放开 video_SeekBar");
            }
        });
        audioVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mediaplayer != null) {
                    mediaplayer.setVolume(i);
                }
                Log.d("ExpressDemo", "音量栏 当前值：" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("ExpressDemo", "触碰 audio_SeekBar");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("ExpressDemo", "放开 audio_SeekBar");
            }
        });
    }

    public void playVideo(View view) {
        engine.loginRoom(roomID, new ZegoUser(userID, userName));
    }

    public void stopVideo(View view) {
        engine.stopPublishingStream(ZegoPublishChannel.MAIN);
        engine.logoutRoom(roomID);

        if (mediaplayer != null) {
            mediaplayer.stop();
            mediaplayer.setPlayerCanvas(null);
        }

        isOpenCustomCapture = false;
        isPublish = false;
    }

    public void pausePlay(View view) {
        if (mediaplayer != null) {
            mediaplayer.pause();
        }
    }

    public void resume(View view) {
        if (mediaplayer != null) {
            mediaplayer.resume();
        }
    }

    public void initSDK() {
        engine = ZegoExpressEngine.createEngine(appConfig.getAppID(), appConfig.getAppSign(), appConfig.isTestEnv(),
                ZegoScenario.GENERAL, getApplication(), new MediaPlayerActivity.MyZegoEventHandler());

        ZegoCustomVideoCaptureConfig videoCaptureConfig = new ZegoCustomVideoCaptureConfig();
        // 选择 RAW_DATA 类型视频帧数据
        videoCaptureConfig.bufferType = ZegoVideoBufferType.RAW_DATA;

        engine.enableCustomVideoCapture(true, videoCaptureConfig, ZegoPublishChannel.MAIN);
        engine.setCustomVideoCaptureHandler(new IZegoCustomVideoCaptureHandler() {
            @Override
            public void onStart(ZegoPublishChannel channel) {
                // 收到回调后，开发者需要执行启动视频采集相关的业务逻辑，例如开启摄像头等
                Log.i("ExpressDemo", "自定义采集已启动");
                isOpenCustomCapture = true;
            }

            @Override
            public void onStop(ZegoPublishChannel channel) {
                // 收到回调后，开发者需要执行停止视频采集相关的业务逻辑，例如关闭摄像头等
                isOpenCustomCapture = false;
            }
        });

        mediaplayer = engine.createMediaPlayer();
        if (mediaplayer != null) {
            Log.d("ExpressDemo", "createMediaplayer create sucess");
        } else {
            Log.d("ExpressDemo", "createMediaplayer create fail");
        }
        mediaplayer.setEventHandler(new MyIZegoMediaPlayerEventHandler());

        // 播放器抛视频数据的回调
        mediaplayer.setVideoHandler(new IZegoMediaPlayerVideoHandler() {
            @Override
            public void onVideoFrame(ZegoMediaPlayer mediaPlayer, ByteBuffer[] data, int[] dataLength, ZegoVideoFrameParam param) {
                // 开发者可以在这个回调里对该抛出的视频帧数据进行处理, 例如进行本地存储、视频图层混合等
                Log.d("ExpressDemo", "onVideoFrame，isOpenCustomCapture：" + isOpenCustomCapture + ",isPublish: " + isPublish);
                if (isOpenCustomCapture && isPublish) {
                    engine.sendCustomVideoCaptureRawData(data[0], dataLength[0], param, getTimestamp(new Date()));
                }
            }
        }, ZegoVideoFrameFormat.RGBA32);// 第二个参数一般应指定为平台默认的视频帧格式
    }

    public class MyIZegoMediaPlayerEventHandler extends IZegoMediaPlayerEventHandler {

        private static final String TAG = "MyIZegoExpressMediaplay";

        @Override
        public void onMediaPlayerStateUpdate(ZegoMediaPlayer mediaPlayer, ZegoMediaPlayerState state, int errorCode) {
            // 本回调在UI线程被回调, 开发者可以在此进行UI的变化, 例如播放按钮的变化
            if (state == ZegoMediaPlayerState.PLAYING) {
                engine.startPublishingStream(localStreamID, ZegoPublishChannel.MAIN);
            }
            Log.d("ExpressDemo", "onMediaPlayerStateUpdate: state = " + state.value() + ", errorCode = " + errorCode + ", zegoExpressMediaplayer = " + mediaPlayer);
        }

        @Override
        public void onMediaPlayerNetworkEvent(ZegoMediaPlayer mediaPlayer, ZegoMediaPlayerNetworkEvent networkEvent) {
            // 本回调在UI线程被回调, 开发者可以在此进行UI的变化, 例如网络不好的情况做友好的提示
            Log.d("ExpressDemo", "onMediaPlayerNetworkEvent: networkEvent = " + networkEvent.value() + ", zegoExpressMediaplayer = " + mediaPlayer);
        }

        @Override
        public void onMediaPlayerPlayingProgress(ZegoMediaPlayer mediaPlayer, long millisecond) {
            // 本回调在UI线程被回调, 开发者可以在此进行UI的变化, 例如进度条的变化
            Log.d("ExpressDemo", "onMediaPlayerPlayingProgress: millisecond = " + millisecond + ", zegoExpressMediaplayer = " + mediaPlayer);
        }
    }


    class MyZegoEventHandler extends IZegoEventHandler {
        @Override
        public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
            super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
            Log.i("ExpressDemo", "onRoomStateUpdate >>>>>  roomID：" + roomID + " state：" + state.toString() + " errorCode：" + errorCode + " extendedData：" + extendedData.toString());
            if (state == ZegoRoomState.CONNECTED) {
                mediaplayer.loadResource(path, new IZegoMediaPlayerLoadResourceCallback() {
                    @Override
                    public void onLoadResourceCallback(int errorCode) {
                        if (errorCode == 0) {
                            Log.d("ExpressDemo", "onLoadResourceCallback: success");

                            Long totalDuration = mediaplayer.getTotalDuration();
                            videoProgress.setMax(totalDuration.intValue());//设置 播放进度条

                            mediaplayer.enableRepeat(repeatButton.isChecked());
                            mediaplayer.enableAux(auxButton.isChecked());
                            mediaplayer.start();//开始播放
                            mediaplayer.setPlayerCanvas(new ZegoCanvas(findViewById(R.id.video_view)));
                        } else {
                            Log.d("ExpressDemo", "onLoadResourceCallback: errorCode = " + errorCode);
                        }
                    }
                });
            }
        }

        @Override
        public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
            super.onRoomUserUpdate(roomID, updateType, userList);
            Log.i("ExpressDemo", "onRoomUserUpdate >>>>>  roomID：" + roomID + " updateType：" + updateType.toString() + " userList：" + JSON.toJSONString(userList));
        }

        @Override
        public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList) {
            super.onRoomStreamUpdate(roomID, updateType, streamList);
            Log.i("ExpressDemo", "onRoomStreamUpdate >>>>>  roomID：" + roomID + " updateType：" + updateType.toString() + " streamList：" + JSON.toJSONString(streamList));
        }

        @Override
        public void onPublisherQualityUpdate(String streamID, ZegoPublishStreamQuality quality) {
            super.onPublisherQualityUpdate(streamID, quality);
            Log.i("ExpressDemo", "onPublisherQualityUpdate >>>>>  streamID：" + streamID + " quality：" + JSON.toJSONString(quality));
        }

        @Override
        public void onPlayerQualityUpdate(String streamID, ZegoPlayStreamQuality quality) {
            super.onPlayerQualityUpdate(streamID, quality);
            Log.i("ExpressDemo", "onPlayerQualityUpdate >>>>>  streamID：" + streamID + " quality：" + JSON.toJSONString(quality));
        }

        @Override
        public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
            super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
            Log.i("ExpressDemo", "onPlayerStateUpdate >>>>>  streamID：" + streamID + " state：" + state + " errorCode：" + errorCode + " extendedData：" + JSON.toJSONString(extendedData));
        }

        @Override
        public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
            super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
            if (state == ZegoPublisherState.PUBLISHING) {
                isPublish = true;
                ((TextView) findViewById(R.id.tv_streamID)).setText(localStreamID);
            } else if (state == ZegoPublisherState.NO_PUBLISH) {
                ((TextView) findViewById(R.id.tv_streamID)).setText("");
            }
            Log.i("ExpressDemo", "onPublisherStateUpdate >>>>>  streamID：" + streamID + " state：" + state + " errorCode：" + errorCode + " extendedData：" + JSON.toJSONString(extendedData));
        }
    }


    /**
     * 获取精确到毫秒的时间戳
     *
     * @param date
     * @return
     **/
    public static Long getTimestamp(Date date) {
        if (null == date) {
            return (long) 0;
        }
        String timestamp = String.valueOf(date.getTime());
        return Long.valueOf(timestamp);
    }

}