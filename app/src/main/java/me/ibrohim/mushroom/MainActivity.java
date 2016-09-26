package me.ibrohim.mushroom;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        try {

            wsClient = new WSClient(this, myLogger);
            wsClient.init();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MyTermLog extends TermLog {



        public void Log(final String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLog.append(msg + "\n");
                }
            });
            scrollView.fullScroll(View.FOCUS_DOWN);
        }

    }
}
