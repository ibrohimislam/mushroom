package me.ibrohim.mushroom;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;

import me.ibrohim.mushroom.network.WSClient;

public class MainActivity extends AppCompatActivity {

    TextView mLog;
    ScrollView scrollView;
    TermLog myLogger;

    WSClient wsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLog = (TextView) findViewById(R.id.mLog);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        myLogger = new MyTermLog();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStart(){
        super.onStart();
        try {

            if (wsClient == null) {
                wsClient = new WSClient(this, myLogger);
            }

            if (!wsClient.isConnected()) {
                wsClient.init();
            }

        } catch (IOException e) {
            e.printStackTrace();
            myLogger.Log(MyTermLog.LOG_ERROR, e.getMessage());
        }
    }

    private class MyTermLog extends TermLog {
        public void Log(final Spanned msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLog.append(msg);
                }
            });
            scrollView.fullScroll(View.FOCUS_DOWN);
        }
    }
}
