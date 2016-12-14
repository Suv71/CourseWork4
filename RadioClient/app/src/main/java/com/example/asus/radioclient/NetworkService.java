package com.example.asus.radioclient;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class NetworkService extends Service {

    final String LOG_TAG = "testService";

    private Client _client;
    private Handler _inputStreamHandler;

    public NetworkService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate()
    {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(LOG_TAG, "onStartCommand");
        if(startId == 1)
        {
            String serverIP = intent.getStringExtra("serverIP");
            int serverPort = intent.getIntExtra("serverPort", 0);

            _inputStreamHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    super.handleMessage(msg);

                    if(msg.what == Client.msgError)
                    {
                        //_tv.setText("Сервер не отвечает");
                    }
                    else
                    {
                        byte res[] = (byte[])msg.obj;
                        Log.d(LOG_TAG, "Размер принятого буффера = " + res.length);
                        Intent intent = new Intent("Messages2");
                        intent.putExtra("temp", res);
                        sendBroadcast(intent);
                    }

                }
            };

            _client = new Client(serverIP, serverPort, _inputStreamHandler);
            new Thread(_client).start();
        }
        else
        {
            byte[] temp = intent.getByteArrayExtra("temp");
            _client.SendMessage(BitConverter.getBytes(temp.length));
            _client.SendMessage(temp);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy()
    {
        super.onDestroy();
        _client.Close();
        Log.d(LOG_TAG, "onDestroy");
    }
}
