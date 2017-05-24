package org.cloudvr.client.main;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.cloudvr.client.R;
import org.cloudvr.client.home.event.EventBus;
import org.cloudvr.client.home.event.Events;
import org.cloudvr.client.home.utils.FullScreenManager;
import org.cloudvr.client.home.adapters.*;
import org.cloudvr.client.home.fragments.LogFragment;
import org.cloudvr.client.home.fragments.ControllerFragment;
import org.cloudvr.client.home.fragments.Fragments;
import org.cloudvr.client.home.fragments.MainFragment;

public class Mode3Activity extends AppCompatActivity {
    private static final String TAG_MAIN_FRAGMENT = "TAG_MAIN_FRAGMENT";
    private static final String TAG_LOG_FRAGMENT = "TAG_LOG_FRAGMENT";
    private static final String TAG_CONTROLLER_FRAGMENT = "TAG_CONTROLLER_FRAGMENT";
    private static final int ERROR = 0;

    private ViewPagerAdapter mPagerAdapter;
    private FullScreenManager fullScreenManager;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String str= (String) msg.obj;
            switch (msg.what){
                case ERROR:
                    Toast.makeText(Mode3Activity.this, str, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // force the Activity to stay in landscape and to keep the screen on
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode3);

        setupFragments();

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TabLayout tabs = (TabLayout) findViewById(R.id.tab_layout);
        fullScreenManager = new FullScreenManager(this, tabs);
    }

    private void setupFragments(){
        ControllerFragment controllerFragment = (ControllerFragment) Fragments.findFragment(getSupportFragmentManager(), ControllerFragment.newInstance(), TAG_CONTROLLER_FRAGMENT);
        MainFragment mainFragment = (MainFragment) Fragments.findFragment(getSupportFragmentManager(), MainFragment.newInstance(), TAG_MAIN_FRAGMENT);
        LogFragment logFragment = (LogFragment) Fragments.findFragment(getSupportFragmentManager(), LogFragment.newInstance(), TAG_LOG_FRAGMENT);

        TabLayout tabs = (TabLayout) findViewById(R.id.tab_layout);
        setupViewPager(tabs, new Pair(controllerFragment, "Controller"), new Pair(mainFragment, getString(R.string.game)), new Pair(logFragment, getString(R.string.log)));
    }

    private void setupViewPager(TabLayout tabs, Pair<Fragment, String>... fragments) {
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        tabs.setupWithViewPager(mViewPager);
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        // This is necessary if I want to retrieve those fragments instances from the stack.
        // Otherwise the ViewPager will re instantiate the fragments when events like configuration changes occurs, and I won't have any control on them.
        // This could result in a double instantiation of the fragments which will lead to the usual fragment problems.
        outState.putString(TAG_MAIN_FRAGMENT, mPagerAdapter.getItem(0).getTag());
        outState.putString(TAG_LOG_FRAGMENT, mPagerAdapter.getItem(1).getTag());
        outState.putString(TAG_LOG_FRAGMENT, mPagerAdapter.getItem(2).getTag());
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

    @Subscribe
    public void goFullScreen(Events.GoFullScreen e) {
        if(e.isGoFullScreen())
            fullScreenManager.enterFullScreen();
        else
            fullScreenManager.exitFullScreen();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getInstance().unregister(this);
    }

    /**
     * 按两次返回键退出程序
     * @param keyCode
     * @param event
     * @return
     */
    private long mExitTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(System.currentTimeMillis()-mExitTime<2000){
                System.exit(0);
            }else{
                Toast.makeText(this, "再按一次退出体验模式", Toast.LENGTH_SHORT).show();
                mExitTime=System.currentTimeMillis();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}