package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dhx
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String DIRECTORY= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();//SD卡目录路径
    private Button btnPlay,btnPause,btnLast,btnNext,btnList,btnUpdate;
    private TextView songName,curTime,maxTime;
    private SeekBar seekBar;

    private List<ListItem> songList =new ArrayList<ListItem>();//播放列表
    int currentPosition=0;//当前播放的歌曲下标
    private MediaPlayer mediaPlayer=new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnPlay=findViewById(R.id.btnPlay);
        btnPause=findViewById(R.id.btnPause);
        btnLast=findViewById(R.id.btnLast);
        btnNext=findViewById(R.id.btnNext);
        btnList=findViewById(R.id.btnList);
        btnUpdate=findViewById(R.id.btnUpdate);
        songName=findViewById(R.id.songName);
        seekBar=findViewById(R.id.seekBar);
        curTime=findViewById(R.id.curTime);
        maxTime=findViewById(R.id.maxTime);

        btnNext.setOnClickListener(this);
        btnLast.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnList.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

        //初始化播放列表
        initList();
        //没有权限就授权
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        else {
            //初始化播放器
            initMediaPlayer();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLast:
                if(currentPosition==0){
                    //0的上一首就是列表的最后一首
                    currentPosition=songList.size()-1;
                }
                else{
                    currentPosition=(currentPosition-1)%songList.size();
                }
                alertMediaPlayer();
                break;

            case R.id.btnNext:
                //环形循环列表
                currentPosition=(currentPosition+1)%songList.size();
                alertMediaPlayer();
                break;

            case R.id.btnPlay:
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    //给进度条设置时长
                    Message message=handler.obtainMessage();
                    message.what=1;
                    message.arg1=mediaPlayer.getDuration();
                    handler.sendMessage(message);
                    //交给更新进度条的子线程更新
                    handler.post(updateThread);
                }
                break;

            case R.id.btnPause:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    //暂停进度条的线程更新
                    handler.removeCallbacks(updateThread);
                }
                break;

            case R.id.btnList:
                Intent intent=new Intent(MainActivity.this,MusicList.class);
                startActivityForResult(intent,1);
                break;
                //更新播放列表
            case R.id.btnUpdate:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                initList();
                initMediaPlayer();
                break;

            default:break;
        }
    }

    /**
     * 初始化播放列表
     * */
    public void initList(){
        songList.clear();
        System.out.println("初始化播放列表");
        //读取DOWNLOAD目录下所有的文件
        File dir=new File(DIRECTORY);
        File[] files=dir.listFiles();
        for(File f:files){
            //逐个遍历，将以.mp3结尾的文件加入播放列表
            String fName=f.getName();
            if(fName.endsWith(".mp3")){
                //分出作者名
                int indexAuthor=fName.lastIndexOf("_");
                String author=fName.substring(0,indexAuthor);
                ListItem song=new ListItem(author,fName,"");
                songList.add(song);
            }
        }

        //进度条的监听器
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //fromUse判断是用户改变的滑块值
                if(fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    /**
     * 初始化播放器
     * */
    public void initMediaPlayer(){
        try {
            System.out.println("初始化播放器");
            mediaPlayer.reset();
            File file=new File(DIRECTORY+"/"+songList.get(currentPosition).getSongText());
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            songName.setText(songList.get(currentPosition).getSongText());
            //设置进度条最大时长
            seekBar.setMax(mediaPlayer.getDuration());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 修改要播放的音乐
     * */
    public void alertMediaPlayer(){
        try {
            //每次切换音乐时需要reset播放器状态
            mediaPlayer.reset();
            File file=new File(DIRECTORY+"/"+songList.get(currentPosition).getSongText());
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            //清空进度条
            seekBar.setProgress(0);
            //设置进度条最大时长
            seekBar.setMax(mediaPlayer.getDuration());

            Message message=handler.obtainMessage();
            message.what=1;
            message.arg1=mediaPlayer.getDuration();
            handler.sendMessage(message);
            //交给更新进度条的子线程更新
            handler.post(updateThread);

            mediaPlayer.start();
            songName.setText(songList.get(currentPosition).getSongText());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**授权*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"拒绝权限将导致无法下载！",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:break;
        }
    }

    /**消息处理，用于更新进度条*/
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                //音乐正在播放，持续更新进度条
                case 1:
                    maxTime.setText(msg.arg1/60000+":"+msg.arg1/1000 % 60);
                    break;
                default:break;
            }
        }
    };
    Runnable updateThread=new Runnable() {
        String str;
        @Override
        public void run() {
            //获取歌曲当前位置并更新
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            str=mediaPlayer.getCurrentPosition()/60000+":"+mediaPlayer.getCurrentPosition()/1000 % 60;
            curTime.setText(str);
            //延时100ms再次启动线程
            handler.postDelayed(updateThread,100);
        }
    };

    /**处理返回的歌曲*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            //找到该曲目
            if(resultCode==1){
                System.out.println("找到该曲目");
                assert data != null;
                boolean play=data.getBooleanExtra("play",false);
                if(play){
                    //play为true直接播放当前音乐
                    String targetName=data.getStringExtra("songName");
                    for(int i=0;i<songList.size();i++){
                        ListItem f=songList.get(i);
                        assert targetName != null;
                        if(targetName.equals(f.getSongText())){
                            currentPosition=i;
                            alertMediaPlayer();
                            break;
                        }
                    }
                }
            }
            //下载
            if(resultCode==2){
                System.out.println("下载该曲目");
            }
            }

    }

}