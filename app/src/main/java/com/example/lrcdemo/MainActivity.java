package com.example.lrcdemo;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videoplayer.listener.OnVideoViewStateChangeListener;
import com.dueeeke.videoplayer.player.AndroidMediaPlayerFactory;
import com.dueeeke.videoplayer.player.VideoView;
import com.dueeeke.videoplayer.player.VideoViewConfig;
import com.dueeeke.videoplayer.player.VideoViewManager;
import com.example.lrcdemo.lrcview_master.DefaultLrcParser;
import com.example.lrcdemo.lrcview_master.LrcRow;
import com.example.lrcdemo.lrcview_master.LrcView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private LrcView mLrcView;
    private Button intentBtn;

    private static final String URL_VOD = "http://vfx.mtime.cn/Video/2019/03/12/mp4/190312143927981075.mp4";

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {

            Log.e("进度",videoView.getCurrentPosition()+"");

            mLrcView.seekTo((int)videoView.getCurrentPosition(),true,false);
            handler.sendEmptyMessageDelayed(0, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VideoViewManager.setConfig(VideoViewConfig.newBuilder()
                //使用MediaPlayer解码
                .setPlayerFactory(AndroidMediaPlayerFactory.create())
                .build());
        videoView = findViewById(R.id.player);
        mLrcView = findViewById(R.id.lrcView);
        intentBtn = findViewById(R.id.btnIntent);
        intentBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,LrcDemo.class)));
        initVideoView();
        initLrcView();
    }

    private void initVideoView(){
        AssetManager am = getResources().getAssets();
        AssetFileDescriptor afd = null;
        try {
            afd = am.openFd("test.mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
        videoView.setUrl("http://vfx.mtime.cn/Video/2019/03/12/mp4/190312143927981075.mp4");
        StandardVideoController controller = new StandardVideoController(this);
        controller.setTitle("公开课"); //设置视频标题
        videoView.setVideoController(controller); //设置控制器，如需定制可继承BaseVideoController
        videoView.start(); //开始播放，不调用则不自动播放

        videoView.addOnVideoViewStateChangeListener(new OnVideoViewStateChangeListener() {
            @Override
            public void onPlayerStateChanged(int playerState) {

            }
            @Override
            public void onPlayStateChanged(int playState) {
                if(playState==VideoView.STATE_PLAYBACK_COMPLETED){
                    mLrcView.reset();
                    handler.removeMessages(0);
                }
                switch (playState){
                    case VideoView.STATE_PAUSED:
                        break;
                    case VideoView.STATE_PREPARED:
                        mLrcView.setLrcRows(getLrcRows());
                        handler.sendEmptyMessage(0);
                        break;
                    case VideoView.STATE_PLAYBACK_COMPLETED:

                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void initLrcView(){
        mLrcView.setOnSeekToListener(new LrcView.OnSeekToListener() {
            @Override
            public void onSeekTo(int progress) {

            }
        });
        mLrcView.setOnLrcClickListener(new LrcView.OnLrcClickListener() {
            @Override
            public void onClick() {

            }
        });
    }


    /**
     * 将播放进度的毫米数转换成时间格式
     * 如 3000 --> 00:03
     * @param progress
     * @return
     */
    private String formatTimeFromProgress(int progress){
        //总的秒数
        int msecTotal = progress/1000;
        int min = msecTotal/60;
        int msec = msecTotal%60;
        String minStr = min < 10 ? "0"+min:""+min;
        String msecStr = msec < 10 ? "0"+msec:""+msec;
        return minStr+":"+msecStr;
    }
    /**
     * 获取歌词List集合
     * @return
     */
    private List<LrcRow> getLrcRows(){
        List<LrcRow> rows = null;
        InputStream is = getResources().openRawResource(R.raw.hs);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line ;
        StringBuffer sb = new StringBuffer();
        try {
            while((line = br.readLine()) != null){
                sb.append(line+"\n");
            }
            System.out.println(sb.toString());
            rows = DefaultLrcParser.getIstance().getLrcRows(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }
}
