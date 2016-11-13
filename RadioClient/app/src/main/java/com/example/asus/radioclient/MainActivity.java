package com.example.asus.radioclient;

import android.content.DialogInterface;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _btn = (Button)findViewById(R.id.btnSend);
        _et = (EditText)findViewById(R.id.etMessage);
        _tv = (TextView)findViewById(R.id.tvServerAnswer);

        _client = new Client("10.175.147.229", 6000);
        _client.run();
    }

    public void OnClickSend(View view)
    {
        _client.SendMessage(_et.getText().toString());
        _tv.setText(_client.GetMessage());
    }
}
