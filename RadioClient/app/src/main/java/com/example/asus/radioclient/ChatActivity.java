package com.example.asus.radioclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends Activity {

    private final String _outFilePath = Environment.getExternalStorageDirectory() + "/outRecord.3gpp";
    private final String _inFilePath = Environment.getExternalStorageDirectory() + "/inRecord.3gpp";

    private byte[] _temp;

    private VoiceWorker _voiceWorker;
    private FileWorker _fileWorker;

    private BroadcastReceiver _chatReceiver;

    private ArrayList<String> _users = new ArrayList<String>();
    private ListView _lv;

    private TextView _tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        _voiceWorker = new VoiceWorker();
        _fileWorker = new FileWorker();

        _lv = (ListView)findViewById(R.id.lvUserList);

        _lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, _users);

        _lv.setAdapter(adapter);

        _chatReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getIntExtra("command", 0))
                {
                    case Client.messageToClient:
                    {
                        if(intent.hasExtra("nickname"))
                        {
                            _tv = (TextView)findViewById(R.id.tvInUser);
                            _tv.setText(intent.getStringExtra("nickname"));
                        }

                        if(intent.hasExtra("fileBuf"))
                        {
                            _temp = intent.getByteArrayExtra("fileBuf");
                            _fileWorker.BytesToFile(_temp, _inFilePath);
                            _voiceWorker.Play(_inFilePath);
                        }

                        break;
                    }

                    case Client.newClient:
                    {
                        String nickname = intent.getStringExtra("nickname");
                        _users.add(nickname);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(context, "К чату подключился пользователь: " + nickname, Toast.LENGTH_SHORT).show();

                        break;
                    }

                    case Client.clientOut:
                    {
                        String nickname= intent.getStringExtra("nickname");
                        _users.remove(nickname);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(context, "Чат покинул пользователь: " + nickname, Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case Client.activeClients:
                    {
                        String[] clients= intent.getStringArrayExtra("clients");
                        for(int i = 0; i < clients.length; i++)
                        {
                            _users.add(clients[i]);
                        }
                        adapter.notifyDataSetChanged();
                        Toast.makeText(context, "Активные пользователи добавлены", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };

        IntentFilter intFilter = new IntentFilter("Chat");

        registerReceiver(_chatReceiver, intFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Disconnect();
        _voiceWorker.FreeResources();
        unregisterReceiver(_chatReceiver);
        stopService(new Intent(this, NetworkService.class));
    }

    private void Disconnect()
    {
        Intent intent = new Intent(this, NetworkService.class);
        intent.putExtra("command", Client.disconnect);
        startService(intent);
    }

    public void StartRecord(View v) {
        _voiceWorker.StartRecord(_outFilePath);
    }

    public void StopRecord(View v) {
        _voiceWorker.StopRecord();
    }

    public void Play(View v) {
        _voiceWorker.Play(_inFilePath);
    }

    public void Send(View view) {
        _lv = (ListView)findViewById(R.id.lvUserList);
        int position = _lv.getCheckedItemPosition();

        if(position != -1)
        {
            Intent intent = new Intent(this, NetworkService.class);

            _temp = _fileWorker.FileToBytes(_outFilePath);

            intent.putExtra("command", Client.messageToClient);
            intent.putExtra("nickname", _users.get(position));
            intent.putExtra("fileBuf", _temp);

            startService(intent);
        }
        else
        {
            Toast.makeText(this, "Выберите пользователя для отправки сообщения", Toast.LENGTH_SHORT).show();
        }
    }
}
