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

    @Override
    public void onBackPressed(){
        if(Bluetooth_2.connectedThread != null) Bluetooth_2.connectedThread.write("Q");
        super.onBackPressed();
    }

    static boolean Lock, AutoScrollX, Stream;
    //graph init
    static LinearLayout GraphView1;
    static GraphView line_graph;
    //graph value
    private static double graph2LastXValue =0;
    private static int Xview =10;
    Button bConnect,bDisconnet,bXminus, bXplus;
    ToggleButton tbLock, tbScroll, tbStream;
    private LineGraphSeries<DataPoint> line_series;
    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Bluetooth_2.SUCCESS_CONNECT:
                    Bluetooth_2.connectedThread = new Bluetooth_2.ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_SHORT).show();
                    String s = "Successfully connected";
                    Bluetooth_2.connectedThread.start();
                    break;
                case Bluetooth_2.MESSAGE_READ:
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

                            //refresh
                            line_graph.removeSeries(line_series);
                            line_graph.addSeries(line_series);
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

        Bluetooth_2.gethandler(mHandler);
        GraphView1 = (LinearLayout) findViewById(R.id.Graph);


        line_series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 0)});

        line_series.setTitle("Signal");
        line_series.setThickness(10);
        line_series.setColor(Color.YELLOW);


        line_graph = (GraphView) findViewById(R.id.graph_view);

        line_graph.getViewport().setScalable(true);
        line_graph.setTitle("Graph");
        line_graph.setTitleColor(Color.YELLOW);
        line_graph.getViewport().setMaxY(5);
        line_graph.getViewport().setBackgroundColor(Color.BLACK);
        line_graph.getViewport().setMinY(0);
        line_graph.getViewport().setMaxX(Xview);
        line_graph.getViewport().setMinX(0);
        line_graph.getViewport().setScrollable(true);
        line_graph.getViewport().setScalableY(true);
        line_graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        line_graph.getViewport().setXAxisBoundsManual(false );
        line_graph.getViewport().setYAxisBoundsManual(false);
        line_graph.getLegendRenderer().setVisible(true);
        line_graph.addSeries(line_series);


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

        switch (v.getId()){
          case R.id.bConnect:
              Intent intent = new Intent(MainActivity.this,Bluetooth_2.class);
              startActivity(intent);

                break;
            case R.id.bDisconnect:
                Bluetooth_2.disconnect();
                break;
            case R.id.bXminus:
                if(Xview>1) Xview--;
                break;
            case R.id.bXplus:
                if (Xview<30) Xview++;
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
                    if(Bluetooth_2.connectedThread!=null) Bluetooth_2.connectedThread.write("E");
                }else {
                    if(Bluetooth_2.connectedThread!= null) Bluetooth_2.connectedThread.write("Q");
                }
                break;
        }
    }
}
