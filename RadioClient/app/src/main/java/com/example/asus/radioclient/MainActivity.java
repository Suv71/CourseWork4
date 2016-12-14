package com.example.asus.radioclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    final String LOG_TAG = "testService";

    private Client _client;
    //private Button _btn;
    //private EditText _et;
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

        //_btn = (Button)findViewById(R.id.btnSend);
        //_et = (EditText)findViewById(R.id.etMessage);
        _tv = (TextView)findViewById(R.id.tvServerAnswer);

        _voiceWorker = new VoiceWorker();
        _fileWorker = new FileWorker();

        _receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                temp = intent.getByteArrayExtra("temp");
                _fileWorker.BytesToFile(temp, fileName2);
                _voiceWorker.Play(fileName2);
            }
        };

        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter("Messages");
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(_receiver, intFilt);

        startService(new Intent(this, NetworkService.class).putExtra("serverIP", "192.168.0.101").putExtra("serverPort", 31010));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _voiceWorker.FreeResources();
        unregisterReceiver(_receiver);
        stopService(new Intent(this, NetworkService.class));
    }

    public void recordStart(View v)
    {
        _voiceWorker.StartRecord(fileName);
    }

    public void recordStop(View v)
    {
        _voiceWorker.StopRecord();
    }

    public void playStart(View v)
    {
        _voiceWorker.Play(fileName);
    }


    public void OnClickSend(View view)
    {
        Intent intent = new Intent(this, CommunicationActivity.class);
        startActivity(intent);
        /*Intent intent = new Intent(this, NetworkService.class);
        temp = _fileWorker.FileToBytes(fileName);
        intent.putExtra("temp", temp);
        startService(intent);*/
    }
}
