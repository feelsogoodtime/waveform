package com.project.waveform.audio;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ShortBuffer;
import java.util.Arrays;

class RecordManager extends Thread {
    boolean RunFlag, EndFlag;
    ShortBuffer shortBuffer, sendBuffer;
    int BufferRecordSize = 3200, BufferTrackSize = 3200, retBufferSize = 3200;
    static short[] BufferStore, BufferTrack;
    static int SamplingRate = 16000;
    static int seekTime = 1;
    static int BufferShortSize = SamplingRate * seekTime;
    AudioRecord audioRecord;
    AudioTrack audioTrack;
    BoardManager bdManager;
    int cnt = 0;
    int cntt = 0;

    static boolean recordFlag = false;
    static boolean playFlag = false;

    public RecordManager(BoardManager bdManager) { // ★ 초기화 몇번 되나 보기
        Log.d("&&","&&RecordManager생성자");
        sendBuffer = ShortBuffer.allocate(BufferRecordSize);
        BufferStore = new short[BufferRecordSize];
        shortBuffer = ShortBuffer.allocate(BufferShortSize * 10); // 10초까지 커버 가능

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SamplingRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,BufferRecordSize); //bufferSizeInBytes == getMinBufferSize()에서 받은 버퍼 사이즈 // 지정해주면 안됨. getMinBufferSize() 써야함

        EndFlag = true;
        this.bdManager = bdManager;
    }



    public static void get (int time) {
        Log.d("&&","&&RecordManager.get");

        BufferShortSize = BufferShortSize * time;
    }

    void onStart() {
        Log.d("&&","&&RecordManager.onStart");

        //RunFlag = true;
    }

    void onPause() {
        Log.d("&&","&&RecordManager.onPause");

        RunFlag = false;
    }

    void onStop() {
        RunFlag = false;
        EndFlag = false;
        Log.d("&&","&&RecordManager.onStop");

        try {
            join();
        } catch (Exception e) {
            Log.d("Test", e.toString());
        }

        audioRecord.stop();
        audioRecord.release();
    }

    void setBoardManager(BoardManager bdManager) {
        Log.d("&&","&&RecordManager.setBoardManager");

        this.bdManager = bdManager;
    }

    @Override
    public void run() {

        int sum = 0;
        int min = 0;

        super.run();
        while (EndFlag) {
            if (recordFlag) {
                Log.d("&&","&&RecordManager.recordFlag");

                BufferShortSize = SamplingRate * seekTime;
                audioRecord.startRecording(); // 녹음시작
                Arrays.fill(sendBuffer.array(), (short) 0);
                Arrays.fill(shortBuffer.array(), (short) 0);



                shortBuffer.rewind(); // rewind() -> 버퍼의 position을 0 으로 바꿈 // limit는 변하지 않음 // .position(0) == rewind()
                while (shortBuffer.position() + BufferRecordSize <= BufferShortSize) { // 버퍼포지션 0,1,2,... + 녹음되는 프레임 사이즈 < 전체 버퍼크기 -> 설정한 limit 버퍼 크기보다 작으면 계속 프레임 단위로 녹음
                    if (cnt == 0) {
                        audioRecord.read(BufferStore, 0, BufferRecordSize); // 1.audioData == (100ms 단위) shoart형 100ms 만큼 크기 , 2.offsetInBytes 읽기 시작할 버퍼의 시작점 == 0 , 3.읽어드릴 사이즈 == Record버퍼의 사이즈 4.readmode blocking 모드 설정
                        cnt++;
                    }

                    audioRecord.read(BufferStore, 0, BufferRecordSize); // 1.audioData == (100ms 단위) shoart형 100ms 만큼 크기 , 2.offsetInBytes 읽기 시작할 버퍼의 시작점 == 0 , 3.읽어드릴 사이즈 == Record버퍼의 사이즈 4.readmode blocking 모드 설정
                    shortBuffer.put(BufferStore, 0, retBufferSize); // 데이터를 저장하는 put 메소드 // 위에서 녹음하고 저장하고
                    sendBuffer.position(0);
                    for (int i = 0; i < BufferStore.length; i = i + 100) {
                        Arrays.sort(BufferStore, i, i + 99);
                    }

                    cntt ++;
                    sendBuffer.put(BufferStore, 0, BufferRecordSize);
                    bdManager.setData(sendBuffer, retBufferSize); // [position=2560,limit=2560,capacity=2560] ,  2560
                }
                BufferStore[0] = 0;
                BufferStore[1] = 1;
                BufferStore[2] = 2;
                BufferStore[3] = 3;
                recordFlag = false;

                Arrays.fill(BoardManager.Buffer.array(), (short) 0);
                Arrays.fill(BoardManager.tempBuffer.array(), (short) 0);

            }

            if (playFlag) {
                Log.d("&&","&&BoardManager.playFlag");

                BoardManager.canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                shortBuffer.rewind(); // 버퍼 position 0으로
                BufferTrack = new short[BufferRecordSize]; // 버퍼 저장공간 == sohrt[1600] -> BufferRecordSize == BufferTrackSize
                BufferTrackSize = BufferTrack.length; //
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        SamplingRate,
                        AudioFormat.CHANNEL_OUT_MONO, // 스피커로 나가기 떄문에 OUT으로 해줘야됨 IN으로 할 시 에러뜸
                        AudioFormat.ENCODING_PCM_16BIT,
                        BufferTrack.length,
                        AudioTrack.MODE_STREAM); //// STREAM 버퍼를 계속 읽거나 쓰는 거 적은 양의 버퍼 필요 // STATIC 한번에 다 쓰고 한번에 다 읽고 많은 양의 버퍼 필요 STATIC 의 경우 audioTrack.reloadStaticData(); 필요 버퍼의 젤 앞부분 가리키도록 해줌

                audioTrack.play();
                while (shortBuffer.position() <= BufferShortSize - BufferTrackSize) { // 버퍼포지션 0,1,2,3..... <= (전체 버퍼 사이즈 - 버퍼트랙 사이즈) -> 버퍼 끝까지만 읽어야됨
                    shortBuffer.get(BufferTrack, 0, BufferTrackSize); // get() 데이터를 읽는 메소드 // 꺼내서 들려주고
                    audioTrack.write(BufferTrack, 0, BufferTrackSize); //
                }
                audioRecord.stop(); //녹음중지
                audioTrack.stop(); //재생중지
                playFlag = false;
            }
        }
    }
}

