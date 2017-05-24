package org.cloudvr.client.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.cloudvr.client.R;
import org.cloudvr.client.home.io.connections.ServerTCP;

public class SettingActivity extends AppCompatActivity {
    private Button btn_complete_setting;
    private EditText et_ip;
    private EditText et_port;
    private CheckBox cb_use_tcp;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        et_ip= (EditText) findViewById(R.id.et_ip);
        et_port= (EditText) findViewById(R.id.et_port);
        cb_use_tcp= (CheckBox) findViewById(R.id.cb_tcp);

        sp=getSharedPreferences(getString(R.string.server_settings_sp_key), MODE_PRIVATE);
        et_ip.setText(sp.getString(getString(R.string.server_ip_key), ""));
        et_port.setText(sp.getString(getString(R.string.server_port_key), ServerTCP.DEFAULT_PORT + ""));
        cb_use_tcp.setChecked(sp.getBoolean(getString(R.string.server_use_tcp_key), true));

        btn_complete_setting= (Button) findViewById(R.id.btn_complete_setting);
        btn_complete_setting.setOnClickListener(listener);
    }

    private View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String ip=et_ip.getText().toString().trim();
            String port=et_port.getText().toString().trim();
            boolean use_tcp=cb_use_tcp.isChecked();

            if(TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)){
                Toast.makeText(SettingActivity.this, R.string.server_ip_port_empty, Toast.LENGTH_SHORT).show();
            }else{
                SharedPreferences.Editor editor=sp.edit();
                editor.putString(getString(R.string.server_ip_key), ip);
                editor.putString(getString(R.string.server_port_key), port);
                editor.putBoolean(getString(R.string.server_use_tcp_key), use_tcp);
                editor.commit();
                finish();
            }
        }
    };
}