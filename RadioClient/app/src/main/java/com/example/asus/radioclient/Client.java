package com.example.asus.radioclient;

import android.icu.text.UnicodeSet;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
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
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * Created by ASUS on 13.11.2016.
 */

public class Client implements Runnable{
    private String _serverIP;
    private int _serverPort;
    private Socket _clientSocket;

    private InputStream _inStream;
    private OutputStream _outStream;

    private Handler _inputStreamHandler;

    public static final int  msgError = 100;

    public Client(String serverIP, int serverPort, Handler inputStreamHandler)
    {
        _serverIP = serverIP;
        _serverPort = serverPort;
        _inputStreamHandler = inputStreamHandler;
    }

    @Override
    public void run()
    {
        Message msg;
        try
        {
            _clientSocket = new Socket(_serverIP, _serverPort);

            _outStream = _clientSocket.getOutputStream();
            _inStream = _clientSocket.getInputStream();


            byte[] sizeArray;
            int fileSize;
            byte[] temp;

            while(true)
            {
                sizeArray = GetMessage();
                fileSize = BitConverter.toInt32(sizeArray, 0);

                temp = null;

                do
                {
                    if (temp == null)
                    {
                        temp = GetMessage();
                    }
                    else
                    {
                        temp = BitConverter.MergeArrays(temp, GetMessage());
                    }
                }while (temp.length < fileSize);

                msg = _inputStreamHandler.obtainMessage(0, temp);
                _inputStreamHandler.sendMessage(msg);
            }

        }
        catch (ConnectException e)
        {
            e.printStackTrace();
            _inputStreamHandler.sendEmptyMessage(msgError);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            Close();
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

    private byte[] GetMessage()
    {
        byte buf[] = new byte[1024 * 20];
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

        return buf;
    }

    public boolean TestConnection()
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
