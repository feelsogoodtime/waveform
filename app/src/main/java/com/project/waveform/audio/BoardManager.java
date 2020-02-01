package com.project.waveform.audio;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

class BoardManager extends SurfaceView implements SurfaceHolder.Callback {
    int ScreenWidth, ScreenHeight, BoardWidth, BoardHeight;
    int BoardStartX, BoardStartY, BoardMiddleWidth,
            BoardMiddleHeight, BoardEndX, BoardEndY;

    static int cnt, cntt;
    static int datacnt;
    static int seekTime = 1;

    static Canvas canvas;

    double RatioX, RatioY;
    int MaxHeight;
    //int SamplingRate, TimeDiv;
    static int SamplingRate = 16000;
    static int TimeDiv = 1000;
    //AudioRecord로 부터 받은 데이터의 수
    int DataLength;
    static ShortBuffer Buffer, tempBuffer, ReadBuffer;
    SurfaceHolder mHolder;

    RecordManager recordManager;
    boolean isData = true;

    List<ShortBuffer> arr = new ArrayList<ShortBuffer>();


    public BoardManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        Log.d("&&","&&Boardmanager생성자");
    }

    public BoardManager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        Log.d("&&","&&Boardmanager생성자");
    }

    public BoardManager(Context context) {
        super(context);
        init();
        Log.d("&&","&&Boardmanager생성자");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("&&","&&BoardManagersurfaceCreated");
        mHolder = holder;
        getScreenInfo();
        canvas = holder.lockCanvas(null);
        Bitmap bgImage = Bitmap.createBitmap(BoardWidth, BoardHeight, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bgImage);
        canvas.drawColor(Color.WHITE);

        holder.unlockCanvasAndPost(canvas);
        //drawBoard();
        //holder.unlockCanvasAndPost(canvas);
        recordManager = new RecordManager(this);
        //recordManager.setBoardManager(this);
        recordManager.start();
        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    void init() {
        Log.d("&&","&&BoardManager.init");

        SurfaceHolder mHolder = getHolder();
        mHolder.addCallback(this);
        SamplingRate = 16000;

        //화면에 표시할 시간
        //표시할 최대값
        MaxHeight = 32767;
    }

    void start() {
        Log.d("&&","&&BoardManager.start");

        recordManager.onStart();
    }

    void Pause() {
        recordManager.onPause();
    }

    void Stop() {
        recordManager.onStop();
    }

    void getScreenInfo() {
        //화면의 크기를 얻어옴
        ScreenWidth = getWidth();
        ScreenHeight = getHeight();
        Log.d("&&","&&BoardManager.getScreenInfo");

        //박스의 크기를 화면의 90%크기로 설정
        BoardWidth = (int) (ScreenWidth * 0.9);
        BoardHeight = (int) (ScreenHeight * 0.9);

        //박스의 그릴 지점 설정
        BoardStartX = (ScreenWidth - BoardWidth) / 2;
        BoardStartY = (ScreenHeight - BoardHeight) / 2;
        BoardEndX = BoardStartX + BoardWidth;
        BoardEndY = BoardStartY + BoardHeight + 2;

        //박스의 중앙선
        BoardMiddleHeight = BoardHeight / 2;
        BoardMiddleWidth = BoardWidth / 2;

        //표시 배율
        RatioY = (BoardHeight) / (MaxHeight * 2.0f); // (6 - VRange)  <- 화면 주파수 높이
        RatioX = (BoardWidth) * 1000 / (double) (TimeDiv); //

    }

    void drawBoard() {
        Paint paint = new Paint();
        Log.d("&&","&&BoardManager.drawBoard");

        //배경화면 힌색으로
        paint.setColor(Color.WHITE);
        canvas.drawRect(1, 1, ScreenWidth, ScreenHeight, paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        //보드 외곽선
        canvas.drawRect(BoardStartX - 1, BoardStartY, BoardEndX + 2, BoardEndY, paint);

        //보드 십자선
        paint.setStrokeWidth(1);
        canvas.drawLine(BoardStartX + BoardMiddleWidth, BoardStartY,
                BoardStartX + BoardMiddleWidth, BoardEndY, paint);
        canvas.drawLine(BoardStartX, BoardStartY + BoardMiddleHeight
                , BoardEndX, BoardStartY + BoardMiddleHeight, paint);



    }

    void setData(ShortBuffer readBuffer, int dataLength) { // [position=2560,limit=2560,capacity=2560] ,  2560
        this.ReadBuffer = readBuffer;  // [position=2560,limit=2560,capacity=2560]
        this.DataLength = dataLength; // ,  2560invalidatenew
        Log.d("&&","&&BoardManager.setData");

        isData = true;

        if(cnt ==6) {
//            invalidate();
        }

        if (cnt == 0) {
            Buffer = ShortBuffer.allocate(SamplingRate * 10);
            tempBuffer = ShortBuffer.allocate(SamplingRate * 10);
            this.TimeDiv = TimeDiv * seekTime;

            cnt++;
        }
        this.TimeDiv = TimeDiv * seekTime;
        getScreenInfo();
        cnt++;
        canvas = mHolder.lockCanvas(null);
        drawBoard();
        drawData();
        mHolder.unlockCanvasAndPost(canvas);
        isData = true;
    }

    void drawData() {
        Log.d("&&","&&BoardManager.drawData");

        double data, Stime, Ttime;
        int x, y,
                xline=0, yline=0, i;
        //AudioRecord로 부터 입력된 데이터가 있을 때만 그림
        if (isData == true) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(3);
            tempBuffer.position(0);
            tempBuffer.put(Buffer.array(), 0, SamplingRate * seekTime);//버퍼 복사 32000 [32000,0,15999]           // 16000 [ 16000, 0, 159999]

            Buffer.position(0);

            Buffer.put(ReadBuffer.array(), 0, DataLength); //32000 [position=2560,limit=2560,capacity=2560] , 0 , 2559      // 16000 [ ,2560, 0 2559 ]
            Buffer.put(tempBuffer.array(), 0, SamplingRate * seekTime - DataLength); // 32000 [32000,0, 13399]      // 16000 [16000 , 0 , 16000-2599-1]
            //샘플링 한주기의 시간을 구함
            Stime = 1.0f / SamplingRate; //0.0000625;

            RatioX = (BoardWidth) * 1000 / (double) (1000*seekTime); //

            //데이터를 읽어와서 화면에 출력
            datacnt++;
            Buffer.position(0);
            for (i = 0; i <= (int) SamplingRate*seekTime; i = i + 99) {
                x = (int) ((i + 1) * Stime * RatioX) + BoardStartX + 1; // RatioX -> 379 // 32000 // 151.6 // < 80000 // 152  >
                data = -RatioY * Buffer.get(i);
                y = BoardMiddleHeight + (int) data + BoardStartY;

                if (i % 2 != 0) {
                    i++;
                    canvas.drawLine(x,y,xline,yline,paint);
                }
                xline = x;
                yline = y;
            }
            isData = false;
        }
    }
}