package org.cloudvr.client.home.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;

import org.cloudvr.client.controller.ControllerClientActivity;
import org.cloudvr.client.controller.OrientationView;
import org.cloudvr.client.R;
import org.cloudvr.client.home.logging.LoggerBus;

import java.util.List;
import java.util.Locale;

/**
 * Created by Hepsilion on 2017/1/13.
 */
public class ControllerFragment extends BaseFragment {
    private static final String TAG = "ControllerClient";

    private RelativeLayout test_controller=null;

    public ControllerFragment() {
    }

    public static ControllerFragment newInstance() {
        return new ControllerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controller, container, false);
        test_controller= (RelativeLayout) view.findViewById(R.id.controller_view);
        test_controller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(), ControllerClientActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    protected void unregister() {
        LoggerBus.getInstance().unregister(this);
    }

    @Override
    protected void register() {
        LoggerBus.getInstance().register(this);
    }
}
