package org.cloudvr.client.home.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;
import com.squareup.otto.Subscribe;

import org.cloudvr.client.R;
import org.cloudvr.client.home.event.EventBus;
import org.cloudvr.client.home.event.Events;
import org.cloudvr.client.home.io.data.GameInput;
import org.cloudvr.client.home.io.data.ResolutionInput;
import org.cloudvr.client.home.io.data.SpeedInput;
import org.cloudvr.client.home.io.data.TurnInput;
import org.cloudvr.client.home.views.RemoteVRView;
import org.cloudvr.client.home.logging.FPSLogger;
import org.cloudvr.client.home.logging.LoggerBus;
import org.cloudvr.client.home.utils.PerformanceMonitor;
import org.cloudvr.client.home.utils.SnackbarFactory;
import org.cloudvr.client.home.headtracking.providers.CalibratedGyroscopeProvider;
import org.cloudvr.client.home.headtracking.providers.OrientationProvider;
import org.cloudvr.client.home.io.connections.ServerIO;
import org.cloudvr.client.home.io.connections.ServerTCP;
import org.cloudvr.client.home.io.connections.ServerUDP;
import org.cloudvr.client.home.io.data.ControllerInput;
import org.cloudvr.client.home.io.data.ControllerState;
import org.cloudvr.client.home.io.data.GyroInput;
import org.cloudvr.client.home.io.data.TouchInput;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * this fragment contains the game view and controls
 *
 * @author Pierfrancesco Soffritti
 */
public class MainFragment extends BaseFragment {
    protected final String LOG_TAG = getClass().getSimpleName();

    private RelativeLayout notConnectedView;
    private RelativeLayout connectionInProgressView;

    /**
     * views in connectedView
     */
    private RelativeLayout connectedView;
    private RemoteVRView remoteVRView;
    private FPSLogger fpsLogger;
    // These TextViews display controller events.TODO(临时放置)
    private TextView apiStatusView;
    private TextView controllerStateView;
    private TextView controllerTouchpadView;
    private TextView controllerButtonView;
    private TextView controllerOrientationText;

    // The various events we need to handle happen on arbitrary threads.
    // They need to be reposted to the UI thread in order to manipulate the TextViews.
    // This is only required if your app needs to perform actions on the UI thread in response to controller events.
    private Handler uiHandler = new Handler();

    /**
     * These two objects are the primary APIs for interacting with the Daydream controller.
     */
    private ControllerManager controllerManager;
    private Controller controller;

    private ServerIO serverConnection;
    private OrientationProvider orientationProvider;

    public MainFragment() {}

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        initViews(view);
        initController();
        enableVrMode();

        return view;
    }

    /**
     *  General views initialization.
     */
    private void initViews(View view){
        notConnectedView= (RelativeLayout) view.findViewById(R.id.not_connected_view);
        notConnectedView.setOnClickListener((v) -> startClient());

        connectionInProgressView= (RelativeLayout) view.findViewById(R.id.connection_in_progress_view);

        connectedView= (RelativeLayout) view.findViewById(R.id.connected_view);
        remoteVRView= (RemoteVRView) view.findViewById(R.id.remote_vr_view);
        fpsLogger = new FPSLogger((TextView) view.findViewById(R.id.fps_counter));
        apiStatusView = (TextView) view.findViewById(R.id.api_status_view);
        controllerStateView = (TextView) view.findViewById(R.id.controller_state_view);
        controllerTouchpadView = (TextView) view.findViewById(R.id.controller_touchpad_view);
        controllerButtonView = (TextView) view.findViewById(R.id.controller_button_view);
        controllerOrientationText = (TextView) view.findViewById(R.id.controller_orientation_text);

        orientationProvider = new CalibratedGyroscopeProvider(getContext());
        orientationProvider.start();
    }

    private void initController(){
        // Start the ControllerManager and acquire a Controller object which represents a single physical controller.
        // Bind our listener to the ControllerManager and Controller.
        EventListener listener = new EventListener();
        controllerManager = new ControllerManager(getContext(), listener);
        apiStatusView.setText("Binding to VR Service");
        controller = controllerManager.getController();
        controller.setEventListener(listener);
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
            List<ApplicationInfo> installed = getActivity().getPackageManager().getInstalledApplications(0);
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
        getActivity().startService(intent);
    }

    // We receive all events from the Controller through this listener.
    // In this example, our listener handles both Controller.EventListener and ControllerManager.EventListener events.
    // This class is also a Runnable since the events will be reposted to the UI thread.
    private class EventListener extends Controller.EventListener implements ControllerManager.EventListener, Runnable {
        // The status of the overall controller API.
        // This is primarily used for error handling since it rarely changes.
        private String apiStatus;

        private boolean speedUp=false, speedDown=false, noChange=false;
        private boolean sentNoTouch=false;
        private double lastTouchPos=0.5;

        // The state of a specific Controller connection.
        private int controllerState = Controller.ConnectionStates.DISCONNECTED;
        private boolean stateChanged = false;

        @Override
        public void onApiStatusChanged(int state) {
            apiStatus = ControllerManager.ApiStatus.toString(state);
            uiHandler.post(this);
        }

        @Override
        public void onConnectionStateChanged(int state) {
            controllerState = state;
            uiHandler.post(this);
        }

        @Override
        public void onRecentered() {
            // If this was a real GVR application, this would call {@link com.google.vr.sdk.base.GvrView#resetHeadTracker} instead of this method.
            //controllerOrientationView.resetYaw();
        }

        @Override
        public void onUpdate() {
            uiHandler.post(this);
        }

        // Update the various TextViews in the UI thread.
        @Override
        public void run() {
            System.out.println("API STATUS: "+apiStatus);
            apiStatusView.setText(apiStatus);
            controllerStateView.setText(Controller.ConnectionStates.toString(controllerState));
            controller.update();
            if (controller.isTouching) {
                controllerTouchpadView.setText(String.format(Locale.US, "[%4.2f, %4.2f]", controller.touch.x, controller.touch.y));
            } else {
                controllerTouchpadView.setText("[ NO TOUCH ]");
            }
            controllerButtonView.setText(String.format("[%s][%s][%s][%s][%s]",
                    controller.appButtonState ? "A" : " ",
                    controller.homeButtonState ? "H" : " ",
                    controller.clickButtonState ? "T" : " ",
                    controller.volumeUpButtonState ? "+" : " ",
                    controller.volumeDownButtonState ? "-" : " "));
            controllerOrientationText.setText(" " + controller.orientation + "\n" + controller.orientation.toAxisAngleString());

            if(serverConnection!=null){
                //加减速命令
                if(controller.appButtonState || controller.homeButtonState){
                    if(controller.appButtonState){
                        if(!speedUp){
                            //Send speedUp command
                            sendSpeedCommand();
                            speedUp=true;
                            speedDown=false;
                            noChange=false;
                        }
                    }else if(controller.homeButtonState){
                        if(!speedDown){
                            //Send speedDown command
                            sendSpeedCommand();
                            speedDown=true;
                            speedUp=false;
                            noChange=false;
                        }
                    }
                }else{
                    if(!noChange) {
                        //Send noChange command
                        sendSpeedCommand();
                        noChange=true;
                        speedUp = false;
                        speedDown = false;
                    }
                }

                //转弯命令
                if(controller.isTouching){
                    float pos=getTurnPos();
                    if(lastTouchPos!=pos){
                        sendTurnCommand(pos);
                        sentNoTouch=false;
                        lastTouchPos=pos;
                    }
                }else{
                    if(!sentNoTouch){
                        sendTurnCommand(0.5f);
                        sentNoTouch=true;
                    }
                }

                //分辨率命令
                if(controller.volumeUpButtonState || controller.volumeDownButtonState){
                    sendResolutionCommand();
                }
            }

//            stateChanged = controller.isTouching || controller.appButtonState || controller.homeButtonState || controller.volumeUpButtonState || controller.volumeDownButtonState;
//            if(controller.isTouching)
//                SnackbarFactory.snackbarRequest(getView(), R.string.isTouch, -1, Snackbar.LENGTH_SHORT);
//            if(controller.appButtonState)
//                SnackbarFactory.snackbarRequest(getView(), R.string.speedUp, -1, Snackbar.LENGTH_SHORT);
//            if(controller.homeButtonState)
//                SnackbarFactory.snackbarRequest(getView(), R.string.speedDown, -1, Snackbar.LENGTH_SHORT);
//            if(controller.volumeUpButtonState)
//                SnackbarFactory.snackbarRequest(getView(), R.string.clearUp, -1, Snackbar.LENGTH_SHORT);
//            if(controller.volumeDownButtonState)
//                SnackbarFactory.snackbarRequest(getView(), R.string.clearDown, -1, Snackbar.LENGTH_SHORT);
//            if(serverConnection!=null && stateChanged){
//
//
//                GameInput gameInput=ControllerInput.getInstance();
//                gameInput.putPayload(ControllerState.getInstance(controller));
//                try {
//                    serverConnection.sendGameInput(gameInput);
//                    SnackbarFactory.snackbarRequest(getView(), R.string.send1, -1, Snackbar.LENGTH_SHORT);
//                } catch (IOException e) {
//                    SnackbarFactory.snackbarRequest(getView(), R.string.error_sending_command, -1, Snackbar.LENGTH_LONG);
//                    e.printStackTrace();
//                }
//
//
//                gameInput.putPayload(ControllerState.getIdleInstance(controller));
//                try {
//                    Thread.sleep(2);
//                    serverConnection.sendGameInput(gameInput);
//                    SnackbarFactory.snackbarRequest(getView(), R.string.send2, -1, Snackbar.LENGTH_SHORT);
//                }catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    SnackbarFactory.snackbarRequest(getView(), R.string.error_sending_command, -1, Snackbar.LENGTH_LONG);
//                    e.printStackTrace();
//                }
//                stateChanged=false;
//            }
        }

        private void sendSpeedCommand(){
            GameInput gameInput= SpeedInput.getInstance();
            gameInput.putPayload(ControllerState.getInstance(controller));
            try {
                serverConnection.sendGameInput(gameInput);
            } catch (IOException e) {
                SnackbarFactory.snackbarRequest(getView(), R.string.error_sending_speed_command, -1, Snackbar.LENGTH_SHORT);
                e.printStackTrace();
            }
        }

        private void sendTurnCommand(float pos){
            GameInput gameInput= TurnInput.getInstance();
            ControllerState controllerState=ControllerState.getInstance(controller);
            controllerState.setTurnPos(pos);
            gameInput.putPayload(controllerState);
            try {
                serverConnection.sendGameInput(gameInput);
            } catch (IOException e) {
                SnackbarFactory.snackbarRequest(getView(), R.string.error_sending_turn_command, -1, Snackbar.LENGTH_SHORT);
                e.printStackTrace();
            }
        }

        private void sendResolutionCommand(){
            GameInput gameInput= ResolutionInput.getInstance();
            gameInput.putPayload(ControllerState.getInstance(controller));
            try {
                serverConnection.sendGameInput(gameInput);
            } catch (IOException e) {
                SnackbarFactory.snackbarRequest(getView(), R.string.error_sending_resolution_command, -1, Snackbar.LENGTH_SHORT);
                e.printStackTrace();
            }
        }

        private float getTurnPos(){
            float turnPos=0.5f;
            if(controller.isTouching){
                float x=controller.touch.x;
                if(x>=0 && x<0.2)  //[0, 0.2)
                    turnPos=0.0f;
                else if(x<0.5)     //[0.2, 0.5)
                    turnPos=0.2f;
                else if(x>0.5 && x<0.8)  //(0.5, 0.8)
                    turnPos=0.8f;
                else if(x<=1)    //[0.8, 1]
                    turnPos=1.0f;
            }
            return turnPos;
        }
    }

    /**
     * starts the connection with the server
     * the IP and PORT of the server are saved in the app's DefaultSharedPreferences because can be changed in SETTINGS
     */
    private void startClient() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String serverIP = sharedPreferences.getString(getString(R.string.server_ip_key), "172.28.49.70");
        int serverPort = Integer.parseInt(sharedPreferences.getString(getString(R.string.server_port_key), ServerTCP.DEFAULT_PORT + ""));
        boolean useTCP = sharedPreferences.getBoolean(getString(R.string.server_use_tcp_key), true);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // TODO refactor
        // it's not nice to have this thread, but for now it's ok
        // also I should create a specific class for this piece of logic. It's wrong to have it here.
        new Thread() {
            public void run() {
                try {
                    if (useTCP)
                        serverConnection = new ServerTCP(serverIP, serverPort);
                    else
                        serverConnection = new ServerUDP(serverIP, serverPort);
                } catch (IOException e) {
                    LoggerBus.getInstance().post(new LoggerBus.Log("Error creating socket: " + e.getClass() + " . " + e.getMessage(), LOG_TAG, LoggerBus.Log.ERROR));
                    SnackbarFactory.snackbarRequest(getView(), R.string.error_cannot_connect, -1, Snackbar.LENGTH_LONG);
                    return;
                }

                // send screen resolution
                try {
                    serverConnection.sendScreenResolution(screenWidth, screenHeight);
                } catch (IOException e) {
                    SnackbarFactory.snackbarRequest(getView(), R.string.error_cant_send_screen_res, -1, Snackbar.LENGTH_LONG);
                    serverConnection.disconnect();
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
                        .doOnError((error) -> SnackbarFactory.snackbarRequest(getView(), R.string.error_receiving_images, -1, Snackbar.LENGTH_LONG))
                        .subscribe(bitmap -> remoteVRView.updateImage(bitmap), Throwable::printStackTrace);

                // game input
                // gyro
                Observable.interval(16, TimeUnit.MILLISECONDS, Schedulers.io())
                        .map(tick -> orientationProvider.getQuaternion())
                        .map(quaternion -> GyroInput.getInstance().putPayload(quaternion))
                        .subscribeOn(Schedulers.io())
                        //.doOnSubscribe(orientationProvider::start)
                        //.doOnUnsubscribe(orientationProvider::stop)
                        .doOnError((error) -> SnackbarFactory.snackbarRequest(getView(), R.string.error_sending_gyro, -1, Snackbar.LENGTH_LONG))
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

    @Override
    public void onStart() {
        super.onStart();
        controllerManager.start();
    }

    @Override
    public void onStop() {
        controllerManager.stop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(serverConnection != null) {
            serverConnection.disconnect();
            serverConnection=null;
        }
        orientationProvider.stop();
        controllerManager=null;
    }

    // events
    @Subscribe
    public void onServerConnecting(Events.ServerConnecting e) {
        connectionInProgressView.setVisibility(View.VISIBLE);
        notConnectedView.setVisibility(View.GONE);
        connectedView.setVisibility(View.GONE);
    }

    @Subscribe
    public void onServerConnected(Events.ServerConnected e) {
        EventBus.getInstance().post(new Events.GoFullScreen(true));

        connectionInProgressView.setVisibility(View.GONE);
        connectedView.setVisibility(View.VISIBLE);
        notConnectedView.setVisibility(View.GONE);
    }

    @Subscribe
    public void onServerDisconnected(Events.ServerDisconnected e) {
        EventBus.getInstance().post(new Events.GoFullScreen(false));

        connectionInProgressView.setVisibility(View.GONE);
        connectedView.setVisibility(View.GONE);
        notConnectedView.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onGameViewSwipeDetected(Events.RemoteView_SwipeTopBottom e) {
        disconnectServer(null);
    }

    @Subscribe
    public void disconnectServer(Events.DisconnectServer e) {
        if(serverConnection != null){
            serverConnection.disconnect();
            serverConnection=null;
        }
    }
}