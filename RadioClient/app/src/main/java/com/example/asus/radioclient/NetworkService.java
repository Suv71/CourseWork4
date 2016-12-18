package com.example.asus.radioclient;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.UnsupportedEncodingException;

public class NetworkService extends Service {

    final String LOG_TAG = "testService";



    private Client _client;
    private Handler _clientHandler;

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

            _clientHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    super.handleMessage(msg);

                    switch (msg.what)
                    {
                        case Client.connectErr:
                        {
                            Intent intent = new Intent("Connection");
                            intent.putExtra("command", Client.connectErr);
                            sendBroadcast(intent);
                            stopSelf();

                            break;
                        }

                        case Client.checkConnectionState:
                        {
                            Intent intent;
                            if(_client.CheckConnection())
                            {
                                intent = new Intent("Connection");
                                intent.putExtra("command", Client.connectionSuccessfully);
                                sendBroadcast(intent);
                            }
                            else
                            {
                                intent = new Intent("Connection");
                                intent.putExtra("command", Client.connectionUnSuccessfully);
                                sendBroadcast(intent);
                                stopSelf();
                            }
                            break;
                        }

                        case Client.newClient:
                        {
                            byte[] res = (byte[])msg.obj;
                            Intent intent = new Intent("Communication");
                            intent.putExtra("command", Client.newClient);

                            try
                            {
                                intent.putExtra("nickname", new String(res, "UTF-8"));
                            }
                            catch (UnsupportedEncodingException e)
                            {
                                e.printStackTrace();
                            }

                            sendBroadcast(intent);
                            Log.d(LOG_TAG, "Новый клиент прибыл");
                            break;
                        }
                        case Client.messageToClient:
                        {
                            Log.d(LOG_TAG, "Начало работы команды приема");
                            int id = msg.arg1;
                            byte res[] = (byte[])msg.obj;

                            Log.d(LOG_TAG, "id отправителя = " + id);
                            Log.d(LOG_TAG, "Размер принятого в хендлер буффера = " + res.length);

                            Intent intent = new Intent("Communication");
                            intent.putExtra("command", Client.messageToClient);
                            intent.putExtra("id", id);
                            intent.putExtra("fileBuf", res);

                            sendBroadcast(intent);
                            Log.d(LOG_TAG, "Конец работы команды приема");
                            break;
                        }

                        case Client.clientOut:
                        {
                            Log.d(LOG_TAG, "Команда clientOut работает");
                            int id = (int)msg.obj;
                            Intent intent = new Intent("Communication");
                            intent.putExtra("command", Client.clientOut);
                            intent.putExtra("id", id);

                            sendBroadcast(intent);
                            break;
                        }
                    }

                }
            };

            _client = new Client(serverIP, serverPort, _clientHandler);
            new Thread(_client).start();
        }
        else
        {
            switch (intent.getIntExtra("command", 0))
            {
                case Client.messageToClient:
                {
                    int id = intent.getIntExtra("id", 0);
                    byte[] temp = intent.getByteArrayExtra("fileBuf");

                    Log.d(LOG_TAG, "id получателя при отправлении" + id);
                    Log.d(LOG_TAG, "размер буффера при отправлении" + temp.length);
                    _client.SendMessage(BitConverter.getBytes(Client.messageToClient));
                    _client.SendMessage(BitConverter.getBytes(id));
                    _client.SendMessage(BitConverter.getBytes(temp.length));
                    _client.SendMessage(temp);
                    Log.d(LOG_TAG, "По идее все должно отправиться");
                    break;
                }

                case Client.connect:
                {
                    int nickSize = intent.getIntExtra("nickSize", 0);
                    Log.d("nickSize на отправку = ", String.valueOf(nickSize));
                    byte[] temp = intent.getByteArrayExtra("nickname");

                    _client.SendMessage(BitConverter.getBytes(Client.connect));
                    _client.SendMessage(BitConverter.getBytes(nickSize));
                    _client.SendMessage(temp);

                    break;
                }

                case Client.disconnect:
                {
                    _client.SendMessage(BitConverter.getBytes(Client.disconnect));

                    break;
                }

                default:
                    break;
            }

        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy()
    {
        _client.Close();
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }
}
