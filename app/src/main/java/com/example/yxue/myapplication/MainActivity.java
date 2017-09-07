package com.example.yxue.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;




public class MainActivity extends Activity implements View.OnClickListener {

    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Bluetooth_1.SUCCESS_CONNECT:
                    Bluetooth_1.connectedThread = new Bluetooth_1.ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_SHORT).show();
                    String s = "Successfully connected";
                    Bluetooth_1.connectedThread.start();
                    break;
                case Bluetooth_1.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String strIncom = new String (readBuf,0,5);
                    if (strIncom.indexOf('s')==0 && strIncom.indexOf('.')==2){
                    strIncom = strIncom.replace("s","");
                        if(isFloatNumber(strIncom)){
                            line_series.appendData(new DataPoint(graph2LastXValue,Double.parseDouble(strIncom)),true,40);

                            if(graph2LastXValue >= Xview && Lock== true){
                                line_series.resetData(new DataPoint[]{});
                                graph2LastXValue =0;
                            }else graph2LastXValue +=0.1;

                        }

                }
                break;
            }
        }
        public boolean isFloatNumber(String num){
            try{
                Double.parseDouble(num);
            }catch (NumberFormatException nfe){
                return false;
            }
            return true;
        }
    };


    Button bConnect,bDisconnet,bXminus, bXplus;
    ToggleButton tbLock, tbScroll, tbStream;
    static boolean Lock, AutoScrollX, Stream;

    //graph init
    static LinearLayout GraphView1;
    static GraphView line_graph;
    private LineGraphSeries<DataPoint> line_series;


    private static double graph2LastXValue =0;
    private static int Xview =10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        LinearLayout background = (LinearLayout)findViewById(R.id.bg);
        background.setBackgroundColor(Color.BLACK);
        init();
        Buttoninit();
    }
    void init(){

        GraphView line_graph = (GraphView) findViewById(R.id.graph);
        line_series = new LineGraphSeries<DataPoint>();

        line_graph.addSeries(line_series);
        line_series.setThickness(10);
        line_series.setColor(Color.YELLOW);

        line_graph.getViewport().setScalable(true);
        line_graph.getViewport().setScrollable(true);
        line_graph.getViewport().setScalableY(true);
        line_graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        line_graph.getLegendRenderer().setVisible(true);
        line_graph.getViewport().setYAxisBoundsManual(true);

    }

    void Buttoninit(){
        bConnect = (Button)findViewById(R.id.bConnect);
        bConnect.setOnClickListener(this);
        bDisconnet = (Button)findViewById(R.id.bDisconnect);
        bDisconnet.setOnClickListener(this);
        bXminus = (Button)findViewById(R.id.bXminus);
        bXminus.setOnClickListener(this);
        bXplus = (Button)findViewById(R.id.bXplus);
        bXplus.setOnClickListener(this);


        tbLock = (ToggleButton)findViewById(R.id.tbLock);
        tbLock.setOnClickListener(this);
        tbScroll = (ToggleButton)findViewById(R.id.tbScroll);
        tbScroll.setOnClickListener(this);
        tbStream = (ToggleButton)findViewById(R.id.tbStream);
        tbStream.setOnClickListener(this);

        Lock= true;
        AutoScrollX = true;
        Stream = false;

    }

    public void onClick(View v){
        //TODO Auto-generated method stub
        switch (v.getId()){
          case R.id.bConnect:
                startActivity(new Intent("android.intent.action.BT1"));
                break;
            case R.id.bDisconnect:
                Bluetooth_1.disconnect();
                break;
            case R.id.bXminus:

                break;
            case R.id.bXplus:

                break;
            case R.id.tbLock:
                if(tbLock.isChecked()){
                    Lock = true;
                }else{
                    Lock = false;
                }
                break;
            case R.id.tbScroll:
                if(tbScroll.isChecked()){
                    AutoScrollX = true;
                }else{
                    AutoScrollX=false;
                }
                break;
            case R.id.tbStream:
                if(tbStream.isChecked()){
                    if(Bluetooth_1.connectedThread!=null) Bluetooth_1.connectedThread.write("E");
                }else {
                    if(Bluetooth_1.connectedThread!= null) Bluetooth_1.connectedThread.write("Q");
                }
                break;
        }
    }
}
