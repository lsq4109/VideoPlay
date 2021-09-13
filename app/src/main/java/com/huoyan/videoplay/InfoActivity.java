package com.huoyan.videoplay;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoControlView;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;
import com.video.model.ResolutionModel;
import com.video.view.BaseVideoPlayer;

import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends AppCompatActivity {
    private BaseVideoPlayer detailPlayer;
    //        private String url = "http://video-cdn.fe.huoyanvideo.com/video/TRANS0001/儿女亲事1950/儿女亲事1950.m3u8?MtsHlsUriToken=aHR0cHM6Ly9odW95YW52aWRlby5jb20=";
//            private String url = "http://39.97.243.76/m3u8enc_t/EE722E4DD669BA18696440BE98DD09E4_1.m3u8";
    private String url1 = "http://meng.wuyou-zuida.com/20191126/22806_073554e7/index.m3u8";
    private String url = "http://video-cdn.fe.huoyanvideo.com/video/TRANS0001/DuShiLiDeCunZhuang-FNgwHbggQLlGlP/DuShiLiDeCunZhuang-FNgwHbggQLlGlP.m3u8?MtsHlsUriToken=aHR0cHM6Ly9odW95YW52aWRlby5jb20=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        detailPlayer = findViewById(R.id.video);
        detailPlayer.initSettings(this);




        Debuger.enable();

        //设置封面图
        ImageView thumbImage = new ImageView(this);
        thumbImage.setImageResource(R.mipmap.ic_launcher);
        detailPlayer.setThumbImageView(thumbImage);



//        detailPlayer.showAirplay(false);//投屏功能显示隐藏
//        detailPlayer.showTakeScreen(false);//截屏功能显示隐藏
//        detailPlayer.showMore(false);//更多功能显示隐藏
//        detailPlayer.showResolution(false);//清晰度功能显示隐藏
//        detailPlayer.showSpeed(false);//倍速功能显示隐藏
//        detailPlayer.showShare(false);//分享功能显示隐藏
//        detailPlayer.setShowNextVideo(false);//下一集图标



//        detailPlayer.speed.setVisibility(View.GONE);//倍速文字的显示隐藏
//        detailPlayer.bottom_start.setVisibility(View.GONE);//底部开始图标的显示隐藏
//        detailPlayer.closeDoubleClick(true);//禁止双击暂停
//
        //        //禁止进度条的拖动和手势的快进
//        detailPlayer.setCloseSeek(true);
                //播放完成是否显示重播按钮默认显示
//                detailPlayer.setPlayOverShowTryPlayView(false);
        //试看
//                        detailPlayer.setTryWatch(true);
//                        detailPlayer.setShowTryWatch(true);
        //                detailPlayer.setTryWatchTime(20);//秒
        //                detailPlayer.tryWatchPay.setTextColor(Color.BLUE);



        //        detailPlayer.repeatImageView      //重播图片
        //        detailPlayer.repeatTextView      //重播文字
        //        detailPlayer.getStartButton()     //播放器中间的播放按钮（ImageView）
        //        detailPlayer.tryWatchPay         //试看支付文字
        //        detailPlayer.tryWatchTip          //试看提示文字
        //        detailPlayer.nextVideo        //下一集控件
        //播放进度监听
        detailPlayer.setPlayProgressListener(new BaseVideoPlayer.PlayProgressListener() {
            @Override
            public void PlayProgress(int curTime, int totalTime, boolean isFullscreen) {
                System.out.println( curTime+"--------------"+totalTime+"--------------"+isFullscreen);
            }
        });
        //播放状态监听
        detailPlayer.setPlayTypeListener(new BaseVideoPlayer.PlayTypeListener() {
            @Override
            public void PlayType(int type, boolean isFullscreen) {
                if (type== GSYVideoView.CURRENT_STATE_AUTO_COMPLETE){//播放完成
                    Toast.makeText(getApplicationContext(),"播放完成",0).show();
                    //退出全屏
                    if (isFullscreen) {
                        detailPlayer.backNormal();
                    }
                }
            }
        });
        //重播
        detailPlayer.setTryPlayListener(new BaseVideoPlayer.TryPlayListener() {
            @Override
            public void TryPlay(View v) {
                //播放视频
//                detailPlayer.startPlayLogic();
            }
        });
        //试看结束回调
        detailPlayer.setTryWatchListenner(new BaseVideoPlayer.TryWatchListener() {
            @Override
            public void tryWatchEnd() {
                Toast.makeText(getApplicationContext(),"试看结束",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void pay() {
                Toast.makeText(getApplicationContext(),"点击了购买",Toast.LENGTH_SHORT).show();
            }
        });
        //获取返回按键
        detailPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Toast.makeText(getApplicationContext(),"返回",Toast.LENGTH_SHORT).show();
            }
        });
        //获取分享按钮
        detailPlayer.setShareListener(new BaseVideoPlayer.ShareListener() {
            @Override
            public void Share(View var1, boolean isFullscreen) {
                Toast.makeText(getApplicationContext(),"点击了分享",Toast.LENGTH_SHORT).show();
            }
        });

        //截屏
        detailPlayer.setTakeScreenListener(new BaseVideoPlayer.TakeScreenListener() {
            @Override
            public void takeScreen(Bitmap screenBitmap) {
                Toast.makeText(getApplicationContext(),"点击了截屏",Toast.LENGTH_SHORT).show();
            }
        });
        detailPlayer.setNextListener(new BaseVideoPlayer.NextListener() {
            @Override
            public void next(View v) {

                detailPlayer.setShowTryWatch(false);
//                detailPlayer.setPlayOverShowTryPlayView(true);
//                detailPlayer.playVideo(url,"123",true);
//                //                List<ResolutionModel> list = new ArrayList<>();
//                //                list.add(new ResolutionModel("540P","高清1",url1));
//                //                list.add(new ResolutionModel("320P","标清1",url1));
//                //                detailPlayer.playVideo(list,"123",true);
//                Toast.makeText(getApplicationContext(),"点击了下一集",Toast.LENGTH_SHORT).show();
            }
        });
        //单个播放
//        detailPlayer.playVideo(url,"Title",true);
        //清晰度切换
                List<ResolutionModel> list = new ArrayList<>();
//                list.add(new ResolutionModel("1080P","蓝光",url));
//                list.add(new ResolutionModel("720P","超清",url));
                list.add(new ResolutionModel("540P","高清","http://video-cdn.fe.huoyanvideo.com/video/TRANS0001/XiaoBingZhangGa-LcqrPsJCNIsfAt/XiaoBingZhangGa-LcqrPsJCNIsfAt.m3u8?MtsHlsUriToken=aHR0cHM6Ly9odW95YW52aWRlby5jb20="));
                list.add(new ResolutionModel("320P","标清","http://video-cdn.fe.huoyanvideo.com/video/TRANS0001/XiaoBingZhangGa-NHYpCeztiWWkqt/XiaoBingZhangGa-NHYpCeztiWWkqt.m3u8?MtsHlsUriToken=aHR0cHM6Ly9odW95YW52aWRlby5jb20="));
                detailPlayer.playVideo(list,"Title",true);

    }

    @Override
    public void onBackPressed() {
        detailPlayer.onBackPressed();
    }

    @Override
    protected void onPause() {
        detailPlayer.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        detailPlayer.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detailPlayer.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        detailPlayer.onConfigurationChanged(newConfig);

    }



}