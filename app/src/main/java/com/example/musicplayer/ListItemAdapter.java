package com.example.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * @author dhx
 */
public class ListItemAdapter extends ArrayAdapter<ListItem> {
    private int resourceId;

    public ListItemAdapter(@NonNull Context context, int textViewResourceId, @NonNull List<ListItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId=textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //获取当前note实例
        ListItem item=getItem(position);
        View view;
        ViewHolder viewHolder;
        //convertView用于缓存，提高listView的效率
        if(convertView==null){
            //缓存为空时才加载布局
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            //缓存空时，创建一个用于缓存的实例
            viewHolder=new ViewHolder();

            viewHolder.singer=(TextView)view.findViewById(R.id.singer);
            viewHolder.singerText=(TextView)view.findViewById(R.id.singerText);
            viewHolder.song=(TextView)view.findViewById(R.id.song);
            viewHolder.songText=(TextView)view.findViewById(R.id.songText);

            //将viewHolder保存在view中
            view.setTag(viewHolder);
        }
        else {
            //否则重用convertView，以此达到了不会重复加载布局
            view=convertView;
            //重用viewHolder,重新获取viewHolder
            viewHolder=(ViewHolder)view.getTag();
        }
        //提示信息
        viewHolder.singer.setText("歌手");
        viewHolder.song.setText("歌名");

        //日记内容
        viewHolder.songText.setText(item.getSongText());
        viewHolder.singerText.setText(item.getSingerText());
        return view;
    }

    //创建一个内部类ViewHolder,用于对控件的实例进行缓存
    public class ViewHolder{
        TextView singer,singerText,song,songText;
    }
}
