package com.example.shouyu1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.ui.camera.CameraActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.util.ArrayList;
import java.util.List;

import jackmego.com.jieba_android.JiebaSegmenter;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    /**************************************** 定义变量 ****************************************/
    private int count;                  //视频数量
    private EditText inputText;         //输入框对象
    private Handler handler;            //消息处理对象
    private String videoUrl;            //视频url
    private ListView mListView;         //视频播放列表
    private VideoListAdapter mAdapter;  //自定义列表布局
    private VideoObject videoObject;    //消息接收对象
    private boolean exit;               //线程终止判断标志
    private TextView mryjTV;            //每日一句
    //动态申请权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static String[] PERMISSIONS_AUDIO = {
            Manifest.permission.RECORD_AUDIO
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**************************************** 绑定控件 ****************************************/
        inputText = findViewById(R.id.inputText);   //输入框
        mListView = findViewById(R.id.lv);          //视频播放列表
        mryjTV = findViewById(R.id.mryjTV);         //每日一句

        //设置状态栏颜色
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.statusBar));

        //语音配置对象初始化
        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID + "=5e3417e0");
        //初始化百度文字识别OCR
        initAccessTokenWithAkSk();
        //结巴分词初始化
        JiebaSegmenter.init(getApplicationContext());
        //接收后台每日一句推送
        receiveRequestWitjOkHttp();

        //接收后台传回数据并播放视频
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == 1) {
                    videoUrl = msg.obj.toString();
                    //Log.d("videoUrl", videoUrl);
                    videoObject = new VideoObject(videoUrl);
                    videoObject.StringToObject();
                    count = videoObject.getIndex();
                    if(videoUrl != null) {
                        //播放视频
                        mAdapter = new VideoListAdapter(MainActivity.this, count);
                        mListView.setAdapter(mAdapter);
                    }
                }
                else if(msg.what == 2) {
                    String mryjContent = msg.obj.toString();
                    mryjTV.setText(mryjContent);
                }
            }
        };
    }

    /**************************************** 语音识别 ****************************************/
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d("cyc:speech", "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(MainActivity.this, "初始化失败，错误码：" + code, Toast.LENGTH_SHORT).show();
            }
        }
    };
    //动态申请权限
    public static void verifyAudioPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_AUDIO, 1);
        }
    }
    //语音翻译按钮
    public void speechRec(View view){
        verifyAudioPermissions(MainActivity.this);
        RecognizerDialog iatDialog;
        //初始化有交互动画的语音识别器
        iatDialog = new RecognizerDialog(MainActivity.this, mInitListener);
        //设置监听，实现听写结果的回调
        iatDialog.setListener(new RecognizerDialogListener() {
            String resultJson = "[";//放置在外边做类的变量则报错，会造成json格式不对
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                if (!isLast) {
                    resultJson += recognizerResult.getResultString() + ",";
                } else {
                    resultJson += recognizerResult.getResultString() + "]";
                }

                if (isLast) {
                    //解析语音识别后返回的json格式的结果
                    Gson gson = new Gson();
                    List<DictationResult> resultList = gson.fromJson(resultJson,
                            new TypeToken<List<DictationResult>>() {
                            }.getType());
                    String result = "";
                    for (int i = 0; i < resultList.size() - 1; i++) {
                        result += resultList.get(i).toString();
                    }
                    //显示识别结果
                    inputText.setText(result);
                    //获取焦点
                    inputText.requestFocus();
                    //将光标定位到文字最后，以便修改
                    inputText.setSelection(result.length());
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                //自动生成的方法存根
                speechError.getPlainDescription(true);
            }
        });
        iatDialog.show();
    }

    /**************************************** 拍照识别 ****************************************/
    //初始化百度文字识别OCR
    private void initAccessTokenWithAkSk() {
        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
            }
            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
            }
        }, getApplicationContext(),  "jHdheKCKdFu0aysWCiTteeA7", "LT1PcH05apQOGQdvoUzXwRugFzjRDeiH");
    }
    //动态申请权限
    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, 1);
        }
    }
    //拍照翻译按钮
    public void photoRec(View view) {
        verifyStoragePermissions(MainActivity.this);
        // 生成intent对象
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        // 设置临时存储
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, com.example.shouyu1.FileUtil.getSaveFile(getApplication()).getAbsolutePath());
        // 调用文字识别的activity
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
        startActivityForResult(intent, 111);
    }

    //获取拍摄图片
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            // 获取调用参数
            String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
            // 通过临时文件获取拍摄的图片
            String filePath = com.example.shouyu1.FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath();
            OCRManager.recognizeAccurateBasic(this, filePath, new OCRManager.OCRCallBack<GeneralResult>() {
                @Override
                public void succeed(GeneralResult data) {
                    // 调用成功，返回GeneralResult对象
                    String content = OCRManager.getResult(data);
                    //显示识别结果
                    inputText.setText(content);
                    //获取焦点
                    inputText.requestFocus();
                    //将光标定位到文字最后，以便修改
                    inputText.setSelection(content.length());
                    Log.d("cyc:ocr", content);
                }
                @Override
                public void failed(OCRError error) {
                    // 调用失败，返回OCRError对象
                    Log.d("cyc:ocr","错误信息：" + error.getMessage());
                }
            });
        }
    }

    /**************************************** 翻译按键 ****************************************/
    public void translate(View view) {
        videoUrl = "";
        String content = inputText.getText().toString();
        //结巴分词
        ArrayList<String> wordList = JiebaSegmenter.getJiebaSegmenterSingleton().getDividedString(content);
        Log.d("cyc:jieba", String.valueOf(wordList));
        //发送查询请求
        sendRequestWitjOkHttp(wordList);
    }

    /**************************************** 每日一句 ****************************************/
    public void mryjRecommend(View view) {
        String content = mryjTV.getText().toString();
        //结巴分词
        ArrayList<String> wordList = JiebaSegmenter.getJiebaSegmenterSingleton().getDividedString(content);
        //发送查询请求
        sendRequestWitjOkHttp(wordList);
        //显示视频
        mAdapter = new VideoListAdapter(MainActivity.this, count);
        mListView.setAdapter(mAdapter);
    }

    /**************************************** 发送请求 ****************************************/
    /*
    向后台发送查询请求
    */
    private void sendRequestWitjOkHttp(final ArrayList<String> content) {
        exit = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(!exit) {
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("content", String.valueOf(content))
                                .build();
                        Request request = new Request.Builder()
                                .url("http://39.108.150.82//Web/androidApi/index/query")
                                //.url("http://10.22.90.62/Web/androidApi/public/index.php/api/index/index")
                                .post(requestBody)
                                .build();
                        Response response = client.newCall(request).execute();
                        String responseData = response.body().string();
                        //Log.d("responseData",responseData);
                        //发送消息
                        Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.obj = responseData;
                        handler.sendMessage(msg);
                        exit = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*
    接收后台每日一句推送
    */
    public void receiveRequestWitjOkHttp() {
        exit = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(!exit) {
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .build();
                        Request request = new Request.Builder()
                                .url("http://39.108.150.82//Web/androidApi/index/recommend")
                                //.url("http://192.168.66.216/Web/androidApi/public/index.php/api/index/recommend")
                                .post(requestBody)
                                .build();
                        Response response = client.newCall(request).execute();
                        String responseData = response.body().string();
                        //Log.d("responseData",responseData);
                        //发送消息
                        Message msg = handler.obtainMessage();
                        msg.what = 2;
                        msg.obj = responseData;
                        handler.sendMessage(msg);
                        exit = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}