package com.example.asus.radioclient;

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

public class MainActivity extends AppCompatActivity {
    final String LOG_TAG = "testService";

    private Button _btn;
    private EditText _et;
    private TextView _tv;

    private String fileName;
    private String fileName2;

    private byte[] temp;

    private VoiceWorker _voiceWorker;
    private FileWorker _fileWorker;

    private BroadcastReceiver _receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileName = Environment.getExternalStorageDirectory() + "/record.3gpp";

        fileName2 = Environment.getExternalStorageDirectory() + "/record2.3gpp";

        _voiceWorker = new VoiceWorker();
        _fileWorker = new FileWorker();

        _receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getIntExtra("command", 0))
                {
                    case Client.connectErr:
                    {
                        Toast.makeText(context, "Не удается подключиться к серверу.", Toast.LENGTH_SHORT).show();
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

                    case Client.messageToClient:
                    {
                        int id = intent.getIntExtra("id", 0);
                        Log.d(LOG_TAG, "id отправителя принятое в активити = " + id);
                        temp = intent.getByteArrayExtra("fileBuf");
                        Log.d(LOG_TAG, "Размер буффера файла принятого в активити = " + temp.length);

                        _fileWorker.BytesToFile(temp, fileName2);
                        _voiceWorker.Play(fileName2);
                        break;
                    }

                    case Client.newClient:
                    {
                        String nickname = intent.getStringExtra("nickname");
                        Log.d(LOG_TAG, "Nickname нового клиента = " + nickname);
                        break;
                    }

                    case Client.clientOut:
                    {
                        int id = intent.getIntExtra("id", 0);
                        Log.d("Активити", "Клиент покинул нас = " + id);

                        break;
                    }
                }

            }
        };


        IntentFilter intFilt = new IntentFilter("Connection");

        registerReceiver(_receiver, intFilt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*Log.d("Команда disconnect", "Работаем");
        Intent intent = new Intent(this, NetworkService.class);
        intent.putExtra("command", Client.disconnect);
        startService(intent);*/

        _voiceWorker.FreeResources();
        unregisterReceiver(_receiver);
        stopService(new Intent(this, NetworkService.class));
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


        if(!ipAdress.equals("") || port != 0)
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
            Log.d("Размер ника", String.valueOf(temp.length));

            try
            {
                Log.d("String", new String(temp, "UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }


            Intent intent = new Intent(this, NetworkService.class);
            intent.putExtra("command", Client.connect);
            intent.putExtra("nickSize", temp.length);
            intent.putExtra("nickname", temp);
            startService(intent);

            intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Введите ник для кодключения к чату", Toast.LENGTH_SHORT).show();
        }
    }


    public void recordStart(View v) {
        _voiceWorker.StartRecord(fileName);
    }

    public void recordStop(View v) {
        _voiceWorker.StopRecord();
    }

    public void playStart(View v) {
        _voiceWorker.Play(fileName);
    }

    public void OnClickDisconnect(View v) {
        Log.d("Команда disconnect", "Работаем");
        Intent intent = new Intent(this, NetworkService.class);
        intent.putExtra("command", Client.disconnect);
        startService(intent);
        onDestroy();
    }

    public void OnClickConnect(View view) {
        String nickname = _et.getText().toString();


        temp = nickname.getBytes();
        Log.d("Размер стринг", String.valueOf(temp.length));

        try {
            Log.d("String", new String(temp, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        Intent intent = new Intent(this, NetworkService.class);
        intent.putExtra("command", Client.connect);
        intent.putExtra("nickSize", temp.length);
        intent.putExtra("nickname", temp);
        startService(intent);
    }

    public void OnClickSend(View view) {
        int id = Integer.decode(_et.getText().toString());
        Intent intent = new Intent(this, NetworkService.class);
        temp = _fileWorker.FileToBytes(fileName);
        intent.putExtra("command", Client.messageToClient);
        intent.putExtra("id", id);
        intent.putExtra("fileBuf", temp);
        Log.d(LOG_TAG, "Размер отправляемого из активити файла = " + temp.length);
        startService(intent);
    }
}
