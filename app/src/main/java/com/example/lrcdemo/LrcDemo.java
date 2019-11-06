package com.example.lrcdemo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lrcdemo.lrcview_master.DefaultLrcParser;
import com.example.lrcdemo.lrcview_master.LrcRow;
import com.example.lrcdemo.lrcview_master.LrcView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class LrcDemo extends AppCompatActivity {

    private MediaPlayer mPlayer;
    /**    控制播放的SeekBar***/

    private SeekBar mPlayerSeekBar;
    /**控制歌词字体大小的SeekBar***/
    private SeekBar mLrcSeekBar;
    private Button mPlayBtn;
    private LrcView mLrcView;

    private Toast mPlayerToast;
    private Toast mLrcToast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lrc);

        initViews();
        initPlayer();

    }
    private void initViews() {
        mLrcView = (LrcView) findViewById(R.id.lrcView);
        mLrcView.setOnSeekToListener(onSeekToListener);
        mLrcView.setOnLrcClickListener(onLrcClickListener);
        mPlayerSeekBar = (SeekBar) findViewById(R.id.include_player_seekbar);
        mLrcSeekBar = (SeekBar) findViewById(R.id.include_lrc_seekbar);
        mLrcSeekBar.setMax(100);
        //为seekbar设置当前的progress
        mLrcSeekBar.setProgress((int) ((mLrcView.getmCurScalingFactor()-LrcView.MIN_SCALING_FACTOR)
                /(LrcView.MAX_SCALING_FACTOR-LrcView.MIN_SCALING_FACTOR) *100));
        mLrcSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mPlayBtn = (Button) findViewById(R.id.btnPlay);
        mPlayerSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mPlayBtn.setOnClickListener(onClickListener);
    }
    private void initPlayer() {
        mPlayer = MediaPlayer.create(this, R.raw.testmp3);
        mPlayer.setOnCompletionListener(onCompletionListener);
    }
    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            mPlayBtn.setText("play");
            mLrcView.reset();
            handler.removeMessages(0);
            mPlayerSeekBar.setProgress(0);
        }
    };
    LrcView.OnLrcClickListener onLrcClickListener = new LrcView.OnLrcClickListener() {

        @Override
        public void onClick() {
            Toast.makeText(getApplicationContext(), "歌词被点击啦", Toast.LENGTH_SHORT).show();
        }
    };
    LrcView.OnSeekToListener onSeekToListener = new LrcView.OnSeekToListener() {

        @Override
        public void onSeekTo(int progress) {
            mPlayer.seekTo(progress);

        }
    };
    Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            mPlayerSeekBar.setMax(mPlayer.getDuration());
            mPlayerSeekBar.setProgress(mPlayer.getCurrentPosition());
            handler.sendEmptyMessageDelayed(0, 100);
        };
    };
    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(v == mPlayBtn){
                if("play".equals(mPlayBtn.getText())){
                    mPlayer.start();
                    mLrcView.setLrcRows(getLrcRows());
                    handler.sendEmptyMessage(0);
                    mPlayBtn.setText("暂停");
                }else{

                    if(mPlayer.isPlaying()){
                        mPlayer.pause();
                        mPlayBtn.setText("播放");
                    }else{
                        mPlayer.start();
                        mPlayBtn.setText("暂停");
                    }
                }
            }
        }
    };

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(seekBar == mPlayerSeekBar){
                mPlayer.seekTo(seekBar.getProgress());
                handler.sendEmptyMessageDelayed(0, 100);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if(seekBar == mPlayerSeekBar){
                handler.removeMessages(0);
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if(seekBar == mPlayerSeekBar){
                         Log.e("音乐播放progress 进度",progress+"eee");
                mLrcView.seekTo(progress, true,fromUser);
                if(fromUser){
                    showPlayerToast(formatTimeFromProgress(progress));
                }
            }else if(seekBar == mLrcSeekBar){
                float scalingFactor = LrcView.MIN_SCALING_FACTOR + progress*(LrcView.MAX_SCALING_FACTOR-LrcView.MIN_SCALING_FACTOR)/100;
                mLrcView.setLrcScalingFactor(scalingFactor);
                showLrcToast((int)(scalingFactor*100)+"%");
            }
        }

    };
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
        InputStream is = getResources().openRawResource(R.raw.test);
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

    private TextView mPlayerToastTv;
    private void showPlayerToast(String text){
        if(mPlayerToast == null){
            mPlayerToast = new Toast(this);
            mPlayerToastTv = (TextView) LayoutInflater.from(this).inflate(R.layout.toast, null);
            mPlayerToast.setView(mPlayerToastTv);
            mPlayerToast.setDuration(Toast.LENGTH_SHORT);
        }
        mPlayerToastTv.setText(text);
        mPlayerToast.show();
    }
    private TextView mLrcToastTv;
    private void showLrcToast(String text){
        if(mLrcToast == null){
            mLrcToast = new Toast(this);
            mLrcToastTv = (TextView) LayoutInflater.from(this).inflate(R.layout.toast, null);
            mLrcToast.setView(mLrcToastTv);
            mLrcToast.setDuration(Toast.LENGTH_SHORT);
        }
        mLrcToastTv.setText(text);
        mLrcToast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeMessages(0);
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        mLrcView.reset();
    }

}
