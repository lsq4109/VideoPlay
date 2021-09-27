package com.video.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hpplay.sdk.source.api.LelinkSourceSDK;
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
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;
import com.video.adapter.SpeedAndResolutionAdapter;
import com.video.adapter.SpeedAndResolutionAdapter.SelectListener;
import com.video.model.ResolutionModel;
import com.video.utils.HpplayUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class BaseVideoPlayer extends StandardGSYVideoPlayer {
    private Activity mActivity;
    private GSYVideoOptionBuilder gsyVideoOptionBuilder;
    private OrientationUtils orientationUtils;
    private boolean isPlay;
    private boolean isPause;
    private boolean isCache = false;
    public ImageView airplay;//投屏按钮
    public ImageView share;//分享按钮
    public ImageView bottom_start;
    public ImageView takeScreen;//截屏按钮
    public ImageView more;//更多功能按钮
    public ImageView nextVideo;//下一集按钮
    private SeekBar mVloum;
    private SeekBar mLight;
    private LinearLayout volumLight;
    private BaseVideoPlayer.TakeScreenTimer mTakeScreenTimer = new BaseVideoPlayer.TakeScreenTimer();
    private com.video.view.MyBatterView mBatterView;
    private TextView time;
    public TextView speed;
    public TextView resolution;
    private RecyclerView selecRecycleView;
    private SpeedAndResolutionAdapter mSpeedAndResolutionAdapter;
    private List<ResolutionModel> mResolutionList;
    private String speedText = "倍速";
    private String resolutionText = "";
    private BaseVideoPlayer.TakeScreenListener mTakeScreenListener;
    private BaseVideoPlayer.ShareListener mShareListener;
    private BaseVideoPlayer.NextListener mNextListener;
    public String playurl = "";
    private boolean changeResolution = false;
    private ResolutionModel nowResolution;

    private RelativeLayout tryPlay;
    public TextView repeatTextView;
    public ImageView repeatImageView;
    private TryPlayListener mTryPlayListener;
    private PlayTypeListener mPlayTypeListener;
    private boolean showTryPlayView = true;

    private HpplayUtils mHpplayUtils;
    private boolean clickAirPlay = false;

    //试看
    private LinearLayout tryWatchView;
    public TextView tryWatchTip;
    public TextView tryWatchPay;
    private boolean tryWatch = false;
    private int tryWatchTime = 0;
    private TryWatchListener tryWatchListenner;

    //播放进度
    private PlayProgressListener mPlayProgressListener;

    //是否展示下一集图表
    protected boolean showNextVideo = true;
    //是否展示截屏
    protected boolean showTakeScreen = true;
    //是否展示分享
    protected boolean showShare = true;
    //是否展示更多功能
    protected boolean showMore = true;
    //是否展示投屏
    protected boolean showAirplay = true;
    //是否展示倍速
    protected boolean showSpeed = true;
    //是否展示清晰度
    protected boolean showResolution = true;


    //禁止双击暂停
    protected boolean closeDoubleClick = false;

    //程序后台时的状态
    protected int onPausePlayState=CURRENT_STATE_PLAYING;


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
        mBottomProgressBar.setVisibility(GONE);
        this.tryPlay = (RelativeLayout) this.findViewById(id.try_play);
        this.repeatTextView = this.findViewById(id.repeatTextView);
        this.repeatImageView = this.findViewById(id.repeatImageView);
        this.takeScreen = (ImageView) this.findViewById(id.take_screen);
        this.bottom_start = (ImageView) this.findViewById(id.bottom_start);
        this.more = (ImageView) this.findViewById(id.more);
        this.airplay = (ImageView) this.findViewById(id.airplay);
        this.volumLight = (LinearLayout) this.findViewById(id.volum_light);
        this.time = (TextView) this.findViewById(id.time);
        this.mBatterView = (com.video.view.MyBatterView) this.findViewById(id.batterView);
        this.speed = (TextView) this.findViewById(id.speed);
        this.share = (ImageView) this.findViewById(id.share);
        this.nextVideo = (ImageView) this.findViewById(id.next_video);
        this.resolution = (TextView) this.findViewById(id.resolution);
        this.tryWatchTip = (TextView) this.findViewById(id.tryWatchTip);
        this.tryWatchPay = (TextView) this.findViewById(id.tryWatchPay);
        this.mLight = (SeekBar) this.findViewById(id.s_light);
        this.mVloum = (SeekBar) this.findViewById(id.s_volum);
        this.tryWatchView = (LinearLayout) this.findViewById(id.try_watch);
        this.selecRecycleView = (RecyclerView) this.findViewById(id.select_recycle);
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
                    if ((double) sp == 1.0D) {
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
                    BaseVideoPlayer.this.playurl = res.url;
                    BaseVideoPlayer.this.setUp(res.url, BaseVideoPlayer.this.isCache, BaseVideoPlayer.this.mTitle);
                    BaseVideoPlayer.this.setSeekOnStart(currentPosition);
                    BaseVideoPlayer.this.startPlayLogic();
                    BaseVideoPlayer.this.onClickUiToggle();
                    changeResolution = true;
                    nowResolution = res;
                }
                BaseVideoPlayer.this.selecRecycleView.setVisibility(GONE);
            }
        });
        //试看购买
        this.tryWatchView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tryWatchListenner != null) {
                    tryWatchListenner.pay();
                }
            }
        });
        //重播
        this.tryPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseVideoPlayer.this.tryPlay.setVisibility(GONE);
                startPlayLogic();
                if (mTryPlayListener != null) {
                    mTryPlayListener.TryPlay(v);
                }
            }
        });
        setBackFromFullScreenListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //退出全屏
                backNormal();
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
                    BaseVideoPlayer.this.mShareListener.Share(v, mIfCurrentIsFullscreen);
                    if (BaseVideoPlayer.this.mCurrentState != 6) {
                        BaseVideoPlayer.this.hideAllWidget();
                        BaseVideoPlayer.this.mTakeScreenTimer.run();
                    }
                }
            }
        });
        this.airplay.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!tryWatch) {
                    if (BaseVideoPlayer.this.mCurrentState != 6) {
                        BaseVideoPlayer.this.hideAllWidget();
                        BaseVideoPlayer.this.mTakeScreenTimer.run();
                        if (mIfCurrentIsFullscreen) {
                            if (BaseVideoPlayer.this.orientationUtils != null) {
                                clickAirPlay = true;
                                BaseVideoPlayer.this.orientationUtils.resolveByClick();
                                //                            backFromFull(getContext());
                                return;
                            }
                        }
                        if (mHpplayUtils != null) {
                            //展示设备弹框
                            mHpplayUtils.showDeviceDialog();
                            int duration = getCurrentPositionWhenPlaying();
                            mHpplayUtils.seekTo(duration / 1000);
                        }
                    }
                } else {
                    Toast.makeText(mContext, "试看视频，不能投屏", Toast.LENGTH_LONG).show();
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
        nextVideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNextListener != null) {
                    mNextListener.next(v);
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
                        if (!closeDoubleClick) {
                            BaseVideoPlayer.this.touchDoubleUp();
                        }
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

    @Override
    protected void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime, boolean forceChange) {
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime, forceChange);
        if (mPlayProgressListener != null) {
            int totalSeconds = totalTime / 1000;
            int currentSeconds = currentTime / 1000;
            mPlayProgressListener.PlayProgress(currentSeconds, totalSeconds, mIfCurrentIsFullscreen);
        }
        if (tryWatch) {
            if (tryWatchTime < currentTime) {//试看
                onPause();
                if (tryWatchListenner != null) {
                    tryWatchListenner.tryWatchEnd();
                    cancelProgressTimer();
                }
            }
        }
    }

    private int getSystemBrightness() {
        WindowManager.LayoutParams lpa = ((Activity) this.mContext).getWindow().getAttributes();
        float systemBrightness = lpa.screenBrightness;
        if (systemBrightness <= 0.00f) {
            systemBrightness = 0.50f;
        } else if (systemBrightness < 0.01f) {
            systemBrightness = 0.01f;
        }
        return (int) (systemBrightness * 100.0F);
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
            //解锁屏幕，显示面板
            onClickUiToggle();
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

        mVideoAllCallBack = new GSYSampleCallBack() {
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
        };
        this.gsyVideoOptionBuilder = (new GSYVideoOptionBuilder())
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setSeekRatio(1.0F)
                .setDismissControlTime(6000)
                .setCacheWithPlay(true)
                .setVideoAllCallBack(mVideoAllCallBack);
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
        Intent batteryInfoIntent = context.getApplicationContext().registerReceiver((BroadcastReceiver) null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
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
        //                super.updateStartImage();
        tryPlay.setVisibility(GONE);
        mStartButton.setVisibility(mCurrentState == CURRENT_STATE_PLAYING?GONE:VISIBLE);
        volumLight.setVisibility(GONE);
        selecRecycleView.setVisibility(GONE);
        //隐藏设备列表
        if (mHpplayUtils != null) {
            mHpplayUtils.dismissDeviceDialog();
        }
        //播放状态监听
        if (mPlayTypeListener != null) {
            mPlayTypeListener.PlayType(this.mCurrentState, mIfCurrentIsFullscreen);
        }

        if (mCurrentState == CURRENT_STATE_PLAYING) {
            bottom_start.setImageResource(drawable.video_play);
            mStartButton.setVisibility(GONE);
            takeScreen.setVisibility(mIfCurrentIsFullscreen && showTakeScreen && !mLockCurScreen ? VISIBLE : GONE);
            if (changeResolution) {
                Toast.makeText(getContext(), "清晰度已切换至" + nowResolution.resolutionName + nowResolution.resolution, Toast.LENGTH_LONG).show();
                changeResolution = false;
            }
        } else if (mCurrentState == CURRENT_STATE_ERROR) {//播放错误
            bottom_start.setImageResource(drawable.video_stop);
            mStartButton.setVisibility(VISIBLE);
        } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {//播放结束
            bottom_start.setImageResource(drawable.video_stop);
            takeScreen.setVisibility(mIfCurrentIsFullscreen && showTakeScreen ? VISIBLE : GONE);
            mStartButton.setVisibility(GONE);
            tryPlay.setVisibility(showTryPlayView ? VISIBLE : GONE);
        } else if (mCurrentState == CURRENT_STATE_PLAYING_BUFFERING_START) {//缓冲中
            mStartButton.setVisibility(GONE);
        } else {
            bottom_start.setImageResource(drawable.video_stop);
            mStartButton.setVisibility(VISIBLE);
        }
    }

    protected void onClickUiToggle() {
        super.onClickUiToggle();
        if (this.mIfCurrentIsFullscreen) {
            this.takeScreen.setVisibility(showTakeScreen && this.mBottomContainer.getVisibility() == VISIBLE ? VISIBLE : GONE);
            this.selecRecycleView.setVisibility(GONE);
        } else {
            this.takeScreen.setVisibility(GONE);
        }
        this.volumLight.setVisibility(GONE);
        this.selecRecycleView.setVisibility(GONE);
    }

    @Override
    protected void clickStartIcon() {
        super.clickStartIcon();
        mBottomProgressBar.setVisibility(GONE);
    }

    @Override
    protected void touchDoubleUp() {
        super.touchDoubleUp();
        if (this.mIfCurrentIsFullscreen) {
            this.takeScreen.setVisibility(showTakeScreen ? VISIBLE : VISIBLE);
        }
    }

    @Override
    protected void changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow();
    }


    protected void startDismissControlViewTimer() {
        this.cancelDismissControlViewTimer();
        this.mPostDismiss = true;
        this.postDelayed(this.mTakeScreenTimer, (long) this.mDismissControlTime);
        this.getPowerAndTime();
        if (mIfCurrentIsFullscreen && mLockCurScreen) {
            setViewShowState(mBottomProgressBar, VISIBLE);
        }
    }

    protected void cancelDismissControlViewTimer() {
        this.mPostDismiss = false;
        this.removeCallbacks(this.mTakeScreenTimer);
    }


    public int getEnlargeImageRes() {
        return drawable.full_screen1;
    }

    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        BaseVideoPlayer landLayoutVideo = (BaseVideoPlayer) super.startWindowFullscreen(context, actionBar, statusBar);
        landLayoutVideo.mResolutionList = this.mResolutionList;
        if (this.mResolutionList == null) {
            landLayoutVideo.resolution.setVisibility(GONE);
        }
        landLayoutVideo.showTakeScreen = this.showTakeScreen;
        landLayoutVideo.showShare = this.showShare;
        landLayoutVideo.showSpeed = this.showSpeed;
        landLayoutVideo.showResolution = this.showResolution;
        landLayoutVideo.showMore = this.showMore;
        landLayoutVideo.showAirplay = this.showAirplay;
        landLayoutVideo.showNextVideo = this.showNextVideo;
        landLayoutVideo.mTextureView = this.mTextureView;
        landLayoutVideo.mTakeScreenListener = this.mTakeScreenListener;
        landLayoutVideo.mShareListener = this.mShareListener;
        landLayoutVideo.mNextListener = this.mNextListener;
        landLayoutVideo.resolution.setText(this.resolutionText);
        landLayoutVideo.speed.setText(this.speedText);
        landLayoutVideo.speed.setVisibility(this.speed.getVisibility());
        landLayoutVideo.bottom_start.setVisibility(this.bottom_start.getVisibility());
        landLayoutVideo.nextVideo.setVisibility(showNextVideo ? VISIBLE : GONE);
        landLayoutVideo.takeScreen.setVisibility(showTakeScreen ? VISIBLE : GONE);
        landLayoutVideo.more.setVisibility(showMore ? VISIBLE : GONE);
        landLayoutVideo.airplay.setVisibility(showAirplay ? VISIBLE : GONE);
        landLayoutVideo.share.setVisibility(showShare ? VISIBLE : GONE);
        landLayoutVideo.speed.setVisibility(showSpeed ? VISIBLE : GONE);
        landLayoutVideo.resolution.setVisibility(showResolution ? VISIBLE : GONE);
        landLayoutVideo.mActivity = mActivity;
        landLayoutVideo.mHpplayUtils = mHpplayUtils;
        landLayoutVideo.orientationUtils = orientationUtils;
        landLayoutVideo.clickAirPlay = clickAirPlay;
        landLayoutVideo.playurl = playurl;
        landLayoutVideo.mLockScreen.setVisibility(VISIBLE);
        landLayoutVideo.tryWatchTime = tryWatchTime;
        landLayoutVideo.tryWatch = tryWatch;
        landLayoutVideo.tryWatchTip.setText(tryWatchTip.getText().toString());
        landLayoutVideo.tryWatchPay.setText(tryWatchPay.getText().toString());
        landLayoutVideo.tryWatchPay.setTextColor(tryWatchPay.getTextColors());
        landLayoutVideo.tryWatchView.setVisibility(tryWatchView.getVisibility());
        landLayoutVideo.tryPlay.setVisibility(tryPlay.getVisibility());
        landLayoutVideo.mTryPlayListener = mTryPlayListener;
        landLayoutVideo.showTryPlayView = showTryPlayView;
        landLayoutVideo.mPlayTypeListener = mPlayTypeListener;
        landLayoutVideo.mPlayProgressListener = mPlayProgressListener;
        landLayoutVideo.repeatImageView = repeatImageView;
        landLayoutVideo.repeatTextView.setText(repeatTextView.getText().toString());
        landLayoutVideo.tryWatchListenner = tryWatchListenner;
        landLayoutVideo.closeSeek = closeSeek;
        landLayoutVideo.closeDoubleClick = this.closeDoubleClick;
        landLayoutVideo.gsyVideoOptionBuilder = this.gsyVideoOptionBuilder;
        //设置是否可以快进
        setCloseProgressBar(landLayoutVideo.mProgressBar, landLayoutVideo.mBottomProgressBar);
        return landLayoutVideo;
    }

    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
        if (gsyVideoPlayer != null) {
            BaseVideoPlayer landLayoutVideo = (BaseVideoPlayer) gsyVideoPlayer;
            landLayoutVideo.dismissProgressDialog();
            landLayoutVideo.dismissVolumeDialog();
            landLayoutVideo.dismissBrightnessDialog();
            landLayoutVideo.resolution.setVisibility(GONE);
            this.showTakeScreen = landLayoutVideo.showTakeScreen;
            this.showShare = landLayoutVideo.showShare;
            this.showSpeed = landLayoutVideo.showSpeed;
            this.showResolution = landLayoutVideo.showResolution;
            this.showMore = landLayoutVideo.showMore;
            this.showAirplay = landLayoutVideo.showAirplay;
            this.showNextVideo = landLayoutVideo.showNextVideo;
            this.mTextureView = landLayoutVideo.mTextureView;
            this.mShareListener = landLayoutVideo.mShareListener;
            this.speedText = landLayoutVideo.speed.getText().toString();
            this.speed.setVisibility(landLayoutVideo.speed.getVisibility());
            this.bottom_start.setVisibility(landLayoutVideo.bottom_start.getVisibility());
            this.resolutionText = landLayoutVideo.resolution.getText().toString();
            this.mActivity = landLayoutVideo.mActivity;
            this.mHpplayUtils = landLayoutVideo.mHpplayUtils;
            this.orientationUtils = landLayoutVideo.orientationUtils;
            this.clickAirPlay = landLayoutVideo.clickAirPlay;
            this.playurl = landLayoutVideo.playurl;
            this.tryWatchView.setVisibility(landLayoutVideo.tryWatchView.getVisibility());
            this.tryWatch = landLayoutVideo.tryWatch;
            this.tryWatchPay.setText(landLayoutVideo.tryWatchPay.getText().toString());
            this.tryWatchPay.setTextColor(landLayoutVideo.tryWatchPay.getTextColors());
            this.tryWatchTip.setText(landLayoutVideo.tryWatchTip.getText().toString());
            this.tryWatchTime = landLayoutVideo.tryWatchTime;
            this.tryPlay.setVisibility(landLayoutVideo.tryPlay.getVisibility());
            this.mTryPlayListener = landLayoutVideo.mTryPlayListener;
            this.showTryPlayView = landLayoutVideo.showTryPlayView;
            this.mPlayTypeListener = landLayoutVideo.mPlayTypeListener;
            this.mPlayProgressListener = landLayoutVideo.mPlayProgressListener;
            this.repeatTextView.setText(landLayoutVideo.repeatTextView.getText().toString());
            this.repeatImageView = landLayoutVideo.repeatImageView;
            this.tryWatchListenner = landLayoutVideo.tryWatchListenner;
            this.closeSeek = landLayoutVideo.closeSeek;
            this.closeDoubleClick = landLayoutVideo.closeDoubleClick;
            this.gsyVideoOptionBuilder = landLayoutVideo.gsyVideoOptionBuilder;
            getTitleTextView().setVisibility(GONE);
            this.airplay.setVisibility(showAirplay ? VISIBLE : GONE);
            this.share.setVisibility(showShare ? VISIBLE : GONE);
            //设置是否可以快进
            setCloseProgressBar(this.mProgressBar, this.mBottomProgressBar);
            //点击投屏旋转的屏幕
            if (clickAirPlay) {
                if (mHpplayUtils != null) {
                    //展示设备弹框
                    mHpplayUtils.showDeviceDialog();
                    int duration = getCurrentPositionWhenPlaying();
                    mHpplayUtils.seekTo(duration / 1000);
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
        return this.getFullWindowPlayer() != null ? (BaseVideoPlayer) this.getFullWindowPlayer() : this;
    }

    public void onBackPressed() {
        if (!this.mIfCurrentIsFullscreen) {
            this.mActivity.finish();
        }

        if (this.orientationUtils != null) {
            this.orientationUtils.backToProtVideo();
        }
        GSYVideoManager.backFromWindowFull(this.getContext());
    }

    public void backNormal() {
        if (this.orientationUtils != null) {
            this.orientationUtils.backToProtVideo();
        }
        GSYVideoManager.backFromWindowFull(this.getContext());
    }

    public void onPause() {
        onPausePlayState = mCurrentState;
        this.getCurPlay().onVideoPause();
        this.isPause = true;
        orientationUtils.setIsPause(true);

    }


    public void onResume() {
        //隐藏设备列表
        if (mHpplayUtils != null) {
            if (mHpplayUtils.getControlDialog() == null && mHpplayUtils.getDeviceDialog() == null) {
                if (onPausePlayState!=CURRENT_STATE_PAUSE) {
                    this.getCurPlay().onVideoResume(false);
                    this.isPause = false;
                    if (mCurrentState!=CURRENT_STATE_AUTO_COMPLETE){
                        this.hideAllWidget();
                    }
                    orientationUtils.setIsPause(false);
                }
            } else {
                if (mHpplayUtils.getControlDialog() != null && mHpplayUtils.getControlDialog().isShowing()) {
                    return;
                }
                if (mHpplayUtils.getDeviceDialog() != null && mHpplayUtils.getDeviceDialog().isShowing()) {
                    return;
                }
                if (onPausePlayState!=CURRENT_STATE_PAUSE) {
                    this.getCurPlay().onVideoResume(false);
                    this.isPause = false;
                    if (mCurrentState!=CURRENT_STATE_AUTO_COMPLETE){
                        this.hideAllWidget();
                    }
                    orientationUtils.setIsPause(false);
                }
            }
        }
    }

    public void onDestroy() {
        GSYVideoManager.releaseAllVideos();
        this.getCurPlay().release();

        if (this.orientationUtils != null) {
            this.orientationUtils.releaseListener();
        }
        LelinkSourceSDK.getInstance().stopPlay();
        LelinkSourceSDK.getInstance().unBindSdk();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.isPlay && !this.isPause) {
            this.onConfigurationChanged(this.mActivity, newConfig, this.orientationUtils, true, true);
        }
        setVideoAllCallBack(null);
        getPowerAndTime();
    }

    public void initSettings(Activity activity) {
        this.mActivity = activity;
        this.initSetting();
        getTitleTextView().setVisibility(GONE);
        //初始化乐播投屏
        mHpplayUtils = new HpplayUtils(activity, this);
    }

    public void playVideo(String url, String title, boolean cachevideo, long seekOnStart) {
        this.playurl = url.replace("https://", "http://");
        this.isCache = cachevideo;
        this.release();
        if (!mIfCurrentIsFullscreen) {
            this.gsyVideoOptionBuilder.setShowFullAnimation(false).setUrl(playurl).setCacheWithPlay(cachevideo).setVideoTitle(title).setSeekOnStart(seekOnStart).build(this);
            this.gsyVideoOptionBuilder.build(this);
        } else {
            getCurrentPlayer().setUp(playurl, cachevideo, title);
        }
        getCurrentPlayer().startPlayLogic();
    }

    public void playVideo(String url, String title, boolean cachevideo) {
        this.mResolutionList = null;
        this.playVideo(url, title, cachevideo, 0L);
    }

    public void playVideo(List<ResolutionModel> list, String title, boolean cachevideo) {
        ResolutionModel resolutionModel = (ResolutionModel) list.get(list.size() - 1);
        this.mResolutionList = list;
        this.resolutionText = resolutionModel.resolutionName;
        this.resolution.setText(this.resolutionText);

        getCurPlay().mResolutionList = list;
        getCurPlay().resolutionText = resolutionModel.resolutionName;
        getCurPlay().resolution.setText(getCurPlay().resolutionText);
        this.playVideo(resolutionModel.url, title, cachevideo, 0L);
    }

    /**
     * 关闭双击暂停
     */
    public void closeDoubleClick(boolean closeDoubleClick) {
        this.closeDoubleClick = closeDoubleClick;
    }

    /**
     * 关闭进度拖动
     */
    public void setCloseSeek(boolean closeSeek) {
        this.closeSeek = closeSeek;
        setCloseProgressBar(mProgressBar, mBottomProgressBar);

    }

    /**
     * 设置不能拖动进度进度颜色
     */
    private void setCloseProgressBar(SeekBar seekBar, ProgressBar bottomBar) {
        seekBar.setEnabled(!closeSeek);
        if (this.closeSeek) {
            if (mIfCurrentIsFullscreen) {
                seekBar.setProgressDrawable(mContext.getResources().getDrawable(drawable.video_noseek_progress));
                seekBar.setThumb(null);
            } else {
                seekBar.setProgressDrawable(mContext.getResources().getDrawable(drawable.normal_video_noseek_progress));
                seekBar.setThumb(null);
            }
            bottomBar.setProgressDrawable(mContext.getResources().getDrawable(drawable.video_progress_no));
        } else {
            if (mIfCurrentIsFullscreen) {
                seekBar.setProgressDrawable(mContext.getResources().getDrawable(drawable.video_seek_progress));
                seekBar.setThumb(mContext.getResources().getDrawable(drawable.video_seek_thumb_drawable));
            } else {
                seekBar.setProgressDrawable(mContext.getResources().getDrawable(drawable.normal_video_seek_progress));
                seekBar.setThumb(mContext.getResources().getDrawable(drawable.video_seek_thumb_drawable));
            }
            bottomBar.setProgressDrawable(mContext.getResources().getDrawable(drawable.video_progress));
        }
    }

    /**
     * 不显示下一集
     */
    public void setShowNextVideo(boolean showNextVideo) {
        this.showNextVideo = showNextVideo;
    }

    /**
     * 是否展示截屏
     */
    public void showTakeScreen(boolean showTakeScreen) {
        this.showTakeScreen = showTakeScreen;
    }

    /**
     * 是否展示分享
     */
    public void showShare(boolean showShare) {
        this.showShare = showShare;
        this.share.setVisibility(showShare ? VISIBLE : GONE);
    }

    /**
     * 是否展示更多功能
     */
    public void showMore(boolean showMore) {
        this.showMore = showMore;
    }

    /**
     * 是否展示投屏
     */
    public void showAirplay(boolean showAirplay) {
        this.showAirplay = showAirplay;
        this.airplay.setVisibility(showAirplay ? VISIBLE : GONE);
    }

    /**
     * 是否展示倍速
     */
    public void showSpeed(boolean showSpeed) {
        this.showSpeed = showSpeed;
    }

    /**
     * 是否展示清晰度
     */
    public void showResolution(boolean showResolution) {
        this.showResolution = showResolution;
    }


    /**
     * 播放完成是否显示重播按钮
     */
    public void setPlayOverShowTryPlayView(boolean showTryPlayView) {
        this.showTryPlayView = showTryPlayView;
        getCurPlay().showTryPlayView = showTryPlayView;
    }

    /**
     * 设置试看时长
     *
     * @param tryWatchTime 秒
     */
    public void setTryWatchTime(int tryWatchTime) {
        this.tryWatchTime = tryWatchTime * 1000;
    }

    public void setShowTryWatch(boolean show) {
        getCurPlay().tryWatchView.setVisibility(show ? VISIBLE : GONE);
    }

    public void setTryWatch(boolean tryWatch) {
        this.tryWatch = tryWatch;
    }

    //--------------------------试看监听-------------------------------
    public void setTryWatchListenner(TryWatchListener tryWatchListenner) {
        this.tryWatchListenner = tryWatchListenner;
    }

    public interface TryWatchListener {
        void tryWatchEnd();

        void pay();
    }

    //--------------------------试看监听-------------------------------
    //--------------------------截屏监听-------------------------------
    public void setTakeScreenListener(BaseVideoPlayer.TakeScreenListener takeScreenListener) {
        this.mTakeScreenListener = takeScreenListener;
    }

    public interface TakeScreenListener {
        void takeScreen(Bitmap var1);
    }

    //--------------------------截屏监听-------------------------------
    //--------------------------分享监听-------------------------------
    public void setShareListener(BaseVideoPlayer.ShareListener shareListener) {
        this.mShareListener = shareListener;
    }

    public interface ShareListener {
        void Share(View var1, boolean isFullscreen);
    }

    //--------------------------分享监听-------------------------------
    //--------------------------下一集-------------------------------
    public void setNextListener(BaseVideoPlayer.NextListener nextListener) {
        this.mNextListener = nextListener;
    }

    public interface NextListener {
        void next(View v);
    }

    //--------------------------下一集-------------------------------
    //--------------------------重播-------------------------------
    public void setTryPlayListener(BaseVideoPlayer.TryPlayListener tryPlayListener) {
        this.mTryPlayListener = tryPlayListener;
    }

    public interface TryPlayListener {
        void TryPlay(View v);
    }

    //--------------------------重播-------------------------------
    //--------------------------状态监听-------------------------------
    public void setPlayTypeListener(BaseVideoPlayer.PlayTypeListener playTypeListener) {
        this.mPlayTypeListener = playTypeListener;
    }

    public interface PlayTypeListener {
        void PlayType(int type, boolean isFullscreen);
    }

    //--------------------------状态监听-------------------------------
    //--------------------------播放进度-------------------------------
    public void setPlayProgressListener(BaseVideoPlayer.PlayProgressListener playProgressListener) {
        this.mPlayProgressListener = playProgressListener;
    }

    public interface PlayProgressListener {
        void PlayProgress(int curTime, int totalTime, boolean isFullscreen);
    }
    //--------------------------播放进度-------------------------------

    class TakeScreenTimer implements Runnable {
        TakeScreenTimer() {
        }

        public void run() {
            if (mCurrentState != CURRENT_STATE_NORMAL
                    && mCurrentState != CURRENT_STATE_ERROR
                    && mCurrentState != CURRENT_STATE_AUTO_COMPLETE) {
                if (getActivityContext() != null) {
                    hideAllWidget();
                    setViewShowState(mLockScreen, GONE);
                    setViewShowState(takeScreen, GONE);
                    if (mHideKey && mIfCurrentIsFullscreen && mShowVKey) {
                        CommonUtil.hideNavKey(mContext);
                    }
                }

                if (mPostDismiss) {
                    postDelayed(this, (long) mDismissControlTime);
                }
                if (mIfCurrentIsFullscreen && mLockCurScreen) {
                    setViewShowState(mBottomProgressBar, GONE);
                }
            }

        }
    }

    /**
     * 获取ijkmediaplayer
     *
     * @return
     */
    public IjkMediaPlayer getIjkMediaPlayer() {
        return GSYVideoManager.getIjkMediaPlayeru();
    }

}
