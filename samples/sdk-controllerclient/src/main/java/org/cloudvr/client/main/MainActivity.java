package org.cloudvr.client.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.cloudvr.client.R;

public class MainActivity extends AppCompatActivity {
    private Button btn_buildup;
    private Button btn_travel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // force the Activity to stay in landscape and to keep the screen on
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_buildup = (Button) findViewById(R.id.btn_buildup);
        btn_travel = (Button) findViewById(R.id.btn_travel);
        btn_buildup.setOnClickListener(listener);
        btn_travel.setOnClickListener(listener);
    }

    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_buildup:
                    if(serverSet()){
                        Intent intent1=new Intent();
                        intent1.setClass(MainActivity.this, Mode1Activity.class);
                        startActivity(intent1);
                    }else{
                        Toast.makeText(MainActivity.this, R.string.server_setting_notification, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btn_travel:
                    if(serverSet()){
                        Intent intent2=new Intent();
                        intent2.setClass(MainActivity.this, Mode2Activity.class);
                        startActivity(intent2);
                    }else{
                        Toast.makeText(MainActivity.this, R.string.server_setting_notification, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private boolean serverSet(){
        SharedPreferences sp=getSharedPreferences(getString(R.string.server_settings_sp_key), MODE_PRIVATE);
        String server_ip=sp.getString(getString(R.string.server_ip_key), "").toString().trim();
        String server_port=sp.getString(getString(R.string.server_port_key), "").toString().trim();
        if(TextUtils.isEmpty(server_ip) || TextUtils.isEmpty(server_port)){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}