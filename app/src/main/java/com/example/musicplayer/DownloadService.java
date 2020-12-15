package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

/**
 * @author dhx
 * 下载服务
 */
public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;
    private DownloadListener listener=new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            System.out.println("开始service中的更新服务,progress="+progress);
            //下拉通知栏显示
            getNotificationManager().notify(1,getNotification("Downloading...",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask=null;
            //下载成功时通知前台服务通知关闭，并创建一个下载成功的toast
            stopForeground(true);
            //-1表示不显示下载进度
            getNotificationManager().notify(1,getNotification("Download Success",-1));
            Toast.makeText(DownloadService.this,"Download Success!",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask=null;
            //下载成功时通知前台服务通知关闭，并创建一个下载失败的toast
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Failed",-1));
            Toast.makeText(DownloadService.this,"Download Failed!",Toast.LENGTH_SHORT).show();
        }
    };


    public DownloadService() {
    }

    private DownloadBinder myBinder=new DownloadBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    /**
     * 内部类：DownloadBinder用于将活动和服务绑定
     * */
    class DownloadBinder extends Binder {
        //开始下载，调用异步任务的下载功能
        public void startDownload(String url){
            if(downloadTask==null){
                System.out.println("开始service中的下载任务");
                NotificationChannel notificationChannel = null;

                downloadUrl=url;
                downloadTask=new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("Downloading...",0));
                Toast.makeText(DownloadService.this,"Downloading.....",Toast.LENGTH_SHORT).show();


            }
        }
    }

    /**
     *
     * */
    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * 下拉通知栏可以显示进度
     * */
    private Notification getNotification(String title,int progress){
        //安卓8.0以后要给notification增加一个channel用于对通知分类，否则会出bad notification的错
        String CHANNEL_ONE_ID = "com.example.musicplayer";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        //进行安卓8.0的判断
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if(manager!=null){
                manager.createNotificationChannel(notificationChannel);
            }
        }

        Intent intent=new Intent(this,MusicList.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,intent,0);

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,CHANNEL_ONE_ID).setChannelId(CHANNEL_ONE_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if(progress>0){
            //当progress>=0时才显示下载进度
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return  builder.build();
    }
}
