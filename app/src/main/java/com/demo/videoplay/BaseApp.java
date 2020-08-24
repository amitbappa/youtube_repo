package com.demo.videoplay;

import android.app.Application;
import android.content.Context;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class BaseApp extends Application {

    private static Context singletonInstance;
    private Socket mSocket;

    public static synchronized Context getAppContext() {
        if (null == singletonInstance) {
            singletonInstance = new BaseApp();
        }
        return singletonInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singletonInstance = this;

        try {
            mSocket = IO.socket(Constants.SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
