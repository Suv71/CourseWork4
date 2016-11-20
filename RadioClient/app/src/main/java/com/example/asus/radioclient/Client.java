package com.example.asus.radioclient;

import android.icu.text.UnicodeSet;
import android.os.Handler;
import android.os.Message;
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
import java.io.UTFDataFormatException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * Created by ASUS on 13.11.2016.
 */

public class Client extends Thread{
    private String _serverIP;
    private int _serverPort;
    private Socket _clientSocket;

    private InputStream _inStream;
    private OutputStream _outStream;

    private Handler _inputStreamHandler;

    public Client(String serverIP, int serverPort, Handler inputStreamHandler)
    {
        _serverIP = serverIP;
        _serverPort = serverPort;
        _inputStreamHandler = inputStreamHandler;
    }

    @Override
    public void run()
    {
        try
        {
            _clientSocket = new Socket(_serverIP, _serverPort);

            _outStream = _clientSocket.getOutputStream();
            _inStream = _clientSocket.getInputStream();

            Message msg;
            String temp;

            while(true)
            {
                if(_inStream.available() > 0)
                {
                    temp = GetMessage();
                    msg = _inputStreamHandler.obtainMessage(0, temp);
                    _inputStreamHandler.sendMessage(msg);
                }
            }

        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
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

    public void SendMessage(String message)
    {
        try
        {
            _outStream.write(message.getBytes());
            _outStream.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String GetMessage()
    {
        byte buf[] = new byte[256];
        int bytes = 0;
        String temp = "";

        try
        {
            bytes = _inStream.read(buf);
            temp = new String(buf, 0, bytes);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return temp;
    }

    public void Close()
    {
        try
        {
            _inStream.close();
            _outStream.close();
            _clientSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
