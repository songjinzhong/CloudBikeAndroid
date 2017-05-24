package org.cloudvr.client.main;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.cloudvr.client.BaseActivity;
import org.cloudvr.client.R;
import org.cloudvr.client.home.event.EventBus;
import org.cloudvr.client.home.event.Events;
import org.cloudvr.client.home.headtracking.providers.CalibratedGyroscopeProvider;
import org.cloudvr.client.home.headtracking.providers.OrientationProvider;
import org.cloudvr.client.home.io.connections.ServerIO;
import org.cloudvr.client.home.io.connections.ServerTCP;
import org.cloudvr.client.home.io.connections.ServerUDP;
import org.cloudvr.client.home.io.data.GyroInput;
import org.cloudvr.client.home.logging.FPSLogger;
import org.cloudvr.client.home.logging.LoggerBus;
import org.cloudvr.client.home.utils.FullScreenManager;
import org.cloudvr.client.home.utils.PerformanceMonitor;
import org.cloudvr.client.home.views.RemoteVRView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class Mode2Activity extends BaseActivity {
    protected final String LOG_TAG = getClass().getSimpleName();
    private static final int ERROR = 0;

    private RelativeLayout notConnectedView;

    private RelativeLayout connectionInProgressView;

    private RelativeLayout connectedView;
    private RemoteVRView remoteVRView;
    private FPSLogger fpsLogger;

    private ServerIO serverConnection;

    private OrientationProvider orientationProvider;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String str= (String) msg.obj;
            switch (msg.what){
                case ERROR:
                    Toast.makeText(Mode2Activity.this, str, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private FullScreenManager fullScreenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // force the Activity to stay in landscape and to keep the screen on
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode2);

        initViews();
        enableVrMode();
    }

    private void initViews(){
        notConnectedView= (RelativeLayout) findViewById(R.id.not_connected_view);
        notConnectedView.setOnClickListener((v) -> connectServer());

        connectionInProgressView= (RelativeLayout) findViewById(R.id.connection_in_progress_view);

        connectedView= (RelativeLayout) findViewById(R.id.connected_view);
        remoteVRView= (RemoteVRView) findViewById(R.id.remote_vr_view);
        fpsLogger = new FPSLogger((TextView) findViewById(R.id.fps_counter));

        orientationProvider = new CalibratedGyroscopeProvider(this);
        orientationProvider.start();

        fullScreenManager = new FullScreenManager(this, null);
    }

    private void enableVrMode(){
        // This configuration won't be required for normal GVR apps.
        // However, since this sample doesn't use GvrView, it needs pretend to be a VR app in order to receive controller events.
        // The Activity.setVrModeEnabled is only enabled on in N, so this is an GVR-internal utility method to configure the app via reflection.
        //
        // If this sample is compiled with the N SDK, Activity.setVrModeEnabled can be called directly.
        // AndroidNCompat.setIsAtLeastNForTesting(true);
        // AndroidCompat.setVrModeEnabled(this, true);
        String servicePackage = "com.google.vr.vrcore";
        String serviceClass = "com.google.vr.vrcore.common.VrCoreListenerService";
        ComponentName serviceComponent = new ComponentName(servicePackage, serviceClass);
        try {
            setVrModeEnabled(true, serviceComponent);
        } catch (PackageManager.NameNotFoundException e) {
            List<ApplicationInfo> installed = getPackageManager().getInstalledApplications(0);
            boolean isInstalled = false;
            for (ApplicationInfo app : installed) {
                if (app.packageName.equals(servicePackage)) {
                    isInstalled = true;
                    break;
                }
            }
            if (isInstalled) {// Package is installed, but not enabled in Settings.  Let user enable it.
                startActivity(new Intent("android.settings.VR_LISTENER_SETTINGS"));//Settings.ACTION_VR_LISTENER_SETTINGS));
            } else {// Package is not installed.  Send an intent to download this.
                //sentIntentToLaunchAppStore(servicePackage);
            }
        }
    }

    public void setVrModeEnabled(boolean paramBoolean, ComponentName paramComponentName) throws PackageManager.NameNotFoundException {
        Log.d(LOG_TAG, "setVrModeEnabled");
        ComponentName componentName = new ComponentName("com.google.vr.vrcore", "com.google.vr.vrcore.common.VrCoreListenerService");
        Intent intent = new Intent();
        intent.setComponent(componentName);
        startService(intent);
    }

    private void connectServer() {
        SharedPreferences sp=getSharedPreferences(getString(R.string.server_settings_sp_key), MODE_PRIVATE);
        String serverIP=sp.getString(getString(R.string.server_ip_key), "172.28.44.82");
        int serverPort=Integer.parseInt(sp.getString(getString(R.string.server_port_key), ServerTCP.DEFAULT_PORT + ""));
        boolean serverUseTCP=sp.getBoolean(getString(R.string.server_use_tcp_key), true);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);

        // TODO refactor
        // it's not nice to have this thread, but for now it's ok
        // also I should create a specific class for this piece of logic. It's wrong to have it here.
        new Thread() {
            public void run() {
                try {
                    if (serverUseTCP)
                        serverConnection = new ServerTCP(serverIP, serverPort);
                    else
                        serverConnection = new ServerUDP(serverIP, serverPort);
                } catch (IOException e) {
                    sendUIMessage(ERROR, getString(R.string.error_cannot_connect));
                    return;
                }

                // send screen resolution
                try {
                    int screenWidth = displayMetrics.widthPixels;
                    int screenHeight = displayMetrics.heightPixels;
                    serverConnection.sendScreenResolution(screenWidth, screenHeight);
                } catch (IOException e) {
                    sendUIMessage(ERROR, getString(R.string.error_cant_send_screen_res));
                    //serverConnection.disconnect();
                    return;
                }

                PerformanceMonitor mPerformanceMonitor = new PerformanceMonitor();

                // game video
                serverConnection
                        .getServerOutput()

                        // performance monitor
                        .doOnSubscribe(mPerformanceMonitor::start)
                        .doOnNext(bitmap -> mPerformanceMonitor.newFrameReceived())
                        .doOnUnsubscribe(mPerformanceMonitor::stop)

                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(() -> EventBus.getInstance().post(new Events.ServerConnected()))
                        .doOnSubscribe(() -> LoggerBus.getInstance().post(new LoggerBus.Log("Started connection with server.", LOG_TAG)))

                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError((error) -> sendUIMessage(ERROR, getString(R.string.error_receiving_images)))//SnackbarFactory.snackbarRequest(getView(), R.string.error_receiving_images, -1, Snackbar.LENGTH_LONG)
                        .subscribe(bitmap -> remoteVRView.updateImage(bitmap), Throwable::printStackTrace);

                // game input
                // gyro
                Observable.interval(16, TimeUnit.MILLISECONDS, Schedulers.io())
                        .map(tick -> orientationProvider.getQuaternion())
                        .map(quaternion -> GyroInput.getInstance().putPayload(quaternion))
                        .subscribeOn(Schedulers.io())
                        //.doOnSubscribe(orientationProvider::start)
                        //.doOnUnsubscribe(orientationProvider::stop)
                        .doOnError((error) -> sendUIMessage(ERROR, getString(R.string.error_sending_gyro)))//SnackbarFactory.snackbarRequest(getView(), R.string.error_sending_gyro, -1, Snackbar.LENGTH_LONG))
                        .subscribe(serverConnection.getServerInput(), Throwable::printStackTrace);

                // touch
//                remoteVRView.getPublishSubject()
//                        .observeOn(Schedulers.io())
//                        .filter(event -> event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP)
//                        .map(event -> TouchInput.getInstance().putPayload(event))
//                        .doOnError((error) -> SnackbarFactory.snackbarRequest(getView(), R.string.error_sending_touch, -1, Snackbar.LENGTH_LONG))
//                        .subscribe(serverConnection.getServerInput(), Throwable::printStackTrace);
            }
        }.start();
    }

    private void sendUIMessage(int what, String obj){
        Message msg=new Message();
        msg.what=what;
        msg.obj=obj;
        handler.sendMessage(msg);
    }

    @Override
    public void onDestroy() {
        orientationProvider.stop();
        if(serverConnection != null) {
            serverConnection.disconnect();
            serverConnection=null;
        }
        super.onDestroy();
    }

    @Subscribe
    public void goFullScreen(Events.GoFullScreen e) {
        if(e.isGoFullScreen())
            fullScreenManager.enterFullScreen();
        else
            fullScreenManager.exitFullScreen();
    }

    // events
    @Subscribe
    public void onServerConnecting(Events.ServerConnecting e) {
        notConnectedView.setVisibility(View.GONE);
        connectedView.setVisibility(View.GONE);
        connectionInProgressView.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onServerConnected(Events.ServerConnected e) {
        EventBus.getInstance().post(new Events.GoFullScreen(true));

        connectionInProgressView.setVisibility(View.GONE);
        notConnectedView.setVisibility(View.GONE);
        connectedView.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onServerDisconnected(Events.ServerDisconnected e) {
        EventBus.getInstance().post(new Events.GoFullScreen(false));

        connectionInProgressView.setVisibility(View.GONE);
        connectedView.setVisibility(View.GONE);
        notConnectedView.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void disconnectServer(Events.DisconnectServer e) {
        if(serverConnection != null){
            serverConnection.disconnect();
            serverConnection=null;
        }
    }

    @Subscribe
    public void onGameViewSwipeDetected(Events.RemoteView_SwipeTopBottom e) {
        disconnectServer(null);
    }

    @Override
    public void register() {
        fpsLogger.register();
        EventBus.getInstance().register(this);
        LoggerBus.getInstance().register(this);
    }

    @Override
    public void unregister() {
        fpsLogger.unregister();
        EventBus.getInstance().unregister(this);
        LoggerBus.getInstance().unregister(this);
    }

    /**
     * 按返回键退出程序
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            };
            AlertDialog.Builder builder=new AlertDialog.Builder(Mode2Activity.this);
            builder.setTitle(R.string.quit_mode2);
            builder.setPositiveButton(R.string.confirm_yes, listener);
            builder.setNegativeButton(R.string.confirm_no, null);
            builder.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}