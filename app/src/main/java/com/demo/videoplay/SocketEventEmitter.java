package com.demo.videoplay;

import android.util.Log;

import com.demo.callback.SocketEventListener;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import static com.demo.videoplay.Constants.EVENT_JOIN;
import static com.demo.videoplay.Constants.EVENT_ROOM_FULL;
import static com.demo.videoplay.Constants.ROOM_ID;

public class SocketEventEmitter {
    String TAG = SocketEventListener.class.getSimpleName();
    private BaseApp app;
    private Socket mSocket;
    private MessageHandler messageHandler;
    private String mVideoUrl;
    private SocketEventListener socketEventListener;
    private boolean mIsRoomCreator;
    private String mRoomID = Constants.ROOM_ID;

    public SocketEventEmitter(SocketEventListener socketEventListener) {
        this.socketEventListener = socketEventListener;
        app = (BaseApp) BaseApp.getAppContext();
        messageHandler = new MessageHandler();
        mSocket = app.getSocket();
        mSocket.connect();

        mSocket.emit(EVENT_JOIN, ROOM_ID);
        mSocket.on(Socket.EVENT_CONNECT, messageHandler.onConnected);
        mSocket.on(Socket.EVENT_DISCONNECT, messageHandler.onDisconnected);
        mSocket.on(Constants.EVENT_ROOM_CREATED, messageHandler.roomCreated);
        mSocket.on(Constants.EVENT_ROOM_JOINED, messageHandler.roomJoined);
        mSocket.on(Constants.EVENT_PLAY_VIDEO, messageHandler.playVideo);
        mSocket.on(EVENT_ROOM_FULL, messageHandler.roomFull);
    }

    private class MessageHandler {
        private MessageHandler() {

        }

        private Emitter.Listener onConnected = args -> {
            try {
                Log.i(TAG, "EVENT:" + "Connected");
            } catch (Exception exp) {
                exp.printStackTrace();
            }

        };
        private Emitter.Listener onDisconnected = args -> {
            Log.i(TAG, "EVENT:" + "DisConnected");

        };

        private Emitter.Listener roomCreated = args -> {
            Log.i(TAG, "EVENT: " + Constants.EVENT_ROOM_CREATED);

            try {
                JSONObject info = (JSONObject) args[0];
                String key = info.getString("YT_API_KEY");
                new Thread(new Task(key)).start();
                Thread.sleep(2000);
                JSONObject data = new JSONObject();
                data.put("yt_video_id", "");
                data.put("roomId", ROOM_ID);

                mSocket.emit(Constants.EVENT_SET_CURRENT_VIDEO, data);// Start play You tube music
                mIsRoomCreator = true;
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        };

        private Emitter.Listener roomJoined = args -> {
            Log.i(TAG, "EVENT: " + Constants.EVENT_ROOM_JOINED);

            try {
                JSONObject info = (JSONObject) args[0];
                String key = info.getString("YT_API_KEY");
                new Thread(new Task(key)).start();
                Thread.sleep(2000);
                mSocket.emit(Constants.EVENT_GET_CURRENT_VIDEO, ROOM_ID);// Start play You tube music
            } catch (Exception exp) {
                exp.printStackTrace();

            }
        };

        private Emitter.Listener playVideo = args -> {
            Log.i(TAG, "EVENT: " + Constants.EVENT_PLAY_VIDEO);
            try {
                JSONObject info = (JSONObject) args[0];
                String v_id = info.getString("yt_video_id");
                mVideoUrl = v_id;
                boolean roomCreator = info.getBoolean("isRoomCreator");

                if (roomCreator && mIsRoomCreator) // Actually who is set the current video he/she is Media center for playing video
                {
                    socketEventListener.onStartVideo(mVideoUrl);
                } else if (!roomCreator && !mIsRoomCreator) { // This is for get video info play accordingly
                    socketEventListener.onStartVideo(mVideoUrl);
                }
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        };


        private Emitter.Listener roomFull = args -> {
            Log.i(TAG, "EVENT: " + EVENT_ROOM_FULL);

        };
    }


    class Task implements Runnable {
        String key;

        Task(String str) {
            key = str;
        }

        @Override
        public void run() {
            socketEventListener.onInitializeYTVideoPlayer(key);
        }
    }
}
