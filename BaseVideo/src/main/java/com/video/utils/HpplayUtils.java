package com.video.utils;

import android.animation.StateListAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hpplay.common.utils.LeLog;
import com.hpplay.sdk.source.api.IBindSdkListener;
import com.hpplay.sdk.source.api.IConnectListener;
import com.hpplay.sdk.source.api.ILelinkPlayerListener;
import com.hpplay.sdk.source.api.LelinkPlayerInfo;
import com.hpplay.sdk.source.api.LelinkSourceSDK;
import com.hpplay.sdk.source.browse.api.IAPI;
import com.hpplay.sdk.source.browse.api.IBrowseListener;
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;
import com.huoyan.basevideo.R;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.video.adapter.BrowseAdapter;
import com.video.view.BaseVideoPlayer;
import com.video.view.RemoteControlMenu;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static android.content.Context.WIFI_SERVICE;

public class HpplayUtils {
    private Context mContext;
    private BaseVideoPlayer baseVideoPlayer;
    private NetworkInfo networkInfo;
    private static final String TAG = "hputils";
    //火眼
//        private static final String APP_ID = "15450";
//        private static final String APP_SECRET = "aed4986840ce3b9669d945a0b24091b7";
    //资料馆
    private static final String APP_ID = "15464";
    private static final String APP_SECRET = "a35047670aaaae15b87140e188fa3316";
    private static final int MSG_SEARCH_RESULT = 100;
    private static final int MSG_CONNECT_FAILURE = 101;
    private static final int MSG_CONNECT_SUCCESS = 102;
    private static final int MSG_UPDATE_PROGRESS = 103;
    private static final int MSG_SEARCH_STOP = 105;
    private boolean isplay=false;
    private boolean loadingSucess=false;

    private List<LelinkServiceInfo> mLelinkServiceInfoList;


    private UIHandler mUiHandler;
    private Dialog mDeviceDialog;
    private BrowseAdapter mBrowseAdapter;
    private TextView network;
    private TextView statue;
    private TextView null_back;
    private RecyclerView deviceRecy;
    private LinearLayout setting;
    private LinearLayout bottom;


    private Dialog controlDialog;


    private SeekBar mProgressBar;
    private ImageView play;
    private ImageView refresh;
    private TextView current;
    private TextView total;
    private int dupro=0;
    private int seekto=0;


    private boolean isSelectClient = false;
    private boolean adapterClick = false;




    public HpplayUtils(Activity context, BaseVideoPlayer baseVideoPlayer) {
        mContext = context;
        this.baseVideoPlayer = baseVideoPlayer;
        try {
            initHpSdk();
        }catch (Exception e){
            e.printStackTrace();
        }
        mUiHandler = new UIHandler(context);
    }
    public void initHpSdk(){
        //sdk初始化
        LelinkSourceSDK.getInstance().bindSdk(mContext.getApplicationContext(), APP_ID, APP_SECRET, new IBindSdkListener() {
            @Override
            public void onBindCallback(boolean b) {
                LelinkSourceSDK.getInstance().setDebugMode(true);
                LeLog.i("onBindCallback", "--------->" + b);
                if (b) {
                    LelinkSourceSDK.getInstance().setBrowseResultListener(iBrowseListener);
                    LelinkSourceSDK.getInstance().setConnectListener(iConnectListener);
                    LelinkSourceSDK.getInstance().setPlayListener(lelinkPlayerListener);
                }
            }
        });
    }
    private IBrowseListener iBrowseListener = new IBrowseListener() {

        @Override
        public void onBrowse(int i, List<LelinkServiceInfo> list) {
            LeLog.i("-------------->list size :", i+"------" + list);
            mLelinkServiceInfoList = list;
            switch (i){
                case IBrowseListener.BROWSE_ERROR_AUTH:
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "授权失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case IBrowseListener.BROWSE_SUCCESS:
                    if (mUiHandler != null) {
                        mUiHandler.sendMessage(Message.obtain(null, MSG_SEARCH_RESULT, list));
                    }
                    break;
//                case IBrowseListener.BROWSE_STOP:
//                    if (mUiHandler != null) {
//                        mUiHandler.sendMessage(Message.obtain(null, MSG_SEARCH_STOP, list));
//                    }
//                    break;
                case IBrowseListener.BROWSE_TIMEOUT:
                    if (mUiHandler != null) {
                        mUiHandler.sendMessage(Message.obtain(null, MSG_SEARCH_STOP, list));
                    }
                    break;

            }
        }

    };
    private IConnectListener iConnectListener = new IConnectListener() {
        @Override
        public void onConnect(LelinkServiceInfo lelinkServiceInfo, int extra) {
            LeLog.d(TAG, "onConnect:" + lelinkServiceInfo.getName());
            if (mUiHandler != null) {
                mUiHandler.sendMessage(Message.obtain(null, MSG_CONNECT_SUCCESS, extra, 0, lelinkServiceInfo));
            }
        }
        @Override
        public void onDisconnect(LelinkServiceInfo lelinkServiceInfo, int what, int extra) {
            LeLog.d(TAG, "onDisconnect:" + lelinkServiceInfo.getName() + " disConnectType:" + what + " extra:" + extra);
            String text = null;
            if (what == IConnectListener.CONNECT_INFO_DISCONNECT) {
                if (null != mUiHandler) {
                    if (TextUtils.isEmpty(lelinkServiceInfo.getName())) {
                        text = "pin码连接断开";
                    } else {
                        text = lelinkServiceInfo.getName() + "连接断开";
                    }
                }
            } else if (what == IConnectListener.CONNECT_ERROR_FAILED) {
                if (extra == IConnectListener.CONNECT_ERROR_IO) {
                    text = lelinkServiceInfo.getName() + "连接失败";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_WAITTING) {
                    text = lelinkServiceInfo.getName() + "等待确认";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_REJECT) {
                    text = lelinkServiceInfo.getName() + "连接拒绝";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_TIMEOUT) {
                    text = lelinkServiceInfo.getName() + "连接超时";
                } else if (extra == IConnectListener.CONNECT_ERROR_IM_BLACKLIST) {
                    text = lelinkServiceInfo.getName() + "连接黑名单";
                }
            }
            if (null != mUiHandler) {
                mUiHandler.sendMessage(Message.obtain(null, MSG_CONNECT_FAILURE, text));
            }
        }
    };
    ILelinkPlayerListener lelinkPlayerListener = new ILelinkPlayerListener() {


        @Override
        public void onLoading() {
            //            loadingSucess = true;
            isplay = false;
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "开始加载", Toast.LENGTH_SHORT).show();
                    play.setImageResource(R.drawable.video_stop);
                    baseVideoPlayer.onVideoPause();
                }
            });
        }

        @Override
        public void onStart() {
            isplay = true;
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    play.setImageResource(R.drawable.video_play);
                    if (seekto!=0){
                        LelinkSourceSDK.getInstance().seekTo(seekto);
                        seekto = 0;
                    }
                }
            });
        }

        @Override
        public void onPause() {
            isplay = false;
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    play.setImageResource(R.drawable.video_stop);
                }
            });

        }

        @Override
        public void onCompletion() {//播放完成
            isplay = false;
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    play.setImageResource(R.drawable.video_stop);
                    controlDialog.dismiss();
                }
            });
        }

        @Override
        public void onStop() {
            isplay = false;
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "播放停止", Toast.LENGTH_SHORT).show();
                    play.setImageResource(R.drawable.video_stop);
                }
            });
        }

        @Override
        public void onSeekComplete(int i) {}
        @Override
        public void onInfo(int i, int i1) {}
        @Override
        public void onInfo(int i, String s) {}
        String text = null;

        @Override
        public void onError(int what, int extra) {
            Log.d(TAG, "onError what:" + what + " extra:" + extra);
            if (what == PUSH_ERROR_INIT) {
                if (extra == PUSH_ERRROR_FILE_NOT_EXISTED) {
                    text = "文件不存在";
                } else if (extra == PUSH_ERROR_IM_OFFLINE) {
                    text = "IM TV不在线";
                } else if (extra == PUSH_ERROR_IMAGE) {

                } else if (extra == PUSH_ERROR_IM_UNSUPPORTED_MIMETYPE) {
                    text = "IM不支持的媒体类型";
                } else {
                    text = "未知";
                }
            } else if (what == MIRROR_ERROR_INIT) {
                if (extra == MIRROR_ERROR_UNSUPPORTED) {
                    text = "不支持镜像";
                } else if (extra == MIRROR_ERROR_REJECT_PERMISSION) {
                    text = "镜像权限拒绝";
                } else if (extra == MIRROR_ERROR_DEVICE_UNSUPPORTED) {
                    text = "设备不支持镜像";
                } else if (extra == NEED_SCREENCODE) {
                    text = "请输入投屏码";
                }
            } else if (what == MIRROR_ERROR_PREPARE) {
                if (extra == MIRROR_ERROR_GET_INFO) {
                    text = "获取镜像信息出错";
                } else if (extra == MIRROR_ERROR_GET_PORT) {
                    text = "获取镜像端口出错";
                } else if (extra == NEED_SCREENCODE) {
                    text = "请输入投屏码";
                    if (extra == PREEMPT_UNSUPPORTED) {
                        text = "投屏码模式不支持抢占";
                    }
                } else if (what == PUSH_ERROR_PLAY) {
                    if (extra == PUSH_ERROR_NOT_RESPONSED) {
                        text = "播放无响应";
                    } else if (extra == NEED_SCREENCODE) {
                        text = "请输入投屏码";

                    } else if (extra == RELEVANCE_DATA_UNSUPPORTED) {
                        text = "老乐联不支持数据透传,请升级接收端的版本！";
                    } else if (extra == ILelinkPlayerListener.PREEMPT_UNSUPPORTED) {
                        text = "投屏码模式不支持抢占";
                    }
                } else if (what == PUSH_ERROR_STOP) {
                    if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                        text = "退出 播放无响应";
                    }
                } else if (what == PUSH_ERROR_PAUSE) {
                    if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                        text = "暂停无响应";
                    }
                } else if (what == PUSH_ERROR_RESUME) {
                    if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                        text = "恢复无响应";
                    }
                }

            } else if (what == MIRROR_PLAY_ERROR) {
                if (extra == MIRROR_ERROR_FORCE_STOP) {
                    text = "接收端断开";
                } else if (extra == MIRROR_ERROR_PREEMPT_STOP) {
                    text = "镜像被抢占";
                }
            } else if (what == MIRROR_ERROR_CODEC) {
                if (extra == MIRROR_ERROR_NETWORK_BROKEN) {
                    text = "镜像网络断开";
                }
            }
            if (null != mUiHandler) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
        @Override
        public void onVolumeChanged(float v) {}
        @Override
        public void onPositionUpdate(long l, long l1) {
            if (mUiHandler != null) {
                Message msg = new Message();
                msg.what = MSG_UPDATE_PROGRESS;
                msg.arg1 = (int) l;
                msg.arg2 = (int) l1;
                mUiHandler.sendMessage(msg);
            }
        }
    };

    private class UIHandler extends Handler {

        private WeakReference<Activity> mReference;

        UIHandler(Activity reference) {
            mReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity mainActivity = mReference.get();
            if (mainActivity == null) {
                return;
            }
            switch (msg.what) {
                case MSG_SEARCH_RESULT:
                    try {
                        if (msg.obj != null) {
                            List<LelinkServiceInfo> obj = (List<LelinkServiceInfo>) msg.obj;
                            if (obj.size()>0){
                                deviceRecy.setVisibility(View.VISIBLE);
                                null_back.setVisibility(View.GONE);
                                bottom.setVisibility(View.GONE);
                                mBrowseAdapter.updateDatas(obj);
                            }else {
                                deviceRecy.setVisibility(View.GONE);
                                null_back.setVisibility(View.VISIBLE);
                                bottom.setVisibility(View.VISIBLE);
                            }
                            //                            refresh.clearAnimation();
                        }
                    } catch (Exception e) {
                        LeLog.w(TAG, e);
                    }
                    break;
                case MSG_SEARCH_STOP:
                    List<LelinkServiceInfo> obj = (List<LelinkServiceInfo>) msg.obj;
                    if (obj.size()>0){
                        deviceRecy.setVisibility(View.VISIBLE);
                        null_back.setVisibility(View.GONE);
                        bottom.setVisibility(View.GONE);
                        mBrowseAdapter.updateDatas(obj);
                    }else {
                        deviceRecy.setVisibility(View.GONE);
                        null_back.setVisibility(View.VISIBLE);
                        bottom.setVisibility(View.VISIBLE);
                    }
                    break;
                case MSG_CONNECT_SUCCESS:
                    try {
                        if (msg.obj != null) {
                            LelinkServiceInfo serviceInfo = (LelinkServiceInfo) msg.obj;
                            String type = msg.arg1 == IConnectListener.TYPE_LELINK ? "Lelink"
                                    : msg.arg1 == IConnectListener.TYPE_DLNA ? "DLNA"
                                    : msg.arg1 == IConnectListener.TYPE_NEW_LELINK ? "NEW_LELINK" : "IM";
                            //开始播放
                            isSelectClient = true;
                            mDeviceDialog.dismiss();
                            playVideo(serviceInfo);
                        }
                    } catch (Exception e) {
                        LeLog.w(TAG, e);
                    }
                    break;
                case MSG_CONNECT_FAILURE:
                    adapterClick = false;
                    if (msg.obj != null) {
                        Toast.makeText(mainActivity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MSG_UPDATE_PROGRESS:
                    if (mProgressBar!=null){
                        dupro = msg.arg2;
                        mProgressBar.setMax(msg.arg1);
                        mProgressBar.setProgress(msg.arg2);
                        total.setText(CommonUtil.stringForTime(msg.arg1*1000));
                        current.setText(CommonUtil.stringForTime(msg.arg2*1000));
                    }
                    break;
            }
            super.handleMessage(msg);
        }

    }

    private void playVideo(LelinkServiceInfo mSelectInfo){
        String url = "";
        //获取随机code
        IjkMediaPlayer ijkMediaPlayer = baseVideoPlayer.getIjkMediaPlayer();
        long code = ijkMediaPlayer._getPropertyLong(20212, 0);
        if (code!=0){
            url = getLocalUrl(mContext,baseVideoPlayer.playurl);
            if (url==null){
                return;
            }
        }else {
            url = baseVideoPlayer.playurl;
        }
        System.out.println(url+"-----------------------");
        Toast.makeText(mContext,  mSelectInfo.getName() + "连接成功", Toast.LENGTH_SHORT).show();
        LelinkPlayerInfo lelinkPlayerInfo = new LelinkPlayerInfo();
        lelinkPlayerInfo.setUrl(url);
        lelinkPlayerInfo.setType(LelinkSourceSDK.MEDIA_TYPE_VIDEO);
        lelinkPlayerInfo.setLelinkServiceInfo(mSelectInfo);
        LelinkSourceSDK.getInstance().startPlayMedia(lelinkPlayerInfo);
        //展示弹框
        showControlDialog(mSelectInfo.getName());
        adapterClick = false;
    }
    /**
     * 控制界面
     * @param dname
     */
    private void showControlDialog(String dname) {
        View localView = LayoutInflater.from(mContext).inflate(R.layout.controleview, null);
        controlDialog = new Dialog(mContext, R.style.hpplay_style_dialog_progress);
        controlDialog.setContentView(localView);
        WindowManager.LayoutParams localLayoutParams = controlDialog.getWindow().getAttributes();
        localLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        controlDialog.getWindow().setAttributes(localLayoutParams);
        //获取组件
        TextView devicename = localView.findViewById(R.id.device_name);
        TextView videoname = localView.findViewById(R.id.video_name);
        total = localView.findViewById(R.id.total);
        current = localView.findViewById(R.id.current);
        localView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlDialog.dismiss();
                baseVideoPlayer.onResume();
                LelinkSourceSDK.getInstance().stopPlay();
            }
        });
        RemoteControlMenu remoteControlMenu= localView.findViewById(R.id.controller_view);
        remoteControlMenu.setListener(new RemoteControlMenu.MenuListener() {
            @Override
            public void onMenuClicked(int type) {
                switch (type){
                    case RemoteControlMenu.TouchArea.CENTER:
                        if (isplay) {
                            LelinkSourceSDK.getInstance().pause();
                        } else {
                            LelinkSourceSDK.getInstance().resume();
                        }
                        break;
                    case RemoteControlMenu.TouchArea.TOP:
                        LelinkSourceSDK.getInstance().addVolume();
                        break;
                    case RemoteControlMenu.TouchArea.BOTTOM:
                        LelinkSourceSDK.getInstance().subVolume();
                        break;
                    case RemoteControlMenu.TouchArea.LEFT:
                        dupro -= 15;
                        LelinkSourceSDK.getInstance().seekTo(dupro < 0 ? 0 : dupro);
                        break;
                    case RemoteControlMenu.TouchArea.RIGHT:
                        dupro+=15;
                        LelinkSourceSDK.getInstance().seekTo(dupro);
                        break;
                }

            }
        });

        mProgressBar= localView.findViewById(R.id.progress);
        play = localView.findViewById(R.id.play);
        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    LelinkSourceSDK.getInstance().seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        videoname.setText(baseVideoPlayer.getTitleTextView().getText().toString());
        devicename.setText(dname);
        // 显示监听
        controlDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //                baseVideoPlayer.onResume();
                //关闭弹框，停止投屏，同步进度
                LelinkSourceSDK.getInstance().stopPlay();
                baseVideoPlayer.setSeekOnStart(dupro*1000);
                baseVideoPlayer.startPlayLogic();
            }
        });

        if (!controlDialog.isShowing()) {
            controlDialog.show();
        }
    }

    /**
     * 展示设备dialog
     */
    public void showDeviceDialog() {
        baseVideoPlayer.onPause();
        LelinkSourceSDK.getInstance().startBrowse();
        if (mDeviceDialog == null) {
            View localView = LayoutInflater.from(mContext).inflate(R.layout.video_hpplay_dialog, null);
            mDeviceDialog = new Dialog(mContext, R.style.hpplay_style_dialog_progress);
            mDeviceDialog.setContentView(localView);

            WindowManager.LayoutParams localLayoutParams = mDeviceDialog.getWindow().getAttributes();
            localLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            localLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mDeviceDialog.getWindow().setAttributes(localLayoutParams);

            statue = localView.findViewById(R.id.statue);
            network = localView.findViewById(R.id.network);
            setting = localView.findViewById(R.id.setting);
            null_back = localView.findViewById(R.id.null_back);
            deviceRecy = localView.findViewById(R.id.device);
            refresh = localView.findViewById(R.id.refresh);
            bottom = localView.findViewById(R.id.bottom);

            deviceRecy.setLayoutManager(new LinearLayoutManager(mContext));
            mBrowseAdapter = new BrowseAdapter(mContext);
            deviceRecy.setAdapter(mBrowseAdapter);
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //开始搜索动画
                    startAnimation();
                    //搜索设备
                    LelinkSourceSDK.getInstance().startBrowse();
                }
            });
            localView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDeviceDialog.dismiss();
                }
            });
            mBrowseAdapter.setOnItemClickListener(new BrowseAdapter.OnItemClickListener() {
                @Override
                public void onClick(int position, LelinkServiceInfo pInfo) {
                    if (adapterClick) {//已经选择设备了
                        Toast.makeText(mContext,"您已选择投屏设备，请稍后",Toast.LENGTH_LONG).show();
                        return;
                    }
                    adapterClick = true;
                    LelinkSourceSDK.getInstance().connect(pInfo);
                }
            });
            setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                    mContext.startActivity(intent);
                }
            });
        }
        if (!mDeviceDialog.isShowing()) {
            mDeviceDialog.show();
            //开始搜索动画
            startAnimation();
        }
        mDeviceDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!isSelectClient) {
                    baseVideoPlayer.onResume();
                }
                isSelectClient = false;
                LelinkSourceSDK.getInstance().stopBrowse();
            }
        });
    }

    /**
     * 开始搜索动画
     */
    private void startAnimation() {
        //初始化网络连接
        networkInfo = ((ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo!=null) {
            if (networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                //wifi连接
                statue.setText("搜索可投屏设备");
                network.setText("当前Wi-Fi："+IpGetUtil.getWifiName(mContext));
                setting.setVisibility(View.GONE);
                deviceRecy.setVisibility(View.GONE);
                null_back.setVisibility(View.GONE);
                bottom.setVisibility(View.GONE);
                //创建旋转动画
                RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (mUiHandler != null) {
                            mUiHandler.sendMessage(Message.obtain(null, MSG_SEARCH_RESULT, mLelinkServiceInfoList));
                        }
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });

                LinearInterpolator lin = new LinearInterpolator();
                rotate.setInterpolator(lin);
                rotate.setDuration(1000);//设置动画持续周期
                rotate.setRepeatCount(10);//设置重复次数
                rotate.setFillAfter(true);//动画执行完后是否停留在执行完的状态
                rotate.setStartOffset(10);//执行前的等待时间
                refresh.startAnimation(rotate);//开始动画
            } else {
                statue.setText("未连接Wi-Fi，");
                network.setText("当前是移动数据网络");
                setting.setVisibility(View.VISIBLE);
                deviceRecy.setVisibility(View.GONE);
                null_back.setVisibility(View.GONE);
                bottom.setVisibility(View.VISIBLE);
            }
        }else {
            statue.setText("未连接Wi-Fi，");
            network.setText("当前无可用网络");
            setting.setVisibility(View.VISIBLE);
            deviceRecy.setVisibility(View.GONE);
            null_back.setVisibility(View.GONE);
            bottom.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏设备dialog
     */
    public void dismissDeviceDialog(){
        if (mDeviceDialog!=null){
            mDeviceDialog.dismiss();
        }
    }

    public Dialog getControlDialog() {
        return controlDialog;
    }

    public Dialog getDeviceDialog() {
        return mDeviceDialog;
    }

    /**
     * 快进到
     */
    public void seekTo(int seekto){
        System.out.println("------投屏-----"+seekto);
        this.seekto = seekto;
    }

    /**
     * 获取ip地址
     * @param context
     * @return
     */
    private String getLocalUrl(Context context,String url) {
        String ht="";
        String domain="";
        String ip=IpGetUtil.getIPAddress(context);
        if (ip==null){
            return null;
        }
        ht = url.split("//")[0];
        domain = url.split("//")[1].split("/")[0];
        //返回播放地址
        return url.replace(ht + "//" + domain, "http://" + ip + ":10024");
    }
}
