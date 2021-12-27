package cn.titansys.produce.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cn.titansys.produce.R;
import cn.titansys.produce.util.CrashExceptioner;

/**
 * 异常显示类
 */
public final class DefaultErrorActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.customactivityoncrash_default_error_activity);
        Button restartButton = (Button) findViewById(R.id.customactivityoncrash_error_activity_restart_button);

        final Class<? extends Activity> restartActivityClass = CrashExceptioner.getRestartActivityClassFromIntent(getIntent());

        if (restartActivityClass != null) {
            restartButton.setText("重启apk");
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DefaultErrorActivity.this, restartActivityClass);
                    CrashExceptioner.restartApplicationWithIntent(DefaultErrorActivity.this, intent);
                }
            });
        } else {
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrashExceptioner.closeApplication(DefaultErrorActivity.this);
                }
            });
        }

        Button moreInfoButton = (Button) findViewById(R.id.customactivityoncrash_error_activity_more_info_button);
        if (!CrashExceptioner.isShowErrorDetails()) {//用户设置不显示错误信息
            moreInfoButton.setVisibility(View.GONE);
        }
        if (CrashExceptioner.isShowErrorDetailsFromIntent(getIntent())) {

            moreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //We retrieve all the error data and show it

                    AlertDialog dialog = new AlertDialog.Builder(DefaultErrorActivity.this)
                            .setTitle("错误详情")
                            .setMessage(CrashExceptioner.getAllErrorDetailsFromIntent(DefaultErrorActivity.this, getIntent()))
                            .setPositiveButton("关闭", null)
                            .setNeutralButton("复制",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            copyErrorToClipboard();
                                            Toast.makeText(DefaultErrorActivity.this, "复制到剪贴板", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                            .show();
                    TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                    textView.setTextSize(14);
                }
            });
        } else {
            moreInfoButton.setVisibility(View.GONE);
        }
    }

    @SuppressLint("NewApi")
    private void copyErrorToClipboard() {
        String errorInformation =
                CrashExceptioner.getAllErrorDetailsFromIntent(DefaultErrorActivity.this, getIntent());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setText(errorInformation);
        } else {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Error information", errorInformation);
            clipboard.setPrimaryClip(clip);
        }
    }
}
