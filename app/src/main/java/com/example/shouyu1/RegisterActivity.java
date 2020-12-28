package com.example.shouyu1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameET;        //用户名输入框
    private EditText passwordET1;       //密码输入框
    private EditText passwordET2;       //密码确认框
    private Handler handler;            //消息处理对象
    private static int SUCCESS=0x123;   //请求成功判断标识
    private static int ERROR=0x124;     //请求失败判断标识

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //绑定控件
        usernameET = findViewById(R.id.tv_username);
        passwordET1 = findViewById(R.id.tv_password);
        passwordET2 = findViewById(R.id.tv_confirm);

        //设置状态栏颜色
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.statusBar));

        handler = new Handler(){  //Handler方式实现子线程和主线程间的通信
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == ERROR) {
                    String info = msg.obj.toString();
                    showToast(info);
                }
                else {
                    String info = msg.obj.toString();
                    showToast(info);
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    /*
    注册按钮点击响应触发该方法
    */
    public void register(View v) {
        String usr = usernameET.getText().toString();
        String pwd1 = passwordET1.getText().toString();
        String pwd2 = passwordET2.getText().toString();
        if (!pwd1.equals(pwd2)) {
            showToast("两次输入密码不一致！");
        }
        else if (usr.length()==0 || pwd1.length()==0 || pwd2.length()==0) {
            showToast("用户名或密码为空");
        }
        else {
            registerWithOkHttp(usr, pwd1);
        }
    }

    /*
    发送http请求，注册账号
    */
    public void registerWithOkHttp(final String usr, final String pwd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String baseUrl = "http://39.108.150.82/Web/androidApi/index.php/api/admin/register?";
                String url = baseUrl + "username=" + usr + "&password=" + pwd;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Message msg = new Message();
                        msg.what = ERROR;
                        msg.obj = "请检查网络是否可用";
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            Message msg = new Message();
                            msg.what = SUCCESS;
                            msg.obj = "注册成功";
                            handler.sendMessage(msg);
                        }
                        else {
                            Message msg = new Message();
                            msg.what = ERROR;
                            msg.obj = "用户名已存在！";
                            handler.sendMessage(msg);
                        }
                    }
                });
            }
        }).start();
    }

    /*
    取消注册按钮点击响应触发该方法
    */
    public void close(View v) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /*
    以Toast方式显示提示消息
    */
    public void showToast(String info) {
        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
    }
}