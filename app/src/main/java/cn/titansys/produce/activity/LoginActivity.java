package cn.titansys.produce.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.IOException;

import cn.titansys.produce.R;
import cn.titansys.produce.util.ACache;
import cn.titansys.produce.util.Constant;
import cn.titansys.produce.util.SystemUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {
    //这个是登录页
    private EditText edt_username;
    private EditText edt_code;
    //    private TextView tv_first;
    private Button btn_login;
    //是否初始化帐号
    private boolean initId = true;
    private ImageView iv_qr_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //设置工具栏颜色
        SystemUtil.setStatusBarColor(this, Color.parseColor("#ffffff"));
        SystemUtil.setAndroidNativeLightStatusBar(this, true);
        //初始化
        initView();

        test();//测试二维码解析的
    }

    private void test() {

        String result = "http://download.hexys.titansys.cn/fixit.apk？mac=123&server=127.0.16.115";
        String macStr = result.split("\\&")[0];
        String serverStr = result.split("\\&")[1];

        String tmac = macStr.split("=")[1];
        String tserver = serverStr.split("=")[1];

        Log.e("tmac::", tmac);
        Log.e("tserver::", tserver);

    }


    //初始化
    private void initView() {
        //找到控件
        edt_username = findViewById(R.id.edt_username);
        edt_code = findViewById(R.id.edt_code);
//        tv_first= findViewById(R.id.tv_first);
//        tv_first.setBackground(getResources().getDrawable(R.drawable.shape_round_solid));
//        tv_first.setTextColor(getResources().getColor(R.color.white));
        btn_login = findViewById(R.id.btn_login);
        iv_qr_code = findViewById(R.id.iv_qr_code);
        //是否初始化测试帐号
        if (initId) {
            edt_username.setText(Constant.hotelId);
            edt_code.setText(Constant.registerCode);
        }

        //手录登录
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //拿到登录数据
                String username = edt_username.getText().toString();
                String code = edt_code.getText().toString();
                //请求服务器校验数据的准确性
                //int i=1/0;
                postLogin(Constant.Prelogin, username, code, 0);
            }
        });
        //扫码登录
        iv_qr_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQrCode();
            }
        });
        //检查是否有数据,有数据就请求尝试
        initPage();
    }

    //初始化页面
    private void initPage() {
        ACache mCache = ACache.get(LoginActivity.this);
        String hotelId = mCache.getAsString("hotelId");
        String registerCode = mCache.getAsString("registerCode");
        if (hotelId != null & registerCode != null) {

            postLogin(Constant.Prelogin, hotelId, registerCode, 1);
        }
    }

    //post请求
    private void postLogin(String url, String hotelId, String registerCode, int operate) {
        OkHttpClient client = new OkHttpClient();
        //post请求
        FormBody formBody = new FormBody.Builder()
                .add("hotelId", hotelId)
                .add("registerCode", registerCode)
                .build();
        Request request = new Request.Builder().url(url)
                .post(formBody).build();
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {

                Message msg = new Message();
                msg.what = 20;
                Thandler.sendMessage(msg);
            }

            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                //System.out.println(result);
                Log.e("msg", result);
                //解析数据
                JSONObject data_key1 = JSONObject.parseObject(result);
                int code = data_key1.getInteger("code");
                if (code == 0) {
                    //写到缓存
                    ACache mCache = ACache.get(LoginActivity.this);
                    mCache.put("hotelId", edt_username.getText().toString(), 1 * ACache.TIME_DAY);//酒店名字
                    mCache.put("registerCode", edt_code.getText().toString(), 1 * ACache.TIME_DAY);//酒店名字
                    String dataItem1 = data_key1.getString("data");
                    //再解析
                    JSONObject data1 = JSONObject.parseObject(dataItem1);
                    String hotelName = data1.getString("hotelName");
                    String server = data1.getString("server");
                    String tvUrl = data1.getString("tvUrl");

                    Log.e("msg", result);
                    Intent intent = new Intent(LoginActivity.this, QrCodeActivity.class);
                    //用Bundle携带数据
                    Bundle bundle = new Bundle();
                    bundle.putString("hotelName", hotelName);
                    bundle.putString("registerCode", registerCode);
                    bundle.putString("hotelId", hotelId);
                    bundle.putString("server", server);
                    bundle.putString("tvUrl", tvUrl);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    //登录失败,清空缓存
                    if (operate == 1) {
                        ACache mCache = ACache.get(LoginActivity.this);
                        mCache.remove("hotelId");
                        mCache.remove("registerCode");
                    } else {
                        String message_result = data_key1.getString("message");
                        Message msg = new Message();
                        msg.what = 16;
                        Bundle bundle = new Bundle();
                        bundle.putString("message_result", message_result);//往Bundle中存放数据
                        msg.setData(bundle);
                        Thandler.sendMessage(msg);
                    }
                }
            }
        });
    }

    //消息列表
    private Handler Thandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 16:
                    String message_result = msg.getData().getString("message_result");
                    Toast.makeText(LoginActivity.this, message_result, Toast.LENGTH_SHORT).show();
                    break;
                case 20:
                    Toast.makeText(LoginActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    // 开始扫码
    private void startQrCode() {
        // 申请相机权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .CAMERA)) {
                Toast.makeText(this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(LoginActivity.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    String userinfo[] = result.split("\\$");
                    //Toast.makeText(this, "解析结果:" + userinfo[0]+userinfo[1], Toast.LENGTH_LONG).show();
                    postLogin(Constant.Prelogin, userinfo[0], userinfo[1], 0);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(LoginActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(LoginActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}