package cn.titansys.produce.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import cn.titansys.produce.R;
import cn.titansys.produce.util.SystemUtil;

public class FinishActivity extends BaseActivity {

    private Button btn_finish;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        //设置工具栏颜色
        SystemUtil.setStatusBarColor(this, Color.parseColor("#ffffff"));
        SystemUtil.setAndroidNativeLightStatusBar(this,true);

        //返回按钮
        ImageView iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //点击退出
        btn_finish =  findViewById(R.id.btn_finish);
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //跳转到扫码
                finish();
            }
        });

    }
}