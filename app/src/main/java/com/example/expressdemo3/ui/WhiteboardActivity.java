package com.example.expressdemo3.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.expressdemo3.AppConfig;
import com.example.expressdemo3.R;
import com.example.expressdemo3.util.FileUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import im.zego.zegodocs.IZegoDocsViewInitListener;
import im.zego.zegodocs.IZegoDocsViewLoadListener;
import im.zego.zegodocs.IZegoDocsViewScrollCompleteListener;
import im.zego.zegodocs.IZegoDocsViewUploadListener;
import im.zego.zegodocs.ZegoDocsView;
import im.zego.zegodocs.ZegoDocsViewConfig;
import im.zego.zegodocs.ZegoDocsViewConstants;
import im.zego.zegodocs.ZegoDocsViewManager;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoLogConfig;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegowhiteboard.ZegoWhiteboardConfig;
import im.zego.zegowhiteboard.ZegoWhiteboardConstants;
import im.zego.zegowhiteboard.ZegoWhiteboardManager;
import im.zego.zegowhiteboard.ZegoWhiteboardView;
import im.zego.zegowhiteboard.callback.IZegoWhiteboardCreateListener;
import im.zego.zegowhiteboard.callback.IZegoWhiteboardDestroyListener;
import im.zego.zegowhiteboard.callback.IZegoWhiteboardExecuteListener;
import im.zego.zegowhiteboard.callback.IZegoWhiteboardGetListListener;
import im.zego.zegowhiteboard.callback.IZegoWhiteboardInitListener;
import im.zego.zegowhiteboard.callback.IZegoWhiteboardManagerListener;
import im.zego.zegowhiteboard.model.ZegoWhiteboardViewModel;

public class WhiteboardActivity extends AppCompatActivity {

    private AppConfig appConfig;
    private ZegoExpressEngine engine;
    private String userID;
    private String userName;
    private String roomID;
    private Boolean isLogin;
    private Boolean isCreateWhiteboard;
    private Boolean isCreateDocsView;
    private FrameLayout layout_whiteboard;
    private ZegoWhiteboardView frontWhiteboardView = null;
    private ZegoDocsView frontDocsView = null;
    private TextView tv_page;
    private int frontPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_whiteboard);

        setData();
        initSDK();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isLogin = false;
        isCreateWhiteboard = false;
        isCreateDocsView = false;
    }

    private void setData() {
        appConfig = AppConfig.getInstance();
        layout_whiteboard = findViewById(R.id.layout_whiteboard);
        tv_page = findViewById(R.id.tv_page);

        userID = "uID" + System.currentTimeMillis();
        userName = "uName" + System.currentTimeMillis();
//        roomID = "whiteboard-1-test";//+ System.currentTimeMillis();
        isLogin = false;
        isCreateWhiteboard = false;
        isCreateDocsView = false;
    }

    /**
     * 初始化SDK的方法
     */
    public void initSDK() {
        /* 初始化 rtc SDK */
        ZegoLogConfig logConfig = new ZegoLogConfig();
        logConfig.logPath = WhiteboardActivity.this.getExternalFilesDir(null).getAbsolutePath() + File.separator + "ZegoLog";
        ZegoExpressEngine.setLogConfig(logConfig);

        engine = ZegoExpressEngine.createEngine(appConfig.getAppID(), appConfig.getAppSign(), appConfig.isTestEnv(),
                ZegoScenario.GENERAL, getApplication(), new WhiteboardActivity.MyZegoEventHandler());


        /* 初始化 whiteboard SDK */
        ZegoWhiteboardConfig whiteboardConfig = new ZegoWhiteboardConfig();
        whiteboardConfig.setLogPath(WhiteboardActivity.this.getExternalFilesDir(null).getAbsolutePath() + File.separator + "ZegoLog");
        whiteboardConfig.setCacheFolder(WhiteboardActivity.this.getExternalFilesDir(null).getAbsolutePath() + File.separator);
        ZegoWhiteboardManager.getInstance().setConfig(whiteboardConfig);

        ZegoWhiteboardManager.getInstance().init(WhiteboardActivity.this, new IZegoWhiteboardInitListener() {
            @Override
            public void onInit(int errorCode) {
                if (errorCode == 0) {
                    /** 初始化成功 */
                    isCreateWhiteboard = true;
                } else {
                    /** 初始化成功 */
                    isCreateWhiteboard = false;
                }
                Log.i("ExpressDemo", "初始化 白板SDK，isCreateWhiteboard：" + isCreateWhiteboard + " , errorCode：" + errorCode);
            }
        });


        /* 初始化 docsView SDK */
        ZegoDocsViewConfig config = new ZegoDocsViewConfig();
        config.setAppID(appConfig.getAppID());
        config.setAppSign(appConfig.getAppSign());
        config.setTestEnv(appConfig.isTestEnv());
        config.setLogFolder(WhiteboardActivity.this.getExternalFilesDir(null).getAbsolutePath() + File.separator + "ZegoLog");
        config.setDataFolder(WhiteboardActivity.this.getExternalFilesDir(null).getAbsolutePath() + File.separator);
        config.setCacheFolder(WhiteboardActivity.this.getExternalFilesDir(null).getAbsolutePath() + File.separator);

        ZegoDocsViewManager.getInstance().init(getApplication(), config, new IZegoDocsViewInitListener() {
            @Override
            public void onInit(int errorCode) {
                if (errorCode == 0) {
                    /** 初始化成功*/
                    isCreateDocsView = true;
                } else {
                    /** 初始化失败 详细错误码见 ZegoDocsViewConstants */
                    isCreateDocsView = false;
                }
                Log.i("ExpressDemo", "初始化 文件共享SDK，isCreateDocsView：" + isCreateDocsView + " , errorCode：" + errorCode);
            }
        });

        /* 设置白板事件监听 */
        ZegoWhiteboardManager.getInstance().setWhiteboardManagerListener(new IZegoWhiteboardManagerListener() {
            @Override
            public void onError(int errorCode) {
                Log.i("ExpressDemo", "onError方法，errorCode：" + errorCode);
            }

            @Override
            public void onWhiteboardAdded(ZegoWhiteboardView zegoWhiteboardView) {
                Log.i("ExpressDemo", "onWhiteboardAdded方法，zegoWhiteboardView：" + zegoWhiteboardView.toString());
            }

            @Override
            public void onWhiteboardRemoved(long whiteboardID) {
                Log.i("ExpressDemo", "onWhiteboardRemoved方法，whiteboardID：" + whiteboardID);
            }
        });
    }


    /* 登录房间 */
    public void login(View view) {
        if (isLogin) {
            Toast.makeText(WhiteboardActivity.this, "已经登录房间，请勿重复操作", Toast.LENGTH_SHORT).show();
        } else {//未登录，进行登录操作
            EditText ed1 = findViewById(R.id.ed_room_id);
            roomID = ed1.getText().toString();
            if (roomID.equals("")) {//没有主动输入房间号，则自动生成一串随机数字
                roomID = String.valueOf((int) (Math.random() * 988 + 11));
                ed1.setText(roomID);
            }
            engine.loginRoom(roomID, new ZegoUser(userID, userName));
        }
    }

    /* 登出房间 */
    public void logout(View view) {
        if (isLogin) {//已登录房间，进行登出操作
            engine.logoutRoom(roomID);
        } else {//未登录，进行登录操作
            Toast.makeText(WhiteboardActivity.this, "用户未登录房间！", Toast.LENGTH_SHORT).show();
        }
    }

    /* 上传文件的监听事件 */
    public void uploadFile(View view) {
        if (isLogin) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 1);
        } else {
            Toast.makeText(WhiteboardActivity.this, "没有登录房间，请先登录", Toast.LENGTH_SHORT).show();
        }
    }

    /* 返回本地文件管理器的选择结果 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String filePath = "";
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            try {
                filePath = FileUtils.getFilePathByUri(WhiteboardActivity.this, uri);//获取文件的绝对路径
                Log.i("ExpressDemo", "获取到的本地文件路径：" + filePath);

                /* 上传文件 */
                int renderType = ZegoDocsViewConstants.ZegoDocsViewRenderTypeDynamicPPTH5;//例如转换成 动态ppt模式
                ZegoDocsViewManager.getInstance().uploadFile(filePath, renderType, new IZegoDocsViewUploadListener() {
                    @Override
                    public void onUpload(int state, int errorCode, @NonNull HashMap<String, Object> infoMap) {
                        if (errorCode == ZegoDocsViewConstants.ZegoDocsViewSuccess) {
                            if (state == ZegoDocsViewConstants.ZegoDocsViewUploadStateUpload) {
                                // 上传中...
                                float uploadPercent = (float) infoMap.get("upload_percent");
                                Log.i("ExpressDemo", "文件上传进度：" + uploadPercent);
                                if (uploadPercent == 1f) {
                                    /** 正在转码... */
                                }
                            } else if (state == ZegoDocsViewConstants.ZegoDocsViewUploadStateConvert) {
                                /** 转换成功 */
                                Log.i("ExpressDemo", "转码后的文件信息，infoMap：" + infoMap.toString());

                                ZegoDocsView zegoDocsView = new ZegoDocsView(WhiteboardActivity.this);
                                layout_whiteboard.addView(zegoDocsView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));   // 添加到父容器中

                                zegoDocsView.setEstimatedSize(16, 9);//设置宽高
                                String fileID = (String) infoMap.get("upload_fileid");  // 上传文件得到fileID
                                Log.i("ExpressDemo", "fileID：" + fileID);

                                String authKey = ""; //
                                //本指引使用docs_view_container作为布局中的view容器(与demo保持一致)，容器可为FrameLayout等
                                zegoDocsView.loadFile(fileID, authKey, new IZegoDocsViewLoadListener() {
                                    @Override
                                    public void onLoadFile(int errorCode) {
                                        if (errorCode == 0) {
                                            /** 加载文件成功 */
                                            frontDocsView = zegoDocsView;//文件加载成功，把当前显示的docsView赋值给全局变量

                                            ZegoWhiteboardViewModel zegoWhiteboardViewModel = new ZegoWhiteboardViewModel();
                                            zegoWhiteboardViewModel.setRoomId(roomID);
                                            zegoWhiteboardViewModel.setWhiteboardID(System.currentTimeMillis());
                                            zegoWhiteboardViewModel.setName(zegoDocsView.getFileName());
                                            zegoWhiteboardViewModel.setPageCount(zegoDocsView.getPageCount());
                                            zegoWhiteboardViewModel.setAspectWidth(zegoDocsView.getContentSize().getWidth());
                                            zegoWhiteboardViewModel.setAspectHeight(zegoDocsView.getContentSize().getHeight());
                                            zegoWhiteboardViewModel.getFileInfo().setFileID(zegoDocsView.getFileID());
                                            zegoWhiteboardViewModel.getFileInfo().setFileName(zegoDocsView.getFileName());
                                            zegoWhiteboardViewModel.getFileInfo().setFileType(zegoDocsView.getFileType());
                                            zegoWhiteboardViewModel.getFileInfo().setAuthKey("");

                                            ZegoWhiteboardManager.getInstance().createWhiteboardView(zegoWhiteboardViewModel, new IZegoWhiteboardCreateListener() {
                                                @Override
                                                public void onCreate(int errorCode, @Nullable ZegoWhiteboardView whiteboardView) {
                                                    if (errorCode == 0 && whiteboardView != null) {
                                                        /** 创建关联文件的白板成功 */
                                                        whiteboardView.setWhiteboardOperationMode(ZegoWhiteboardConstants.ZegoWhiteboardOperationModeZoom | ZegoWhiteboardConstants.ZegoWhiteboardOperationModeDraw);
                                                        ZegoWhiteboardManager.getInstance().setToolType(ZegoWhiteboardConstants.ZegoWhiteboardViewToolPen);

                                                        if (zegoDocsView.getVisibleSize().getHeight() != 0 || zegoDocsView.getVisibleSize().getWidth() != 0) {
                                                            whiteboardView.setVisibleRegion(zegoDocsView.getVisibleSize());
                                                        }

                                                        layout_whiteboard.addView(whiteboardView, new ViewGroup.LayoutParams(
                                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                                ViewGroup.LayoutParams.MATCH_PARENT
                                                        ));

                                                        frontWhiteboardView = whiteboardView;//加载文件白板成功，把当前显示的白板赋值给全局变量
                                                        frontPage = 1;//赋值当前页数
                                                        tv_page.setText(frontPage + "/" + frontDocsView.getPageCount());
                                                    } else {
                                                        /** 创建关联文件的白板失败 */
                                                    }
                                                    Log.i("ExpressDemo", "创建关联文件，errorCode：" + errorCode);
                                                }
                                            });
                                        } else {
                                            /** 加载文件失败 */
                                        }
                                        Log.i("ExpressDemo", "文件加载结果，errorCode：" + errorCode);
                                    }
                                });
                            }
                        } else {
                            /** 上传失败 */
                            Log.i("ExpressDemo", "文件上传失败，errorCode：" + errorCode);
                        }
                    }
                });
            } catch (Exception e) {
                Log.i("ExpressDemo", "您选择的文件 无法获取绝对路径！Exception：" + e);
            }
        }
    }

    /* 加载文档的监听事件 */
    public void loadDocs(View view) {
        if (isLogin) {
            ArrayList<ZegoWhiteboardView> list = new ArrayList<>();

            ZegoWhiteboardManager.getInstance().getWhiteboardViewList(new IZegoWhiteboardGetListListener() {
                @Override
                public void onGetList(int err, ZegoWhiteboardView[] zegoWhiteboardViews) {
                    if (err == 0) {
                        int len = zegoWhiteboardViews.length;

                        Log.i("ExpressDemo", "getWhiteboardViewList is success，Length：" + len);

                        String[] educationArray = new String[len];

                        for (int i = 0; i < zegoWhiteboardViews.length; i++) {
                            list.add(zegoWhiteboardViews[i]);
                            String fileName = zegoWhiteboardViews[i].getWhiteboardViewModel().getFileInfo().getFileName();
                            if (!fileName.equals("")) {
                                educationArray[i] = fileName;
                            }
                        }

                        new AlertDialog.Builder(WhiteboardActivity.this)
                                .setTitle("请选择 房间内的文件").setIcon(R.drawable.wenjian)
                                .setItems(educationArray, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(WhiteboardActivity.this, "您选择了: " + which + ":" + educationArray[which], Toast.LENGTH_LONG).show();
                                        dialog.dismiss();

                                        ZegoDocsView zegoDocsView = new ZegoDocsView(WhiteboardActivity.this);
                                        layout_whiteboard.addView(zegoDocsView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));   // 添加到父容器中

                                        zegoDocsView.setEstimatedSize(16, 9);//设置宽高
                                        String fileID = zegoWhiteboardViews[which].getWhiteboardViewModel().getFileInfo().getFileID();
                                        Log.i("ExpressDemo", "LoadDocs，fileID：" + fileID);

                                        String authKey = ""; //
                                        //本指引使用docs_view_container作为布局中的view容器(与demo保持一致)，容器可为FrameLayout等
                                        zegoDocsView.loadFile(fileID, authKey, new IZegoDocsViewLoadListener() {
                                            @Override
                                            public void onLoadFile(int errorCode) {
                                                if (errorCode == 0) {
                                                    frontDocsView = zegoDocsView;//文件加载成功，把当前显示的docsView赋值给全局变量

                                                    /** 加载白板视图 */
                                                    layout_whiteboard.addView(zegoWhiteboardViews[which], new ViewGroup.LayoutParams(
                                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                                            ViewGroup.LayoutParams.MATCH_PARENT
                                                    ));

                                                    frontWhiteboardView = zegoWhiteboardViews[which];//加载文件白板成功，把当前显示的白板赋值给全局变量
                                                    frontPage = 1;//赋值当前页数
                                                    tv_page.setText(frontPage + "/" + frontDocsView.getPageCount());
                                                }
                                                Log.i("ExpressDemo", "文件加载结果，errorCode：" + errorCode);
                                            }
                                        });
                                    }
                                }).setNegativeButton("取消", null)
                                .show();
                    } else {
                        Toast.makeText(WhiteboardActivity.this, "房间内，获取白板view列表失败，error:" + err, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(WhiteboardActivity.this, "没有登录房间，请先登录", Toast.LENGTH_SHORT).show();
        }
    }

    /* 上一页的监听事件 */
    public void previousPage(View view) {
        if (frontPage != 1) {
            int targetPage = frontPage - 1;
            frontDocsView.flipPage(targetPage, new IZegoDocsViewScrollCompleteListener() {
                @Override
                public void onScrollComplete(boolean result) {
                    if (result) {
                        frontWhiteboardView.scrollTo(0f, frontDocsView.getVerticalPercent(), new IZegoWhiteboardExecuteListener() {
                            @Override
                            public void onExecute(int err) {
                                if (err == 0) {
                                    tv_page.setText(targetPage + "/" + frontDocsView.getPageCount());
                                    frontPage = targetPage;
                                }
                            }
                        });
                    }
                }
            });
        } else {
            Toast.makeText(WhiteboardActivity.this, "当前已是首页，无法向上翻页", Toast.LENGTH_SHORT).show();
        }
    }

    /* 下一页的监听事件 */
    public void nextPage(View view) {
        if (frontPage != frontDocsView.getPageCount()) {
            int targetPage = frontPage + 1;
            frontDocsView.flipPage(targetPage, new IZegoDocsViewScrollCompleteListener() {
                @Override
                public void onScrollComplete(boolean result) {
                    if (result) {
                        frontWhiteboardView.scrollTo(0f, frontDocsView.getVerticalPercent(), new IZegoWhiteboardExecuteListener() {
                            @Override
                            public void onExecute(int err) {
                                if (err == 0) {
                                    tv_page.setText(targetPage + "/" + frontDocsView.getPageCount());
                                    frontPage = targetPage;
                                }
                            }
                        });
                    }
                }
            });
        } else {
            Toast.makeText(WhiteboardActivity.this, "当前已是尾页，无法向下翻页", Toast.LENGTH_SHORT).show();
        }
    }

    /* 卸载文件/白板的监听事件 */
    public void unload(View view) {
        if (isLogin && (frontWhiteboardView != null)) {
            ZegoWhiteboardManager.getInstance().destroyWhiteboardView(frontWhiteboardView.getWhiteboardViewModel().getWhiteboardID(), new IZegoWhiteboardDestroyListener() {
                @Override
                public void onDestroy(int error, long id) {
                    if (error == 0) {
                        Log.i("ExpressDemo", "白板销毁成功，对应whiteboardID为：" + id);
                        if (frontDocsView != null) {
                            frontDocsView.unloadFile();
                        }
                        layout_whiteboard.removeAllViews();

                        frontDocsView = null;
                        frontWhiteboardView = null;
                        frontPage = 1;//重置当前页数
                    } else {
                        Log.i("ExpressDemo", "白板销毁失败，错误码error为：" + error);
                    }
                }
            });
        } else {
            Toast.makeText(WhiteboardActivity.this, "没有登录房间 或者 当前没有展示白板，无需销毁", Toast.LENGTH_SHORT).show();
        }
    }

    /* 创建纯白板 */
    public void createWhiteBoard(View view) {
        if (isLogin) {
            ZegoWhiteboardViewModel zegoWhiteboardViewModel = new ZegoWhiteboardViewModel();
            zegoWhiteboardViewModel.setAspectHeight(9);    // 希望创建的白板的等比高
            zegoWhiteboardViewModel.setAspectWidth(16);    // 希望创建的白板的等比宽，如果需要创建多页的白板，需要乘以相应的倍数
            zegoWhiteboardViewModel.setPageCount(1);      // 白板的页数
            zegoWhiteboardViewModel.setRoomId(roomID);
            zegoWhiteboardViewModel.setWhiteboardID(System.currentTimeMillis());

            ZegoWhiteboardManager.getInstance().createWhiteboardView(zegoWhiteboardViewModel, new IZegoWhiteboardCreateListener() {
                @Override
                public void onCreate(int errorCode, @Nullable ZegoWhiteboardView whiteboardView) {
                    if (errorCode == 0 && whiteboardView != null) {
                        /** 创建纯白板成功 */
                        Log.i("ExpressDemo", "whiteboardView的宽高" + whiteboardView.getHorizontalPercent() + " ::: " + whiteboardView.getVerticalPercent());

                        whiteboardView.setBackgroundColor(Color.parseColor("#F8F8FF"));
                        whiteboardView.setWhiteboardOperationMode(ZegoWhiteboardConstants.ZegoWhiteboardOperationModeDraw);
                        ZegoWhiteboardManager.getInstance().setToolType(ZegoWhiteboardConstants.ZegoWhiteboardViewToolPen);

                        layout_whiteboard.addView(whiteboardView);

                        frontWhiteboardView = whiteboardView;//加载文件白板成功，把当前显示的白板赋值给全局变量
                    } else {
                        /** 创建关联文件的白板失败 */
                    }
                    Log.i("ExpressDemo", "创建纯白板，errorCode：" + errorCode);
                }
            });
        } else {
            Toast.makeText(WhiteboardActivity.this, "没有登录房间，请先登录", Toast.LENGTH_SHORT).show();
        }
    }


    class MyZegoEventHandler extends IZegoEventHandler {
        @Override
        public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
            super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
            if (state == ZegoRoomState.CONNECTED) {
                isLogin = true;
            } else {
                isLogin = false;
            }
            Log.i("ExpressDemo", "onRoomStateUpdate >>>>>  roomID：" + roomID + " state：" + state.toString() + " errorCode：" + errorCode + " extendedData：" + extendedData.toString());
        }

        @Override
        public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
            super.onRoomUserUpdate(roomID, updateType, userList);
            Log.i("ExpressDemo", "onRoomUserUpdate >>>>>  roomID：" + roomID + " updateType：" + updateType.toString() + " userList：" + JSON.toJSONString(userList));
        }
    }

}