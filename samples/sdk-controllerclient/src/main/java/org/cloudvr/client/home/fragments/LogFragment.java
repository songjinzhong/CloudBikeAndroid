package org.cloudvr.client.home.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.cloudvr.client.R;
import org.cloudvr.client.home.logging.ILogger;
import org.cloudvr.client.home.logging.LoggerBus;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * fragment responsible for showing the logs
 *
 * @author Pierfrancesco Soffritti
 */
public class LogFragment extends BaseFragment implements ILogger {
    @Bind(R.id.log_view) TextView logView;

    public LogFragment() {
    }

    public static LogFragment newInstance() {
        LogFragment fragment = new LogFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void register() {
        LoggerBus.getInstance().register(this);
    }

    @Override
    public void unregister() {
        LoggerBus.getInstance().unregister(this);
    }

    @Subscribe
    @Override
    public void onLog(LoggerBus.Log log) {
        switch (log.getType()) {
            case LoggerBus.Log.NORMAL:
                logView.append("\n" +log.getSender() +" : " +log.getMessage() +"\n");
                break;
            case LoggerBus.Log.ERROR:
                logView.append(Html.fromHtml("\n<br/><font color=\"red\">" +log.getSender() +" : " +log.getMessage() +"</font><br/>\n"));
                break;
            case LoggerBus.Log.FPS:
                break;
            case LoggerBus.Log.FPS_AVG:
                break;
        }
    }
}