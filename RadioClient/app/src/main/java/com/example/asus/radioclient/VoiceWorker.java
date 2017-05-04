package com.example.asus.radioclient;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by ASUS on 01.12.2016.
 */

public class VoiceWorker
{
    private final int _sampleRate = 8000;
    private final int _inChannels = AudioFormat.CHANNEL_IN_MONO;
    private final int _outChannels = AudioFormat.CHANNEL_OUT_MONO;
    private final int _audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private final int _audioSource = MediaRecorder.AudioSource.MIC;

    private int _recorderBufferSize = AudioRecord.getMinBufferSize(_sampleRate, _inChannels, _audioEncoding) * 4;

    private AudioRecord _recorder;
    private AudioTrack _player;

    private boolean _isRecording = false;

    private byte[] _recordingResult;

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

    public void StartRecord()
    {
        ReleaseRecorder();

        _recordingResult = new byte[_recorderBufferSize * 100];

        _recorder = new AudioRecord(_audioSource, _sampleRate, _inChannels, _audioEncoding, _recorderBufferSize);

        _recorder.startRecording();

        _isRecording = true;

        ReadingRecord();

    }

    private void ReadingRecord()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] audioBuffer = new byte[_recorderBufferSize];

                int currentCount = 0;
                int totalCount = 0;

                while (_isRecording) {
                    currentCount = _recorder.read(audioBuffer, 0, _recorderBufferSize);
                    System.arraycopy(audioBuffer, 0, _recordingResult, 0 + totalCount, currentCount);
                    totalCount += currentCount;
                }

                byte[] temp = new byte[totalCount];
                System.arraycopy(_recordingResult, 0, temp, 0, totalCount);

                _recordingResult = new byte[totalCount];
                System.arraycopy(temp, 0, _recordingResult, 0, totalCount);

            }
        }).start();
    }

    public byte[] StopRecord()
    {
        if (_recorder != null)
        {
            _isRecording = false;
            _recorder.stop();
            _recorder.release();

            return _recordingResult;
        }

        return null;
    }

    public void Play(byte[] record)
    {
        ReleasePlayer();

        int intSize = android.media.AudioTrack.getMinBufferSize(_sampleRate, _outChannels, _audioEncoding);
        _player = new AudioTrack(AudioManager.STREAM_MUSIC, _sampleRate, _outChannels, _audioEncoding, intSize, AudioTrack.MODE_STREAM);

        if (_player != null)
        {
            _player.play();
            // Write the byte array to the track
            _player.write(record, 0, record.length);
            _player.stop();
            _player.release();
        }
        else
        {
            System.out.println("Audio track isn't initialized");
        }

    }



    public void FreeResources()
    {
        ReleaseRecorder();
        ReleasePlayer();
    }
}
