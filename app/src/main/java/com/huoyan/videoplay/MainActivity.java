package com.huoyan.videoplay;

import androidx.appcompat.app.AppCompatActivity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.video.model.ResolutionModel;
import com.video.view.BaseVideoPlayer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BaseVideoPlayer detailPlayer;
    private EditText mEditText;
    private Button play;
//    private String url = "http://39.97.243.76/m3u8enc_t/EE722E4DD669BA18696440BE98DD09E4_2.m3u8";
    private String url = "https://video-cdn.fe.huoyanvideo.com/video/TRANS0001/B9283E340D5F3E77F02585F0EB89077E/B9283E340D5F3E77F02585F0EB89077E.m3u8?MtsHlsUriToken=aHR0cHM6Ly9odW95YW52aWRlby5jb20=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detailPlayer = findViewById(R.id.video);
        mEditText = findViewById(R.id.url);
        play = findViewById(R.id.play);

        mEditText.setText(url);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                url = mEditText.getText().toString();
                detailPlayer.playVideo(url,"Title",true);
            }
        });


        detailPlayer.initSettings(this);
        //获取返回按键
        detailPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"返回",Toast.LENGTH_SHORT).show();
            }
        });
        //获取分享按钮
        detailPlayer.setShareListener(new BaseVideoPlayer.ShareListener() {
            @Override
            public void Share(View view) {
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
        //单个播放
//        detailPlayer.playVideo(url,"Title",true);
        //清晰度切换
        List<ResolutionModel> list = new ArrayList<>();
        list.add(new ResolutionModel("1080P","蓝光",url));
        list.add(new ResolutionModel("720P","超清",url));
        list.add(new ResolutionModel("540P","高清",url));
        list.add(new ResolutionModel("320P","标清",url));
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