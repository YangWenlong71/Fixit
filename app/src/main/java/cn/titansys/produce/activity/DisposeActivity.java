package cn.titansys.produce.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

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

public class DisposeActivity extends BaseActivity {


    //这个是配置页
  //  private TextView tv_third;
    private Button btn_dispose;
    private EditText edt_roomid;
    private TextView tv_hotelname;
    private TextView tv_updata_url;
    private TextView tv_weburl;
    private TextView tv_deviceid;
    private TextView tv_hotel_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispose);
        //设置工具栏颜色
        SystemUtil.setStatusBarColor(this, Color.parseColor("#ffffff"));
        SystemUtil.setAndroidNativeLightStatusBar(this,true);
        //初始化
        initView();
    }

    //初始化
    private void initView(){
        //返回按钮
        ImageView iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //找到控件
//        tv_third= findViewById(R.id.tv_third);
//        tv_third.setBackground(getResources().getDrawable(R.drawable.shape_round_solid));
//        tv_third.setTextColor(getResources().getColor(R.color.white));
        btn_dispose = findViewById(R.id.btn_dispose);
        edt_roomid= findViewById(R.id.edt_roomid);
        tv_deviceid= findViewById(R.id.tv_deviceid);
        tv_hotelname= findViewById(R.id.tv_hotelname);
        tv_updata_url= findViewById(R.id.tv_updata_url);
        tv_weburl= findViewById(R.id.tv_weburl);
        tv_hotel_id= findViewById(R.id.tv_hotel_id);
        //新页面接收数据
        Bundle bundle = this.getIntent().getExtras();
        String tvId = bundle.getString("tvId");
        String server = bundle.getString("server");

        //从缓存拿数据
        ACache mCache = ACache.get(DisposeActivity.this);
        String hotelName = mCache.getAsString("hotelName");
        //String server = mCache.getAsString("server");
        String tvUrl = mCache.getAsString("tvUrl");
        String registerCode = mCache.getAsString("registerCode");
        String hotelId = mCache.getAsString("hotelId");

        //设置
        tv_hotelname.setText(hotelName);
        tv_updata_url.setText(server);

        tv_hotel_id.setText(hotelId);
        try {
            URL url = new URL(tvUrl);
            String host = url.getHost();
            tv_weburl.setText(host);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        tv_deviceid.setText(tvId);
        //配置完成
        btn_dispose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //请求url接口,这个是写入数据库的
                String roomId = edt_roomid.getText().toString();
                if(roomId.equals("")){
                    Toast.makeText(DisposeActivity.this, "房间号不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    postDeviceReg(Constant.DeviceReg,hotelId,tvId,roomId,registerCode,tv_updata_url.getText().toString());
                }
            }
        });
    }


    private void postDeviceReg(String url,String hotelId,String mac,String room,String registerCode,String server){
        OkHttpClient client = new OkHttpClient();
        //post请求
        FormBody formBody = new FormBody.Builder()
                .add("hotelId",hotelId)
                .add("mac",mac)
                .add("room",room.trim())
                .add("server", "http://"+server+"/junction/device/get")
                .add("registerCode",registerCode)
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
                System.out.println(result);
                Message msg = new Message();
                msg.what = 6;
                Bundle bundle = new Bundle();
                bundle.putString("result", result);//往Bundle中存放数据
                msg.setData(bundle);
                Thandler.sendMessage(msg);
            }
        });
    }

    private Handler Thandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 6:
                    String result = msg.getData().getString("result");//接受msg传递过来的参数
                    //解析数据
                    JSONObject data_key1 = JSONObject.parseObject(result);
                    int code = data_key1.getInteger("code");
                    if(code==0){
                        //当前页面也删掉

                        //Toast.makeText(DisposeActivity.this, "配置完成", Toast.LENGTH_SHORT).show();
                        //跳转配置完成页面
                        Intent intent = new Intent(DisposeActivity.this,FinishActivity.class);
                        startActivity(intent);
                        //关闭当前页面
                        finish();

//                        TimerTask task = new TimerTask(){
//                            public void run(){
//                                //method
//                                finish();
//                            }
//                        };
//                        Timer timer = new Timer();
//                        timer.schedule(task,3000);
                    }else {
                        //有错误消息
                        String message_result = data_key1.getString("message");
                        Message msg6 = new Message();
                        msg6.what = 16;
                        Bundle bundle = new Bundle();
                        bundle.putString("message_result", message_result);//往Bundle中存放数据
                        msg6.setData(bundle);
                        Thandler.sendMessage(msg6);
                    }
                    break;
                case 16:
                    String message_result = msg.getData().getString("message_result");//接受msg传递过来的参m数
                    Toast.makeText(DisposeActivity.this, message_result, Toast.LENGTH_SHORT).show();
                    break;
                case 20:
                    Toast.makeText(DisposeActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}