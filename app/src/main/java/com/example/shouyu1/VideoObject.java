package com.example.shouyu1;

import android.util.Log;

public class VideoObject {

    public static String[][] mVideoUrls;    //视频url
    public static String[][] mVideoTitle;   //视频标题
    public String videoString;              //后台返回数据
    public int index;                       //视频下标

    public VideoObject(String s) {
        mVideoUrls = new String[1][10];
        mVideoTitle = new String[1][10];
        videoString = s;
        index = 0;
    }
    //获取视频数量
    public int getIndex() {
        return index;
    }

    //处理后台返回数据
    public void StringToObject() {
        String tmpUrl = "";
        String tmpName = "";
        char a = 0, b = 0, c = 0;
        boolean isFirst = true;
        for(int i=0; i<videoString.length(); i++) {
            if(isChineseChar(videoString.charAt(i)) == true) {
                tmpName += videoString.charAt(i);   //若为中文
            }else {
                tmpUrl += videoString.charAt(i);    //若为字符
                try {
                    //分词结束
                    if (i == videoString.length() - 1 || isChineseChar(videoString.charAt(i + 1)) == true) {
                        if(!tmpUrl.isEmpty() && !tmpName.isEmpty()) {
                            if(isFirst) {
                                String newUrl = "";
                                for (int j = 2; j < tmpUrl.length(); j++) {
                                    newUrl += tmpUrl.charAt(j);
                                }
                                tmpUrl = newUrl;
                                isFirst = false;
                            }
                            mVideoUrls[0][index] = tmpUrl;
                            mVideoTitle[0][index] = tmpName;
                            index++;
                            tmpUrl = "";
                            tmpName = "";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //判断是否为汉字
    public static boolean isChineseChar(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }
}