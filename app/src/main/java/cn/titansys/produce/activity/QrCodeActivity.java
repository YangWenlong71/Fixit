package cn.titansys.produce.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;


import java.util.List;

import cn.titansys.produce.R;
import cn.titansys.produce.util.ACache;
import cn.titansys.produce.util.CheckPermissionUtils;
import cn.titansys.produce.util.Constant;
import cn.titansys.produce.util.ImageUtil;
import cn.titansys.produce.util.RestartAPPTool;
import cn.titansys.produce.util.SystemUtil;

public class QrCodeActivity extends BaseActivity{

    private ImageView btn_capture;
   // private TextView tv_second;
    private TextView tv_hotel;
    private ImageView iv_logout;
    private ACache mCache;

    private TextView tv_from_image;

    /**
     * 选择系统图片Request Code
     */
    public static final int REQUEST_IMAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        //设置工具栏颜色
        SystemUtil.setStatusBarColor(this, Color.parseColor("#ffffff"));
        SystemUtil.setAndroidNativeLightStatusBar(this,true);

        //初始化控件
        initView();

        //初始化权限
        initPermission();

    }

    //初始化
    private void initView(){


        //新页面接收数据
        Bundle bundle = getIntent().getExtras();
        //接收name值
        String hotelName = bundle.getString("hotelName");
        String server = bundle.getString("server");
        String tvUrl = bundle.getString("tvUrl");
        String registerCode = bundle.getString("registerCode");
        String hotelId = bundle.getString("hotelId");

        //缓存到本地
        //登录之后存储到缓存,维持一天
        mCache = ACache.get(QrCodeActivity.this);
        mCache.put("hotelName", hotelName, 1 * ACache.TIME_DAY);//酒店名字
        //mCache.put("server", server, 1 * ACache.TIME_DAY);
        mCache.put("tvUrl", tvUrl, 1 * ACache.TIME_DAY);
        mCache.put("registerCode", registerCode, 1 * ACache.TIME_DAY);
        mCache.put("hotelId", hotelId, 1 * ACache.TIME_DAY);

        //找到相关控件
//        tv_second = findViewById(R.id.tv_second);
//        tv_second.setBackground(getResources().getDrawable(R.drawable.shape_round_solid));
//        tv_second.setTextColor(getResources().getColor(R.color.white));
        btn_capture = findViewById(R.id.btn_capture);
        iv_logout = findViewById(R.id.iv_logout);
        tv_hotel = findViewById(R.id.tv_hotel);
        tv_from_image= findViewById(R.id.tv_from_image);
        tv_hotel.setText(hotelName);

        //点击扫码
        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQrCode();
            }
        });

        //退出当前酒店
        iv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除缓存,回到上一级
                mCache.clear();
                //重启app(像报错似的,暂时不用)
                //RestartAPPTool.restartAPP(QrCodeActivity.this,10);
                //关闭当前页
                finish();
            }
        });


        tv_from_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
    }



    /**
     * 初始化权限事件
     */
    private void initPermission() {
        //检查权限
        String[] permissions = CheckPermissionUtils.checkPermission(this);
        if (permissions.length == 0) {
            //权限都申请了
            //是否登录
        } else {
            //申请权限
            ActivityCompat.requestPermissions(this, permissions, 100);
        }
    }




    // 开始扫码
    private void startQrCode() {
        // 申请相机权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .CAMERA)) {
                Toast.makeText(this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(QrCodeActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(QrCodeActivity.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    //http://download.hexys.titansys.cn/fixit.apk？mac=123&server=127.0.16.115/hexys


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
                    Log.e("qrcode-qr",result);
                    String macStr = result.split("\\&")[0];
                    String serverStr = result.split("\\&")[1];
                    //Toast.makeText(this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                    Intent intent =new Intent(QrCodeActivity.this,DisposeActivity.class);
                    Bundle bd=new Bundle();
                    bd.putString("server", serverStr.split("=")[1]);
                    bd.putString("tvId", macStr.split("=")[1]);
                    intent.putExtras(bd);
                    startActivity(intent);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(QrCodeActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
        /**
         * 选择系统图片并解析
         */
        else if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                Log.e("ImageUtil",ImageUtil.getImageAbsolutePath(this, uri));
                try {
                    CodeUtils.analyzeBitmap(ImageUtil.getImageAbsolutePath(this, uri), new CodeUtils.AnalyzeCallback() {
                        @Override
                        public void onAnalyzeSuccess(String result) {
                            Log.e("qrcode-Image",result);
                            if(result!=null&&result.contains("&")){
                                String macStr = result.split("\\&")[0];
                                String serverStr = result.split("\\&")[1];
                                Intent intent =new Intent(QrCodeActivity.this,DisposeActivity.class);
                                Bundle bd=new Bundle();
                                bd.putString("server", serverStr.split("=")[1]);
                                bd.putString("tvId", macStr.split("=")[1]);
                                intent.putExtras(bd);
                                startActivity(intent);
                            }else {
                                Toast.makeText(QrCodeActivity.this, "解析结果:"+result, Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onAnalyzeFailed() {
                            Toast.makeText(QrCodeActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
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
                    Toast.makeText(QrCodeActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        //屏蔽返回键
        if(keyCode== KeyEvent.KEYCODE_BACK)
            return true;
        return super.onKeyDown(keyCode, event);
    }
}