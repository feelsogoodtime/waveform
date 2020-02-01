package com.project.waveform.audio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("&&","&&MainCreate");

        Button buttonRecord = (Button) findViewById(R.id.buttonrecord); // 녹음 버튼
        Button buttonPlay = (Button) findViewById(R.id.buttonPlay); // 녹음 버튼
        Button buttonCheck = (Button) findViewById(R.id.buttonCheck);

        final EditText editTime = (EditText) findViewById(R.id.editText1);
        final SeekBar seekTime = (SeekBar) findViewById(R.id.seekBar1);


        buttonRecord.setOnClickListener(new View.OnClickListener() { // 녹음 버튼을 클릭했을때
            @Override
            public void onClick(View v) {
                Log.d("&&","&&MainActivity.buttonRecord.setOnClickListener");

                Log.d("Test", "Main01");
                RecordManager.recordFlag = true;
                Log.d("Test", "Main02");
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() { // 녹음 버튼을 클릭했을때
            @Override
            public void onClick(View v) {
                Log.d("&&","&&MainActivity.buttonPlay.setOnClickListener");

                Log.d("Test", "Main01");
                RecordManager.playFlag = true;
                Log.d("Test", "Main02");
            }
        });

        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "ID 값은 " + RecordManager.BufferStore[0] + RecordManager.BufferStore[1] + RecordManager.BufferStore[2]
                        + RecordManager.BufferStore[3] ,Toast.LENGTH_LONG).show();
            }
        });



        seekTime.setMax(9);
        seekTime.setProgress(0);


        seekTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("&&","&&MainActivity.seekTime.setOnSeekBarChangeListener");

                RecordManager.seekTime = progress + 1;
                BoardManager.seekTime = progress + 1;
                RecordManager.get(progress + 1);
                //BoardManager.get(progress + 1);
                editTime.setText(String.valueOf(RecordManager.seekTime));
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {

            }
        });

        //마시멜로우 이상 버전 권한 체크
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    //마시멜로우 이상 버전 권한 체크
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "RECORD 권한 승인", Toast.LENGTH_LONG).show();
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "RECORD 권한 거부.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "RECORD 권한 승인 부여 받지 못함", Toast.LENGTH_LONG).show();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
