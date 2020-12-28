package com.example.shouyu1;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

import java.util.Random;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

public class VideoListAdapter extends BaseAdapter {
    int mVideoIndexs;
    Context mContext;

    public VideoListAdapter(Context context,int count) {
        this.mContext = context;
        mVideoIndexs = count;
    }

    @Override
    public int getCount() {
        return mVideoIndexs;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (null == convertView) {
            holder = new ViewHolder();
            LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
            convertView = mLayoutInflater.inflate(R.layout.lv_item, null);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mJCVideoPlayerStandard = convertView.findViewById(R.id.videoplayer);

        Log.d("cyc:mVideoTitle" + position, VideoObject.mVideoTitle[0][position]);
        Log.d("cyc:mVideoUrls" + position, VideoObject.mVideoUrls[0][position]);
        //System.out.println(VideoObject.mVideoUrls[0][position].length());
        //设置视频标题、url
        try {
            holder.mJCVideoPlayerStandard.setUp(
                    VideoObject.mVideoUrls[0][position],
                    //VideoConstant.mVideoUrls[0][position],
                    JCVideoPlayer.SCREEN_LAYOUT_LIST,
                    VideoObject.mVideoTitle[0][position]);
            //VideoConstant.mVideoTitles[0][position]);
            int imageIndex = new Random().nextInt(5);
            Picasso.with(convertView.getContext())
                    .load(VideoConstant.mVideoThumbs[0][imageIndex])
                    .into(holder.mJCVideoPlayerStandard.thumbImageView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    class ViewHolder {
        JCVideoPlayerStandard mJCVideoPlayerStandard;
    }
}