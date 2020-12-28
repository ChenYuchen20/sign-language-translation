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

public class LoginActivity extends AppCompatActivity {

    private EditText usernameET;        //用户名输入框
    private EditText passwordET;        //密码输入框
    private Handler handler;            //消息处理对象
    private static int SUCCESS=0x123;   //请求成功判断标识
    private static int ERROR=0x124;     //请求失败判断标识

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //绑定控件
        usernameET = findViewById(R.id.tv_username);
        passwordET = findViewById(R.id.tv_password);

        //设置状态栏颜色
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.statusBar));

        //接收后台传回数据
        handler = new Handler(){  //Handler方式实现子线程和主线程间的通信
            @Override
            public void handleMessage(Message msg) {
                if (SUCCESS==msg.what) {
                    User user= (User) msg.obj;
                    showToast(user.getUsername()+"，你好！");
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if (ERROR==msg.what) {
                    String info= (String) msg.obj;
                    showToast(info);
                }
            }
        };
    }
    /*
    注册按钮点击响应触发该方法
    */
    public void register(View v) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
    /*
    登陆按钮点击响应触发该方法
    */
    public void login(View v) {
        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        if (username.length()==0 || password.length()==0) {
            showToast("用户名或者密码为空");
        }else {
            loginWithOkHttp(username, password);
        }
    }
    /*
    发送http请求，验证登录账号
    */
    public void loginWithOkHttp(final String username, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String baseUrl = "http://39.108.150.82/Web/androidApi/index.php/api/admin/login?";
                String url = baseUrl + "username=" + username + "&password=" + password;
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
                            String jsonData = response.body().string();//获取服务器返回的json格式数据
                            String tmp = "";
                            for (int i = 2; i < jsonData.length(); i++) {
                                tmp += jsonData.charAt(i);
                            }
                            jsonData = tmp;
                            Log.d("cyc:jsonData", jsonData);
                            Gson gson = new Gson();
                            if (!jsonData.equals("[]")) {
                                User user = gson.fromJson(jsonData, User.class);
                                Message msg = new Message();
                                msg.what = SUCCESS;
                                msg.obj = user;
                                handler.sendMessage(msg);
                            } else {
                                Message msg = new Message();
                                msg.what = ERROR;
                                msg.obj = "用户名或者密码错误";
                                handler.sendMessage(msg);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    /*
    以Toast方式显示提示消息
    */
    public void showToast(String info) {
        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
    }
}