package com.video.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
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
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.video.adapter.BrowseAdapter;
import com.video.view.BaseVideoPlayer;
import com.video.view.RemoteControlMenu;

import java.lang.ref.WeakReference;
import java.util.List;

public class HpplayUtils implements View.OnClickListener {
    private Context mContext;
    private BaseVideoPlayer baseVideoPlayer;
    private static final String TAG = "hputils";
    private static final String APP_ID = "15450";
    private static final String APP_SECRET = "aed4986840ce3b9669d945a0b24091b7";
    private static final int MSG_SEARCH_RESULT = 100;
    private static final int MSG_CONNECT_FAILURE = 101;
    private static final int MSG_CONNECT_SUCCESS = 102;
    private static final int MSG_UPDATE_PROGRESS = 103;
    private boolean isplay=false;
    private boolean loadingSucess=false;


    private UIHandler mUiHandler;
    private Dialog mDeviceDialog;
    private BrowseAdapter mBrowseAdapter;
    private TextView tip;
    private RecyclerView deviceRecy;

    private Dialog controlDialog;


    private SeekBar mProgressBar;
    private ImageView play;
    private TextView current;
    private TextView total;
    private int dupro=0;
    private int seekto=0;



    public HpplayUtils(Activity context, BaseVideoPlayer baseVideoPlayer) {
        mContext = context;
        this.baseVideoPlayer = baseVideoPlayer;
        initHpSdk();
        mUiHandler = new UIHandler(context);
    }
    public void initHpSdk(){
        //sdk初始化
        LelinkSourceSDK.getInstance().bindSdk(mContext.getApplicationContext(), APP_ID, APP_SECRET, new IBindSdkListener() {
            @Override
            public void onBindCallback(boolean b) {
                LeLog.i("onBindCallback", "--------->" + b);
                if (b) {
                    LelinkSourceSDK.getInstance().setBrowseResultListener(iBrowseListener);
                    LelinkSourceSDK.getInstance().setConnectListener(iConnectListener);
                    LelinkSourceSDK.getInstance().setPlayListener(lelinkPlayerListener);
                }
                LelinkSourceSDK.getInstance().setDebugMode(true);
            }
        });
    }
    private IBrowseListener iBrowseListener = new IBrowseListener() {

        @Override
        public void onBrowse(int i, List<LelinkServiceInfo> list) {
            if (i == IBrowseListener.BROWSE_ERROR_AUTH) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "授权失败", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            if (mUiHandler != null) {
                mUiHandler.sendMessage(Message.obtain(null, MSG_SEARCH_RESULT, list));
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
            loadingSucess = true;
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
                    LelinkSourceSDK.getInstance().seekTo(seekto);
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
        public void onCompletion() {
            isplay = false;
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    play.setImageResource(R.drawable.video_stop);
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
        public void onSeekComplete(int i) {

        }

        @Override
        public void onInfo(int i, int i1) {

        }

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
        public void onVolumeChanged(float v) {

        }

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

    @Override
    public void onClick(View v) {
        if (loadingSucess) {
            int id = v.getId();
            if (id == R.id.left) {
                int i = dupro - 15;
                LelinkSourceSDK.getInstance().seekTo(i < 0 ? 0 : i);
            } else if (id == R.id.right) {
                LelinkSourceSDK.getInstance().seekTo(dupro + 15);
            } else if (id == R.id.play) {
                if (isplay) {
                    LelinkSourceSDK.getInstance().pause();
                } else {
                    LelinkSourceSDK.getInstance().resume();
                }
            } else if (id == R.id.top) {
                LelinkSourceSDK.getInstance().addVolume();
            } else if (id == R.id.bottom) {
                LelinkSourceSDK.getInstance().subVolume();
            }
        }
    }

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
                            tip.setText("搜索完成");
                            mBrowseAdapter.updateDatas((List<LelinkServiceInfo>) msg.obj);
                        }
                    } catch (Exception e) {
                        LeLog.w(TAG, e);
                    }
                    break;
                case MSG_CONNECT_SUCCESS:
                    try {
                        if (msg.obj != null) {
                            LelinkServiceInfo serviceInfo = (LelinkServiceInfo) msg.obj;
                            String type = msg.arg1 == IConnectListener.TYPE_LELINK ? "Lelink"
                                    : msg.arg1 == IConnectListener.TYPE_DLNA ? "DLNA"
                                    : msg.arg1 == IConnectListener.TYPE_NEW_LELINK ? "NEW_LELINK" : "IM";
                            Toast.makeText(mainActivity,  serviceInfo.getName() + "连接成功", Toast.LENGTH_SHORT).show();
                            //开始播放
                            mDeviceDialog.dismiss();
                            playVideo(serviceInfo);
                            showControlDialog(serviceInfo.getName());
                        }
                    } catch (Exception e) {
                        LeLog.w(TAG, e);
                    }
                    break;
                case MSG_CONNECT_FAILURE:
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
        LelinkPlayerInfo lelinkPlayerInfo = new LelinkPlayerInfo();
//        lelinkPlayerInfo.setUrl("http://39.97.243.76/m3u8enc_t/EE722E4DD669BA18696440BE98DD09E4_1.m3u8");
        lelinkPlayerInfo.setUrl("http://211.148.220.133/cntv/media/new/2013/icntv2/media/newmedia/1.8M/2020/01/02/666291890.m3u8");
        lelinkPlayerInfo.setType(LelinkSourceSDK.MEDIA_TYPE_VIDEO);
        lelinkPlayerInfo.setLelinkServiceInfo(mSelectInfo);
        LelinkSourceSDK.getInstance().startPlayMedia(lelinkPlayerInfo);
    }
    /**
     * 控制界面
     * @param dname
     */
    private void showControlDialog(String dname) {
        View localView = LayoutInflater.from(mContext).inflate(R.layout.controleview, null);
        controlDialog = new Dialog(mContext, R.style.video_style_dialog_progress);
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
        localView.findViewById(R.id.left).setOnClickListener(this);
        localView.findViewById(R.id.right).setOnClickListener(this);
        localView.findViewById(R.id.top).setOnClickListener(this);
        localView.findViewById(R.id.bottom).setOnClickListener(this);
        localView.findViewById(R.id.play).setOnClickListener(this);
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
            mDeviceDialog = new Dialog(mContext, R.style.video_style_dialog_progress);
            mDeviceDialog.setContentView(localView);

            WindowManager.LayoutParams localLayoutParams = mDeviceDialog.getWindow().getAttributes();
            localLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            localLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mDeviceDialog.getWindow().setAttributes(localLayoutParams);

            tip = localView.findViewById(R.id.tip);
            deviceRecy = localView.findViewById(R.id.device);
            deviceRecy.setLayoutManager(new LinearLayoutManager(mContext));
            mBrowseAdapter = new BrowseAdapter(mContext);
            deviceRecy.setAdapter(mBrowseAdapter);
            localView.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tip.setText("搜索设备中");
                    LelinkSourceSDK.getInstance().startBrowse();
                }
            });
            localView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDeviceDialog.dismiss();
                    baseVideoPlayer.onResume();
                }
            });
            mBrowseAdapter.setOnItemClickListener(new BrowseAdapter.OnItemClickListener() {
                @Override
                public void onClick(int position, LelinkServiceInfo pInfo) {
                    LelinkSourceSDK.getInstance().connect(pInfo);
                }
            });
        }
        if (!mDeviceDialog.isShowing()) {
            mDeviceDialog.show();
        }
        mDeviceDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                LelinkSourceSDK.getInstance().stopBrowse();
            }
        });
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

    /**
     * 快进到
     */
    public void seekTo(int seekto){
        this.seekto = seekto;
    }
}
