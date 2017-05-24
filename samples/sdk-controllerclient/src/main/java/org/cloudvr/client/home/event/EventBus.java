package org.cloudvr.client.home.event;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * @author Pierfrancesco Soffritti
 */
public class EventBus {
    private static EventBus ourInstance = new EventBus();

    private Bus bus;
    private Handler handler;

    private EventBus() {
        bus = new Bus("events");
        handler = new Handler(Looper.getMainLooper());
    }

    public static EventBus getInstance() {
        return ourInstance;
    }

    public void register(Object obj) {
        bus.register(obj);
    }

    public void unregister(Object obj) {
        bus.unregister(obj);
    }

    public void post(final Object event) {
        handler.post(() -> bus.post(event));
    }
}