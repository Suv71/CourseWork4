package com.example.asus.radioclient;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Client _client;
    private Button _btn;
    private EditText _et;
    private TextView _tv;
    private Handler _inputStreamHandler;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String fileName;
    private String fileName2;

    private byte[] temp;
    private byte[] resul;
    private int byteNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileName = Environment.getExternalStorageDirectory() + "/record.3gpp";
        fileName2 = Environment.getExternalStorageDirectory() + "/record2.3gpp";

        System.out.println(fileName);
        System.out.println(fileName2);

        _btn = (Button)findViewById(R.id.btnSend);
        _et = (EditText)findViewById(R.id.etMessage);
        _tv = (TextView)findViewById(R.id.tvServerAnswer);

        byteNumber = 0;

        _inputStreamHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);

                //byte res[] = (byte[])msg.obj;
                int fileSize = (int)msg.obj;
                System.out.println("File size = " + fileSize);
                //System.out.println("Количество полученных байтов = " + res.length);
                //byteNumber += res.length;
                //System.out.println("byteNumber сейчас = " + byteNumber);

                /*if(byteNumber != temp.length)
                {
                    resul = BiggerArray(resul, res);
                }
                else
                {
                    resul = BiggerArray(resul, res);
                    BytesToFile(resul, fileName2);
                    try
                    {
                        releasePlayer();
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(fileName2);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }*/


                //_tv.setText(msg.obj.toString());
            }
        };

        _client = new Client("10.175.147.229", 31010, _inputStreamHandler);
        _client.start();
        System.out.println("Клиентский поток стартовал");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        releaseRecorder();
        _client.Close();
    }

    public void OnClickSend(View view)
    {
        _client.SendMessage(BitConverter.getBytes(10000));
        //System.out.println("Количество байтов в temp = " + temp.length);
        //_client.SendMessage(temp);
    }

    public void OnClickAppExit(View view)
    {
        _client.Close();
        finish();
    }



}
