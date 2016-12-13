package com.example.asus.radioclient;

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

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Client _client;
    //private Button _btn;
    //private EditText _et;
    //private TextView _tv;
    private Handler _inputStreamHandler;

    private String fileName;
    private String fileName2;

    private byte[] temp;

    private VoiceWorker _voiceWorker;
    private FileWorker _fileWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileName = Environment.getExternalStorageDirectory() + "/record.3gpp";

        fileName2 = Environment.getExternalStorageDirectory() + "/record2.3gpp";

        //_btn = (Button)findViewById(R.id.btnSend);
        //_et = (EditText)findViewById(R.id.etMessage);
        //_tv = (TextView)findViewById(R.id.tvServerAnswer);

        _voiceWorker = new VoiceWorker();
        _fileWorker = new FileWorker();


        _inputStreamHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);

                byte res[] = (byte[])msg.obj;
                _fileWorker.BytesToFile(res, fileName2);
                _voiceWorker.Play(fileName2);
            }
        };

        _client = new Client("192.168.0.101", 31010, _inputStreamHandler);
        new Thread(_client).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _voiceWorker.FreeResources();
        _client.Close();
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
        temp = _fileWorker.FileToBytes(fileName);
        _client.SendMessage(BitConverter.getBytes(temp.length));
        _client.SendMessage(temp);
    }

    public void OnClickAppExit(View view)
    {
        _client.Close();
        finish();
    }



}
