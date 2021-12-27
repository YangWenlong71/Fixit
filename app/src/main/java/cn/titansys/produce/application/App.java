package cn.titansys.produce.application;

import android.app.Application;

import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import cn.titansys.produce.util.CrashExceptioner;

/**
 * Created by yangwenlong on 2021/6/28.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ZXingLibrary.initDisplayOpinion(this);

        //全局异常捕获
        CrashExceptioner.init(this);
        CrashExceptioner.setShowErrorDetails(true);//设置不显示详细错误按钮，默认为true
    }
}
