package com.video.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hpplay.common.utils.LeLog;
import com.hpplay.sdk.source.api.IBindSdkListener;
import com.hpplay.sdk.source.api.IConnectListener;
import com.hpplay.sdk.source.api.ILelinkPlayerListener;
import com.hpplay.sdk.source.api.LelinkSourceSDK;
import com.hpplay.sdk.source.browse.api.IBrowseListener;
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;
import com.huoyan.basevideo.R;
import com.huoyan.basevideo.R.drawable;
import com.huoyan.basevideo.R.id;
import com.huoyan.basevideo.R.layout;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.video.adapter.BrowseAdapter;
import com.video.adapter.SpeedAndResolutionAdapter;
import com.video.adapter.SpeedAndResolutionAdapter.SelectListener;
import com.video.model.ResolutionModel;
import com.video.utils.HpplayUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BaseVideoPlayer extends StandardGSYVideoPlayer {
    private Activity mActivity;
    private GSYVideoOptionBuilder gsyVideoOptionBuilder;
    private OrientationUtils orientationUtils;
    private boolean isPlay;
    private boolean isPause;
    private boolean isCache = false;
    private ImageView airplay;
    private ImageView share;
    private ImageView bottom_start;
    private ImageView takeScreen;
    private ImageView more;
    private SeekBar mVloum;
    private SeekBar mLight;
    private LinearLayout volumLight;
    private BaseVideoPlayer.TakeScreenTimer mTakeScreenTimer = new BaseVideoPlayer.TakeScreenTimer();
    private com.video.view.MyBatterView mBatterView;
    private TextView time;
    private TextView speed;
    private TextView resolution;
    private RecyclerView selecRecycleView;
    private SpeedAndResolutionAdapter mSpeedAndResolutionAdapter;
    private List<ResolutionModel> mResolutionList;
    private String speedText = "倍速";
    private String resolutionText = "";
    private BaseVideoPlayer.TakeScreenListener mTakeScreenListener;
    private BaseVideoPlayer.ShareListener mShareListener;


    private HpplayUtils mHpplayUtils;
    private boolean clickAirPlay =false;




    public BaseVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public BaseVideoPlayer(Context context) {
        super(context);
    }

    public BaseVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void init(Context context) {
        super.init(context);
        this.takeScreen = (ImageView)this.findViewById(id.take_screen);
        this.bottom_start = (ImageView)this.findViewById(id.bottom_start);
        this.more = (ImageView)this.findViewById(id.more);
        this.airplay = (ImageView)this.findViewById(id.airplay);
        this.volumLight = (LinearLayout)this.findViewById(id.volum_light);
        this.time = (TextView)this.findViewById(id.time);
        this.mBatterView = (com.video.view.MyBatterView)this.findViewById(id.batterView);
        this.speed = (TextView)this.findViewById(id.speed);
        this.share = (ImageView)this.findViewById(id.share);
        this.resolution = (TextView)this.findViewById(id.resolution);
        this.mLight = (SeekBar)this.findViewById(id.s_light);
        this.mVloum = (SeekBar)this.findViewById(id.s_volum);
        this.selecRecycleView = (RecyclerView)this.findViewById(id.select_recycle);
        this.selecRecycleView.setLayoutManager(new LinearLayoutManager(context));
        this.mSpeedAndResolutionAdapter = new SpeedAndResolutionAdapter(context);
        this.selecRecycleView.setAdapter(this.mSpeedAndResolutionAdapter);
        this.mSpeedAndResolutionAdapter.setSelectListener(new SelectListener() {
            public void speed(float sp) {
                String st = sp + "x";
                String sst = BaseVideoPlayer.this.speed.getText().toString().equals("倍速") ? "1.0x" : BaseVideoPlayer.this.speed.getText().toString();
                if (!st.equals(sst)) {
                    BaseVideoPlayer.this.speed.setText(st);
                    BaseVideoPlayer.this.setSpeed(sp);
                    if ((double)sp == 1.0D) {
                        Toast.makeText(BaseVideoPlayer.this.getContext(), "已恢复正常播放速度", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(BaseVideoPlayer.this.getContext(), "已切换为" + sp + "倍速播放", Toast.LENGTH_LONG).show();
                    }

                    BaseVideoPlayer.this.onClickUiToggle();
                }

                BaseVideoPlayer.this.selecRecycleView.setVisibility(GONE);
            }

            public void resolution(ResolutionModel res) {
                if (!res.resolutionName.equals(BaseVideoPlayer.this.resolution.getText().toString())) {
                    BaseVideoPlayer.this.resolution.setText(res.resolutionName);
                    long currentPosition = BaseVideoPlayer.this.getGSYVideoManager().getCurrentPosition();
                    BaseVideoPlayer.this.setUp(res.url, BaseVideoPlayer.this.isCache, BaseVideoPlayer.this.mTitle);
                    BaseVideoPlayer.this.setSeekOnStart(currentPosition);
                    BaseVideoPlayer.this.startPlayLogic();
                    BaseVideoPlayer.this.onClickUiToggle();
                    Toast.makeText(BaseVideoPlayer.this.getContext(), "清晰度已切换至" + res.resolutionName + res.resolution, Toast.LENGTH_LONG).show();
                }

                BaseVideoPlayer.this.selecRecycleView.setVisibility(GONE);
            }
        });
        this.takeScreen.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BaseVideoPlayer.this.taskShotPic(new GSYVideoShotListener() {
                    public void getBitmap(Bitmap bitmap) {
                        if (BaseVideoPlayer.this.mTakeScreenListener != null) {
                            BaseVideoPlayer.this.mTakeScreenListener.takeScreen(bitmap);
                        }

                        if (BaseVideoPlayer.this.mCurrentState != 6) {
                            BaseVideoPlayer.this.hideAllWidget();
                            BaseVideoPlayer.this.mTakeScreenTimer.run();
                        }

                    }
                });
            }
        });
        this.speed.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (BaseVideoPlayer.this.mCurrentState != 6) {
                    String st = BaseVideoPlayer.this.speed.getText().toString();
                    BaseVideoPlayer.this.mSpeedAndResolutionAdapter.initSpeed(st.equals("倍速") ? "1.0x" : st);
                    BaseVideoPlayer.this.selecRecycleView.setVisibility(VISIBLE);
                    BaseVideoPlayer.this.hideAllWidget();
                    BaseVideoPlayer.this.mTakeScreenTimer.run();
                }

            }
        });
        this.resolution.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (BaseVideoPlayer.this.mCurrentState != 6) {
                    BaseVideoPlayer.this.mSpeedAndResolutionAdapter.initResoultion(BaseVideoPlayer.this.mResolutionList, BaseVideoPlayer.this.resolution.getText().toString());
                    BaseVideoPlayer.this.selecRecycleView.setVisibility(VISIBLE);
                    BaseVideoPlayer.this.hideAllWidget();
                    BaseVideoPlayer.this.mTakeScreenTimer.run();
                }

            }
        });
        this.bottom_start.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BaseVideoPlayer.this.mStartButton.callOnClick();
            }
        });
        this.share.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (BaseVideoPlayer.this.mShareListener != null) {
                    BaseVideoPlayer.this.mShareListener.Share(v);
                    if (BaseVideoPlayer.this.mCurrentState != 6) {
                        BaseVideoPlayer.this.hideAllWidget();
                        BaseVideoPlayer.this.mTakeScreenTimer.run();
                    }
                }

            }
        });
        this.airplay.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (BaseVideoPlayer.this.mCurrentState != 6) {
                    BaseVideoPlayer.this.hideAllWidget();
                    BaseVideoPlayer.this.mTakeScreenTimer.run();
                    if (mIfCurrentIsFullscreen){
                        if (BaseVideoPlayer.this.orientationUtils != null) {
                            clickAirPlay = true;
                            BaseVideoPlayer.this.orientationUtils.resolveByClick();
//                            backFromFull(getContext());
                            return;
                        }
                    }
                    if (mHpplayUtils!=null) {
                        //展示设备弹框
                        mHpplayUtils.showDeviceDialog();
                        int duration = getDuration();
                        mHpplayUtils.seekTo(duration/10000);
                    }
                }
            }
        });
        this.more.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (BaseVideoPlayer.this.mCurrentState != 6) {
                    BaseVideoPlayer.this.hideAllWidget();
                    BaseVideoPlayer.this.mTakeScreenTimer.run();
                    BaseVideoPlayer.this.volumLight.setVisibility(VISIBLE);
                    int streamMaxVolume = BaseVideoPlayer.this.mAudioManager.getStreamMaxVolume(3);
                    int streamVolume = BaseVideoPlayer.this.mAudioManager.getStreamVolume(3);
                    BaseVideoPlayer.this.mVloum.setMax(streamMaxVolume);
                    BaseVideoPlayer.this.mVloum.setProgress(streamVolume);
                    BaseVideoPlayer.this.mLight.setMax(100);
                    BaseVideoPlayer.this.mLight.setProgress(BaseVideoPlayer.this.getSystemBrightness());
                }

            }
        });
        this.mVloum.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                BaseVideoPlayer.this.mAudioManager.setStreamVolume(3, progress, 0);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.mLight.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    BaseVideoPlayer.this.changeAppBrightness(progress);
                }

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.post(new Runnable() {
            public void run() {
                BaseVideoPlayer.this.gestureDetector = new GestureDetector(BaseVideoPlayer.this.getContext().getApplicationContext(), new SimpleOnGestureListener() {
                    public boolean onDoubleTap(MotionEvent e) {
                        BaseVideoPlayer.this.touchDoubleUp();
                        BaseVideoPlayer.this.selecRecycleView.setVisibility(GONE);
                        return super.onDoubleTap(e);
                    }

                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (!BaseVideoPlayer.this.mChangePosition && !BaseVideoPlayer.this.mChangeVolume && !BaseVideoPlayer.this.mBrightness) {
                            BaseVideoPlayer.this.onClickUiToggle();
                        }

                        return super.onSingleTapConfirmed(e);
                    }

                    public void onLongPress(MotionEvent e) {
                        super.onLongPress(e);
                    }
                });
            }
        });
    }

    private int getSystemBrightness() {
        WindowManager.LayoutParams lpa = ((Activity)((Activity)this.mContext)).getWindow().getAttributes();
        int systemBrightness = (int)(lpa.screenBrightness * 100.0F);
        return systemBrightness;
    }

    private void changeAppBrightness(int brightness) {
        float pp = brightness / 100.0F;
        mBrightnessData = pp;
        WindowManager.LayoutParams lpa = ((Activity) (mContext)).getWindow().getAttributes();
        lpa.screenBrightness = pp;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        ((Activity) (mContext)).getWindow().setAttributes(lpa);

    }

    protected void lockTouchLogic() {
        super.lockTouchLogic();
        if (this.mLockCurScreen) {
            this.takeScreen.setVisibility(GONE);
            this.mBottomProgressBar.setVisibility(VISIBLE);
        } else {
            this.mBottomProgressBar.setVisibility(GONE);
        }

    }

    public boolean onTouch(View v, MotionEvent event) {
        this.selecRecycleView.setVisibility(GONE);
        this.volumLight.setVisibility(GONE);
        return super.onTouch(v, event);
    }

    private void initSetting() {
        this.orientationUtils = new OrientationUtils(this.mActivity, this);
        this.orientationUtils.setEnable(false);
        this.gsyVideoOptionBuilder = (new GSYVideoOptionBuilder()).setIsTouchWiget(true).setRotateViewAuto(true).setLockLand(false).setShowFullAnimation(false).setNeedLockFull(true).setSeekRatio(1.0F).setDismissControlTime(6000).setCacheWithPlay(true).setVideoAllCallBack(new GSYSampleCallBack() {
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                BaseVideoPlayer.this.orientationUtils.setEnable(true);
                BaseVideoPlayer.this.isPlay = true;
            }

            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                if (BaseVideoPlayer.this.orientationUtils != null) {
                    BaseVideoPlayer.this.orientationUtils.backToProtVideo();
                }
            }
        });
        this.gsyVideoOptionBuilder.build(this);
        this.mFullscreenButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BaseVideoPlayer.this.orientationUtils.resolveByClick();
                BaseVideoPlayer.this.startWindowFullscreen(BaseVideoPlayer.this.getContext(), true, true);
            }
        });
        this.setLockClickListener(new LockClickListener() {
            public void onClick(View view, boolean lock) {
                if (BaseVideoPlayer.this.orientationUtils != null) {
                    BaseVideoPlayer.this.orientationUtils.setEnable(!lock);
                }

            }
        });
    }

    private void getPowerAndTime() {
        this.time.setText(this.getSystemTime());
        this.mBatterView.setPro(this.getSystemBattery(this.getContext()));
    }

    private int getSystemBattery(Context context) {
        Intent batteryInfoIntent = context.getApplicationContext().registerReceiver((BroadcastReceiver)null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        int level = batteryInfoIntent.getIntExtra("level", 0);
        int batterySum = batteryInfoIntent.getIntExtra("scale", 100);
        int percentBattery = 100 * level / batterySum;
        return percentBattery;
    }

    private String getSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public int getLayoutId() {
        return this.mIfCurrentIsFullscreen ? layout.sample_video_land : layout.sample_video_normal;
    }

    protected void updateStartImage() {
        super.updateStartImage();
        if (this.mCurrentState == 2) {
            this.bottom_start.setImageResource(drawable.video_play);
            this.mStartButton.setVisibility(GONE);
        } else if (this.mCurrentState == 7) {
            this.bottom_start.setImageResource(drawable.video_stop);
            this.mStartButton.setVisibility(VISIBLE);
        } else {
            this.bottom_start.setImageResource(drawable.video_stop);
            this.mStartButton.setVisibility(VISIBLE);
        }

        this.volumLight.setVisibility(GONE);
        this.selecRecycleView.setVisibility(GONE);
        //隐藏设备列表
        if (mHpplayUtils!=null) {
            mHpplayUtils.dismissDeviceDialog();
        }
    }

    protected void onClickUiToggle() {
        super.onClickUiToggle();
        if (this.mIfCurrentIsFullscreen) {
            this.takeScreen.setVisibility(this.mBottomContainer.getVisibility());
            this.selecRecycleView.setVisibility(GONE);
        } else {
            this.takeScreen.setVisibility(GONE);
        }

        this.volumLight.setVisibility(GONE);
        this.selecRecycleView.setVisibility(GONE);
    }

    protected void startDismissControlViewTimer() {
        this.cancelDismissControlViewTimer();
        this.mPostDismiss = true;
        this.postDelayed(this.mTakeScreenTimer, (long)this.mDismissControlTime);
        this.getPowerAndTime();
    }

    protected void cancelDismissControlViewTimer() {
        this.mPostDismiss = false;
        this.removeCallbacks(this.mTakeScreenTimer);
    }

    public int getEnlargeImageRes() {
        return drawable.full_screen1;
    }

    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        BaseVideoPlayer landLayoutVideo = (BaseVideoPlayer)super.startWindowFullscreen(context, actionBar, statusBar);
        landLayoutVideo.mResolutionList = this.mResolutionList;
        if (this.mResolutionList == null) {
            landLayoutVideo.resolution.setVisibility(GONE);
        }

        landLayoutVideo.takeScreen.setVisibility(VISIBLE);
        landLayoutVideo.mTakeScreenListener = this.mTakeScreenListener;
        landLayoutVideo.mShareListener = this.mShareListener;
        landLayoutVideo.resolution.setText(this.resolutionText);
        landLayoutVideo.speed.setText(this.speedText);
        landLayoutVideo.mActivity = mActivity;
        landLayoutVideo.mHpplayUtils = mHpplayUtils;
        landLayoutVideo.orientationUtils = orientationUtils;
        landLayoutVideo.clickAirPlay = clickAirPlay;
        this.getPowerAndTime();
        return landLayoutVideo;
    }

    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
        if (gsyVideoPlayer != null) {
            BaseVideoPlayer landLayoutVideo = (BaseVideoPlayer)gsyVideoPlayer;
            landLayoutVideo.dismissProgressDialog();
            landLayoutVideo.dismissVolumeDialog();
            landLayoutVideo.dismissBrightnessDialog();
            landLayoutVideo.resolution.setVisibility(GONE);
            this.speedText = landLayoutVideo.speed.getText().toString();
            this.resolutionText = landLayoutVideo.resolution.getText().toString();
            this.mActivity = landLayoutVideo.mActivity;
            this.mHpplayUtils = landLayoutVideo.mHpplayUtils;
            this.orientationUtils = landLayoutVideo.orientationUtils;
            this.clickAirPlay = landLayoutVideo.clickAirPlay;
            //点击投屏旋转的屏幕
            if (clickAirPlay){
                if (mHpplayUtils!=null) {
                    //展示设备弹框
                    mHpplayUtils.showDeviceDialog();
                    int duration = getDuration();
                    mHpplayUtils.seekTo(duration/10000);
                }
                clickAirPlay = false;
            }
        }

    }

    protected void changeUiToCompleteClear() {
        super.changeUiToCompleteClear();
        this.setTextAndProgress(0, true);
    }

    protected void changeUiToCompleteShow() {
        super.changeUiToCompleteShow();
        this.setTextAndProgress(0, true);
    }

    private BaseVideoPlayer getCurPlay() {
        return this.getFullWindowPlayer() != null ? (BaseVideoPlayer)this.getFullWindowPlayer() : this;
    }

    public void onBackPressed() {
        if (!this.mIfCurrentIsFullscreen) {
            this.mActivity.finish();
        }

        if (this.orientationUtils != null) {
            this.orientationUtils.backToProtVideo();
        }

        if (!GSYVideoManager.backFromWindowFull(this.getContext())) {
            ;
        }
    }

    public void onPause() {
        this.getCurPlay().onVideoPause();
        this.isPause = true;
    }

    public void onResume() {
        //隐藏设备列表
        if (mHpplayUtils!=null) {
            if (mHpplayUtils.getControlDialog()==null){
                this.getCurPlay().onVideoResume();
                this.isPause = false;
                this.hideAllWidget();
            }else {
                if (!mHpplayUtils.getControlDialog().isShowing()) {
                    this.getCurPlay().onVideoResume();
                    this.isPause = false;
                    this.hideAllWidget();
                }
            }

        }
    }

    public void onDestroy() {
        if (this.isPlay) {
            this.getCurPlay().release();
        }

        if (this.orientationUtils != null) {
            this.orientationUtils.releaseListener();
        }

    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.isPlay && !this.isPause) {
            this.onConfigurationChanged(this.mActivity, newConfig, this.orientationUtils, true, true);
        }

    }

    public void initSettings(Activity activity) {
        this.mActivity = activity;
        this.initSetting();
        //初始化乐播投屏
        mHpplayUtils = new HpplayUtils(activity,this);
    }

    public void playVideo(String url, String title, boolean cachevideo, long seekOnStart) {
        this.isCache = cachevideo;
        this.release();
        this.gsyVideoOptionBuilder.setShowFullAnimation(false).setUrl(url).setCacheWithPlay(cachevideo).setVideoTitle(title).setSeekOnStart(seekOnStart).build(this);
        this.gsyVideoOptionBuilder.build(this);
        this.postDelayed(new Runnable() {
            public void run() {
                BaseVideoPlayer.this.startPlayLogic();
            }
        }, 1000L);
    }

    public void playVideo(String url, String title, boolean cachevideo) {
        this.mResolutionList = null;
        this.playVideo(url, title, cachevideo, 0L);
    }

    public void playVideo(List<ResolutionModel> list, String title, boolean cachevideo) {
        this.mResolutionList = list;
        ResolutionModel resolutionModel = (ResolutionModel)list.get(list.size() - 1);
        this.resolutionText = resolutionModel.resolutionName;
        this.resolution.setText(this.resolutionText);
        this.playVideo(resolutionModel.url, title, cachevideo, 0L);
    }

    public void setTakeScreenListener(BaseVideoPlayer.TakeScreenListener takeScreenListener) {
        this.mTakeScreenListener = takeScreenListener;
    }

    public void setShareListener(BaseVideoPlayer.ShareListener shareListener) {
        this.mShareListener = shareListener;
    }

    public interface ShareListener {
        void Share(View var1);
    }

    public interface TakeScreenListener {
        void takeScreen(Bitmap var1);
    }

    class TakeScreenTimer implements Runnable {
        TakeScreenTimer() {
        }

        public void run() {
            if (BaseVideoPlayer.this.mCurrentState != 0 && BaseVideoPlayer.this.mCurrentState != 7 && BaseVideoPlayer.this.mCurrentState != 6) {
                if (BaseVideoPlayer.this.getActivityContext() != null) {
                    BaseVideoPlayer.this.hideAllWidget();
                    BaseVideoPlayer.this.setViewShowState(BaseVideoPlayer.this.mLockScreen, 8);
                    BaseVideoPlayer.this.setViewShowState(BaseVideoPlayer.this.takeScreen, 8);
                    if (BaseVideoPlayer.this.mHideKey && BaseVideoPlayer.this.mIfCurrentIsFullscreen && BaseVideoPlayer.this.mShowVKey) {
                        CommonUtil.hideNavKey(BaseVideoPlayer.this.mContext);
                    }
                }

                if (BaseVideoPlayer.this.mPostDismiss) {
                    BaseVideoPlayer.this.postDelayed(this, (long)BaseVideoPlayer.this.mDismissControlTime);
                }
            }

        }
    }


}
