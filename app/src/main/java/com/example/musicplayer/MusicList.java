package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicList extends AppCompatActivity {
    public static final String DIRECTORY= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();//SD卡目录路径
    private DownloadService.DownloadBinder myBinder;
    private List<ListItem> curFiles=new ArrayList<ListItem>();//目前已经存在的音乐文件列表
    private ListItem target;
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder=(DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    ListView listView;

    boolean fileExits=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        listView=(ListView)findViewById(R.id.listview);
        FindFiles();
        final List<ListItem> list=new ArrayList<ListItem>();
        ListItem item=new ListItem("Juanitos","Juanitos_-_06_-_Exotica.mp3","https://files.freemusicarchive.org/storage-freemusicarchive-org/music/Oddio_Overplay/Juanitos/Exotica/Juanitos_-_06_-_Exotica.mp3?download=1");
        ListItem item1=new ListItem("K.I.R.K","KIRK_-_02_-_Dont_Go.mp3","https://files.freemusicarchive.org/storage-freemusicarchive-org/music/ccCommunity/KIRK/FrostWire_Creative_Commons_Mixtape_Vol_5/KIRK_-_02_-_Dont_Go.mp3?download=1");
        ListItem item2=new ListItem("Little Glass Men","Little_Glass_Men_-_07_-_Spray_paint_it_Gold.mp3","https://files.freemusicarchive.org/storage-freemusicarchive-org/music/Music_for_Video/Little_Glass_Men/The_Age_of_Insignificance/Little_Glass_Men_-_07_-_Spray_paint_it_Gold.mp3?download=1");
        ListItem item3=new ListItem("Captive Portal","Captive_Portal_-_05_-_T-Shirts_Silly_Bus.mp3","https://files.freemusicarchive.org/storage-freemusicarchive-org/music/no_curator/Captive_Portal/Somethign_Abbadat_-_EP/Captive_Portal_-_05_-_T-Shirts_Silly_Bus.mp3?download=1");
        ListItem item4=new ListItem(" Cullah","Cullah_-_04_-_Lonely_Spider.mp3","https://files.freemusicarchive.org/storage-freemusicarchive-org/music/Music_for_Video/Cullah/Cullahmity/Cullah_-_04_-_Lonely_Spider.mp3?download=1");
        list.add(item);list.add(item1);list.add(item2);list.add(item3);list.add(item4);
        ListItemAdapter adapter=new ListItemAdapter(MusicList.this,R.layout.list_item,list);;
        listView.setAdapter(adapter);

        //通过IntentService启动服务
        Intent intent=new Intent(this,DownloadService.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
        //检查有无授权
        if(ContextCompat.checkSelfPermission(MusicList.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MusicList.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                target=list.get(position);
                String targetName=target.getSongText();
                for(ListItem f:curFiles){
                    if(targetName.equals(f.getSongText())){
                        fileExits=true;
                        break;
                    }
                    fileExits=false;
                }
                if(fileExits){
                    //存在的话直接播放
                    Intent intent2=new Intent(MusicList.this,MainActivity.class);
                    //play为true表示直接播放，false表示要下载音乐
                    intent2.putExtra("play",true);
                    intent2.putExtra("songName",targetName);
                    setResult(1,intent2);
                    finish();
                }
                else{
                    //不存在的话启动下载
                    if(myBinder==null){
                        return;
                    }

                    String url=target.getResourceUrl();
                    myBinder.startDownload(url);
                    Intent intent3=new Intent(MusicList.this,MainActivity.class);
                    intent3.putExtra("play",false);
                    setResult(2,intent3);
                    finish();
                }
            }
        });
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
    /**将目前存在的mp3文件全部找到*/
    public void FindFiles(){
        //读取DOWNLOAD目录下所有的文件
        File dir=new File(DIRECTORY);
        File[] files=dir.listFiles();
        for(File f:files){
            //逐个遍历，将以.mp3结尾的文件加入列表
            String fName=f.getName();
            if(fName.endsWith(".mp3")){
                ListItem song=new ListItem("author",fName,"");
                curFiles.add(song);
            }
        }
    }

}
