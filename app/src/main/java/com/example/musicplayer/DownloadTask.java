package com.example.musicplayer;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.loader.content.AsyncTaskLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author dhx
 * 下载的具体类，采用异步任务AsyncTask
 */
public class DownloadTask extends AsyncTask<String, Integer, Integer> {
    public static final int TYPE_SUCCESS=0;
    public static final int TYPE_FAILED=1;

    private DownloadListener listener;
    //当前下载进度
    private int lastProgress;

    //构造方法
    public DownloadTask(DownloadListener listener){
        this.listener=listener;
    }
    /**
     * 下载的具体逻辑
     * */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected Integer doInBackground(String... strings) {
        InputStream is=null;
        RandomAccessFile savedFile=null;
        File file=null;

        try {
            System.out.println("开始执行异步任务的下载工作");
            //已下载文件长度
            long downloadedLength=0;
            String url=strings[0];
            //分出文件名
            int start=url.lastIndexOf("/");
            int end=url.lastIndexOf("?");
            String fileName=url.substring(start,end);
            System.out.println("下载的文件名为"+fileName);
            //下载位于SD卡内存中
            String directory= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            //新建一个文件实例
            file=new File(directory+fileName);

            //文件存在获取之前的文件大小，与网页上面的资源作比较，更新新的部分
            if(file.exists()){
                System.out.println("已存在该文件！继续断点下载");
                downloadedLength=file.length();
                System.out.println("该文件大小="+downloadedLength);
            }
            long webFileLength=getWebFileLength(url);
            System.out.println("webFileLength="+webFileLength);
            //获取file长度=0表示失败
            if(webFileLength==0){
                return TYPE_FAILED;
            }
            else if(downloadedLength==webFileLength){
                return TYPE_SUCCESS;
            }

            OkHttpClient client=new OkHttpClient();
            //断点下载，从追加的地方开始下载
//            Request request=new Request.Builder().addHeader("RANGE","bytes="+downloadedLength+"-").url(url).build();
            Request request=new Request.Builder().url(url).build();
            Response response=client.newCall(request).execute();
            //response.body()只能用一次，用完就关掉了

            if(response.isSuccessful()){
                System.out.println("执行网络请求成功！");
                is= response.body().byteStream();
                System.out.println("inputStream="+is.toString());
                savedFile=new RandomAccessFile(file,"rw");
                //移动文件指针，跳过已经下载过的部分
                savedFile.seek(downloadedLength);
                byte[] b=new byte[1024];
                int total=0;
                int len=0;
                while ((len=is.read(b))!=-1){
//                    System.out.println("追加文件内容");
                    total+=len;
                    //在文件后面追加内容
                    savedFile.write(b,0,len);
                    //计算下载百分比
                    int progress=(int) ((total+downloadedLength)*100/webFileLength);
                    //通知更新下载百分比
                    publishProgress(progress);
                }
                Objects.requireNonNull(response.body()).close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(is!=null){
                    is.close();
                }
                if(savedFile!=null){
                    savedFile.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }
    /**
     * 通知更新当前界面上的下载进度
     * */
    @Override
    protected void onProgressUpdate(Integer... values) {

        int progress=values[0];
//        System.out.println("获取的progress="+progress);
        //若新的下载进度大于当前下载进度则更新
        if(progress>lastProgress){
            System.out.println("异步任务的更新操作");
            //调用接口的更新进度的方法
            listener.onProgress(progress);
            lastProgress=progress;
        }
    }
    /**
     * 通知最终的下载结果
     * */
    @Override
    protected void onPostExecute(Integer state) {
        switch (state){
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            default:break;
        }
    }

    /**
     * 获取url指向文件的文件大小
     * */
    public long getWebFileLength(String url) throws IOException{
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(url).build();
        Response response=client.newCall(request).execute();
        if(response.isSuccessful()){
            long fileLength=response.body().contentLength();
            response.body().close();
            return fileLength;
        }
        return 0;
    }
}
