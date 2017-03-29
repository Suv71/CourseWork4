package com.example.asus.radioclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.server.converter.StringToIntConverter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class MainActivity extends Activity {

    private Button _btn;
    private EditText _et;
    private TextView _tv;

    private byte[] temp;

    private BroadcastReceiver _connectionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _connectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getIntExtra("command", 0))
                {
                    case Client.connectErr:
                    {
                        Toast.makeText(context, "Адрес и/или порт сервера не верны", Toast.LENGTH_SHORT).show();
                        _btn = (Button)findViewById(R.id.btnConnectServer);
                        _btn.setEnabled(true);

                        break;
                    }

                    case Client.connectionSuccessfully:
                    {
                        _tv = (TextView)findViewById(R.id.tvServerState);
                        _tv.setText("Подключен");

                        _btn = (Button)findViewById(R.id.btnConnectChat);
                        _btn.setEnabled(true);

                        break;
                    }

                    case Client.connectionUnSuccessfully:
                    {
                        _tv = (TextView)findViewById(R.id.tvServerState);
                        _tv.setText("Не удалось подключиться");

                        _btn = (Button)findViewById(R.id.btnConnectServer);
                        _btn.setEnabled(true);

                        break;
                    }
                }
            }
        };


        IntentFilter intFilt = new IntentFilter("Connection");

        registerReceiver(_connectionReceiver, intFilt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(_connectionReceiver);
    }

    public void ConnectServer(View v)
    {

        _et = (EditText)findViewById(R.id.etIpAdress);
        String ipAdress = _et.getText().toString();

        _et = (EditText)findViewById(R.id.etPort);
        int port = 0;
        try
        {
            port = Integer.decode(_et.getText().toString());
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }


        if(!ipAdress.equals("") && port != 0)
        {
            startService(new Intent(this, NetworkService.class).putExtra("serverIP", ipAdress).putExtra("serverPort", port));
            _btn = (Button)findViewById(R.id.btnConnectServer);
            _btn.setEnabled(false);

            _tv = (TextView)findViewById(R.id.tvServerState);
            _tv.setText("Идет подключение");

        }
        else
        {
            Toast.makeText(this, "Введите ip-адрес и/или порт", Toast.LENGTH_SHORT).show();
        }
    }

    public void ConnectChat(View v)
    {
        _et = (EditText)findViewById(R.id.etNickname);
        String nickname = _et.getText().toString();

        if(!nickname.equals(""))
        {
            temp = nickname.getBytes();

            Intent intent = new Intent(this, NetworkService.class);
            intent.putExtra("command", Client.connect);
            intent.putExtra("nickSize", temp.length);
            intent.putExtra("nickname", temp);
            startService(intent);

            intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            Toast.makeText(this, "Введите ник для подключения к чату", Toast.LENGTH_SHORT).show();
        }
    }
}
