package com.example.asus.radioclient;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.UnsupportedEncodingException;

public class NetworkService extends Service {

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
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
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
                            Intent intent = new Intent("Chat");
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
                            break;
                        }
                        case Client.messageToClient:
                        {
                            Intent intent = new Intent("Chat");

                            if(msg.arg1 == 1)
                            {
                                String nickname = msg.obj.toString();
                                intent.putExtra("command", Client.messageToClient);
                                intent.putExtra("nickname", nickname);

                                sendBroadcast(intent);
                            }
                            else
                            {
                                byte res[] = (byte[])msg.obj;
                                intent.putExtra("command", Client.messageToClient);
                                intent.putExtra("fileBuf", res);

                                sendBroadcast(intent);
                            }

                            break;
                        }

                        case Client.clientOut:
                        {
                            String nickname = msg.obj.toString();
                            Intent intent = new Intent("Chat");
                            intent.putExtra("command", Client.clientOut);
                            intent.putExtra("nickname", nickname);

                            sendBroadcast(intent);
                            break;
                        }

                        case Client.activeClients:
                        {
                            String[] clients = (String[]) msg.obj;

                            Intent intent = new Intent("Chat");
                            intent.putExtra("command", Client.activeClients);
                            intent.putExtra("clients", clients);

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
                    String nickname = intent.getStringExtra("nickname");
                    byte[] temp = intent.getByteArrayExtra("fileBuf");

                    _client.SendMessage(BitConverter.getBytes(Client.messageToClient));
                    _client.SendMessage(BitConverter.getBytes(nickname.length()));
                    _client.SendMessage(nickname.getBytes());
                    _client.SendMessage(BitConverter.getBytes(temp.length));
                    _client.SendMessage(temp);

                    break;
                }

                case Client.connect:
                {
                    int nickSize = intent.getIntExtra("nickSize", 0);
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
    }
}
