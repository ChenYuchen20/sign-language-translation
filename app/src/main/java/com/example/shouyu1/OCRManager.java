package com.example.shouyu1;

import android.content.Context;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import java.io.File;

public class OCRManager {
    /**
     * 通用文字识别接口（高精度版）
     * @param context   上下文
     * @param filePath  图片文件路径
     * @param ocrCallBack 请求回调
     */
    public static void recognizeAccurateBasic(final Context context, String filePath, final OCRCallBack<GeneralResult> ocrCallBack){
        // 通用文字识别参数设置
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(filePath));

        // 调用通用文字识别服务
        OCR.getInstance(context).recognizeAccurateBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                ocrCallBack.succeed(result);
            }

            @Override
            public void onError(OCRError ocrError) {
                ocrCallBack.failed(ocrError);
            }
        });
    }
    /**
     * 从返回内容中提取识别出的信息
     * @param result
     * @return
     */
    public static String getResult(GeneralResult result){
        final StringBuffer sb=new StringBuffer();
        for (WordSimple wordSimple : result.getWordList()) {
            // wordSimple不包含位置信息
            sb.append(wordSimple.getWords());
        }
        return sb.toString();
    }
    /**
     * 图片识别统一回调接口
     */
    public interface OCRCallBack<T>{
        void succeed(T data);
        void failed(OCRError error);
    }
}