package com.example.asus.radioclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by ASUS on 13.11.2016.
 */

public class Client implements Runnable{
    private String _serverIP;
    private int _serverPort;
    private Socket _clientSocket;

    public Client(String serverIP, int serverPort)
    {
        _serverIP = serverIP;
        _serverPort = serverPort;
    }

    @Override
    public void run()
    {
        try
        {
            _clientSocket = new Socket(_serverIP, _serverPort);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void SendMessage(String message)
    {
        try
        {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_clientSocket.getOutputStream())), true);
            out.println(message);
            out.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String GetMessage()
    {
        BufferedReader in;
        String temp = "";
        try
        {
            in = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));
            temp = in.readLine();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return temp;
    }
}
