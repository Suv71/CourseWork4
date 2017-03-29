package com.example.asus.radioclient;

import android.icu.text.UnicodeSet;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UTFDataFormatException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * Created by ASUS on 13.11.2016.
 */

public class Client implements Runnable{

    public static final int messageToClient = 1;
    public static final int newClient = 2;
    public static final int clientOut = 3;
    public static final int connect = 4;
    public static final int disconnect = 5;
    public static final int activeClients = 6;

    public static final int checkConnectionState = 50;
    public static final int connectionSuccessfully = 51;
    public static final int connectionUnSuccessfully = 52;

    public static final int  connectErr = 100;

    private String _serverIP;
    private int _serverPort;
    private Socket _clientSocket;

    private InputStream _inStream;
    private OutputStream _outStream;

    private Handler _clientHandler;



    public Client(String serverIP, int serverPort, Handler clientHandler)
    {
        _serverIP = serverIP;
        _serverPort = serverPort;
        _clientHandler = clientHandler;
    }

    @Override
    public void run()
    {
        try
        {
            _clientHandler.sendEmptyMessageDelayed(checkConnectionState, 5000);

            _clientSocket = new Socket(_serverIP, _serverPort);

            _outStream = _clientSocket.getOutputStream();
            _inStream = _clientSocket.getInputStream();

            byte[] commandBuf;
            int command = 0;

            while(true)
            {
                commandBuf = GetMessage(4);

                command = BitConverter.toInt32(commandBuf, 0);

                if (command == 0)
                {
                    break;
                }
                else
                {
                    HandleCommand(command);
                }
            }

        }
        catch (ConnectException e)
        {
            e.printStackTrace();
            _clientHandler.sendEmptyMessage(connectErr);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            Close();
        }
    }

    private void HandleCommand(int command)
    {
        byte[] temp = null;
        Message msg;

        switch(command)
        {
            case messageToClient:
            {

                int nickSize = BitConverter.toInt32(GetMessage(4), 0);

                String nickname = null;

                try
                {
                    nickname = new String(GetMessage(nickSize), "UTF-8");
                }
                catch(UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }

                int fileSize = BitConverter.toInt32(GetMessage(4), 0);

                do
                {
                    if (temp == null)
                    {
                        temp = GetMessage(0);
                    }
                    else
                    {
                        temp = BitConverter.MergeArrays(temp, GetMessage(0));
                    }
                }while (temp.length < fileSize);

                msg = _clientHandler.obtainMessage(messageToClient, 1, 0, nickname);
                _clientHandler.sendMessage(msg);

                msg = _clientHandler.obtainMessage(messageToClient, 2, 0, temp);
                _clientHandler.sendMessage(msg);

                break;
            }

            case newClient:
            {
                int nickSize = BitConverter.toInt32(GetMessage(4), 0);
                temp = GetMessage(nickSize);

                msg = _clientHandler.obtainMessage(newClient, temp);
                _clientHandler.sendMessage(msg);

                break;
            }

            case clientOut:
            {
                int nickSize = BitConverter.toInt32(GetMessage(4), 0);
                String nickname = null;

                try
                {
                    nickname = new String(GetMessage(nickSize), "UTF-8");
                }
                catch(UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }

                msg = _clientHandler.obtainMessage(clientOut, nickname);
                _clientHandler.sendMessage(msg);

                break;
            }

            case activeClients:
            {
                int clientsNumber = BitConverter.toInt32(GetMessage(4), 0);
                String[] clients = new String[clientsNumber];

                int strSize;

                for(int i = 0; i < clientsNumber; i++)
                {
                    strSize = BitConverter.toInt32(GetMessage(4), 0);

                    try
                    {
                        clients[i] = new String(GetMessage(strSize), "UTF-8");
                    }
                    catch(UnsupportedEncodingException e)
                    {
                        e.printStackTrace();
                    }
                }

                msg = _clientHandler.obtainMessage(activeClients, clients);
                _clientHandler.sendMessage(msg);

                break;
            }

            default:
                break;
        }
    }

    public void SendMessage(byte[] message)
    {
        try
        {
            _outStream.write(message);
            _outStream.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean CheckConnection()
    {
        try
        {
            if(_clientSocket.isConnected())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            return false;
        }

    }

    private byte[] GetMessage(int byteNumber)
    {
        byte[] buf;

        if(byteNumber == 0)
        {
            buf = new byte[1024 * 20];
            int bytes = 0;

            try
            {
                bytes = _inStream.read(buf);

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if(bytes < 1024 * 20)
            {
                byte result[] = new byte[bytes];
                for (int i = 0; i < bytes; i++)
                {
                    result[i] = buf[i];
                }
                return result;
            }
        }
        else
        {
            buf = new byte[byteNumber];

            try
            {
                _inStream.read(buf, 0, byteNumber);

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return buf;
    }


    public void Close()
    {
        try
        {
            if(_inStream != null)
            {
                _inStream.close();
            }
            if(_outStream != null)
            {
                _outStream.close();
            }
            if(_clientSocket != null)
            {
                _clientSocket.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
