package com.example.asus.radioclient;

import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    private Client _client;
    private Button _btn;
    private EditText _et;
    private TextView _tv;
    private Handler _inputStreamHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _btn = (Button)findViewById(R.id.btnSend);
        _et = (EditText)findViewById(R.id.etMessage);
        _tv = (TextView)findViewById(R.id.tvServerAnswer);

        _inputStreamHandler = new Handler()
        {
            public void handleMessage(android.os.Message msg)
            {
                _tv.setText(msg.obj.toString());
            }
        };

        _client = new Client("10.175.147.229", 6000, _inputStreamHandler);
        _client.start();
    }

    public void OnClickSend(View view)
    {
        _client.SendMessage(_et.getText().toString());
    }



}
