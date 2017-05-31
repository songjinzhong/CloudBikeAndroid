package org.cloudvr.client.splash;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.cloudvr.client.R;
import org.cloudvr.client.main.MainActivity;
import org.cloudvr.client.splash.apkutils.ApkUtils;
import org.cloudvr.client.splash.apkutils.VersionUpdateUtils;

public class SplashActivity extends Activity {
    private String mVersion;
    private TextView mVersionTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // force the Activity to stay in landscape and to keep the screen on
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mVersion= ApkUtils.getVersion(getApplicationContext());
        mVersionTV= (TextView) findViewById(R.id.tv_splash_version);
        mVersionTV.setText("Version: "+mVersion);
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        //final VersionUpdateUtils updateUtils=new VersionUpdateUtils(mVersion, SplashActivity.this);
//        new Thread() {
//            public void run() {
//                //获取服务器版本号
 //               updateUtils.getCloudVersion();
//            }
//        }.start();
    }
}