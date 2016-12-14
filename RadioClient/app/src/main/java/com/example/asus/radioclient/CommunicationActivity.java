package com.example.asus.radioclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class CommunicationActivity extends AppCompatActivity {

    private byte[] temp;
    private FileWorker _fileWorker;
    private VoiceWorker _voiceWorker;
    private String fileName;
    private String fileName2;

    private BroadcastReceiver _receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);

        fileName = Environment.getExternalStorageDirectory() + "/record.3gpp";
        fileName2 = Environment.getExternalStorageDirectory() + "/record2.3gpp";
        _fileWorker = new FileWorker();
        _voiceWorker = new VoiceWorker();

        _receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                temp = intent.getByteArrayExtra("temp");
                _fileWorker.BytesToFile(temp, fileName2);
                _voiceWorker.Play(fileName2);
            }
        };

        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter("Messages2");
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(_receiver, intFilt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _voiceWorker.FreeResources();
        unregisterReceiver(_receiver);
    }

    public void OnClickTest(View v)
    {
        Intent intent = new Intent(this, NetworkService.class);
        temp = _fileWorker.FileToBytes(fileName);
        intent.putExtra("temp", temp);
        startService(intent);
    }


}
