package me.ibrohim.mushroom.network;

import android.content.Context;
import android.provider.Settings;
import android.telephony.SmsManager;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;

import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import me.ibrohim.mushroom.TermLog;

/**
 * Created by ibrohim on 9/25/16.
 */
public class WSClient {

    private static final String SERVER = "wss://risthanata.ahmadiyah.id/smsgateway";
    private static final int TIMEOUT = 5000;

    private String HWID;
    private Queue<SMS> messageQueue;

    private WebSocket webSocket;
    private TermLog log;
    Thread routine;

    WebSocketFactory websocketFactory;

    public WSClient(Context ctx, TermLog _log) throws IOException {
        HWID = Settings.Secure.getString(ctx.getContentResolver(),Settings.Secure.ANDROID_ID);
        log = _log;
        messageQueue = new LinkedList<>();

        websocketFactory = new WebSocketFactory();
        websocketFactory.setConnectionTimeout(TIMEOUT);

        webSocket = websocketFactory.createSocket(SERVER);
        webSocket.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);
        webSocket.addListener(new WebSocketAdapter() {

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                log.Log(TermLog.LOG_INFO, "Connected to server.");

                websocket.sendText("{\"action\":\"register\", \"HWID\":\"" + HWID + "\"}");

                routine = new SMSGRoutine();
                routine.start();
            }

            @Override
            public void onTextMessage(WebSocket websocket, String message) {
                log.Log(TermLog.LOG_VERBOSE, "Received: " + message);

                try {
                    SMS newMessage = new SMS();
                    JSONObject jsonMessage = new JSONObject(message);
                    newMessage.id = jsonMessage.getString("id");
                    newMessage.destination = jsonMessage.getString("destination");
                    newMessage.body = jsonMessage.getString("body");

                    synchronized (messageQueue) {
                        messageQueue.add(newMessage);
                        messageQueue.notifyAll();
                    }

                    websocket.sendText("{\"action\":\"update\", \"id\":\"" + newMessage.id + "\", \"status\":1}");

                } catch (JSONException e) {
                    e.printStackTrace();
                    log.Log(TermLog.LOG_ERROR, e.getMessage());
                }

            }

            @Override
            public void onDisconnected(WebSocket websocket,
                                       WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                                       boolean closedByServer) throws Exception
            {
                log.Log(TermLog.LOG_INFO, "Disconnected from server.");
                new Reconnect(5);
            }


            @Override
            public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception
            {
                exception.printStackTrace();
                log.Log(TermLog.LOG_ERROR, exception.getMessage());
                new Reconnect(5);
            }
        });
    }

    public void init() throws IOException {
        if (webSocket.getState() == WebSocketState.CLOSED) {
            webSocket = webSocket.recreate();
        }

        log.Log(TermLog.LOG_INFO, "Connecting to server...");
        webSocket.connectAsynchronously();
    }

    public boolean isConnected(){
        return webSocket.isOpen();
    }

    private class SMS {
        String id;
        String destination;
        String body;

        public String toString(){
            return "["+destination+"] "+body;
        }
    }

    public class Reconnect extends TimerTask {

        public Reconnect(int duration) {
            log.Log(TermLog.LOG_INFO, "Reconnect to server in "+duration+"s.");
            new Timer().schedule(this, duration*1000);
        }

        public void run(){
            try {
                init();
            } catch (IOException e) {
                e.printStackTrace();
                log.Log(TermLog.LOG_ERROR, e.getMessage());
            }
        }
    }

    private class SMSGRoutine extends Thread{

        private SmsManager smsManager;

        public SMSGRoutine() {
            smsManager = SmsManager.getDefault();
        }

        @Override
        public void run() {
            while (true) {

                SMS newMessage;

                synchronized (messageQueue) {

                    while (messageQueue.isEmpty()) {
                        try {
                            messageQueue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    newMessage = messageQueue.remove();
                }

                log.Log(TermLog.LOG_INFO, "Sending message: " + newMessage);
                smsManager.sendTextMessage(newMessage.destination, null, newMessage.body, null, null);

            }
        }
    }
}
