package com.example.asus.radioclient;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

/**
 * Created by ASUS on 01.12.2016.
 */

public class VoiceWorker
{
    private MediaRecorder _recorder;
    private MediaPlayer _player;

    private void ReleaseRecorder()
    {
        if (_recorder != null)
        {
            _recorder.release();
            _recorder = null;
        }
    }

    private void ReleasePlayer()
    {
        if (_player != null)
        {
            _player.release();
            _player = null;
        }
    }

    public void StartRecord(String filePath)
    {
        ReleaseRecorder();

        File outFile = new File(filePath);

        if (outFile.exists())
        {
            outFile.delete();
        }

        if(outFile != null)
        {
            System.out.println("Файл создан");
        }

        _recorder = new MediaRecorder();

        if(_recorder != null)
        {
            System.out.println("Media recorder создан");
        }

        _recorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        _recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        _recorder.setOutputFile(filePath);
        _recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try
        {
            _recorder.prepare();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        _recorder.start();
        System.out.println("Запись начата");
    }

    public void StopRecord()
    {
        if (_recorder != null)
        {
            _recorder.stop();
            System.out.println("Запись остановлена");
        }
    }

    public void Play(String filePath)
    {
        try
        {
            ReleasePlayer();
            _player = new MediaPlayer();
            _player.setDataSource(filePath);
            _player.prepare();
            _player.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void FreeResources()
    {
        ReleaseRecorder();
        ReleasePlayer();
    }
}
