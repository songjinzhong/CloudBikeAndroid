package org.cloudvr.client;

import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;

/**
 * Created by Hepsilion on 2017/2/14.
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected abstract void unregister();
    protected abstract void register();

    @Override
    public void onResume() {
        super.onResume();
        register();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
