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

public class Client implements Runnable{
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

            /*if(_clientSocket.isConnected())
            {
                System.out.println("connected");
            }
            else
            {
                System.out.println("not connected");
            }*/

            _outStream = _clientSocket.getOutputStream();
            _inStream = _clientSocket.getInputStream();

            Message msg;
            byte[] sizeArray = new byte[4];
            int fileSize;
            byte[] temp;
            int i;

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

    public byte[] GetMessage()
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
