package com.example.yxue.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;



/**
 * Created by yxue on 01.09.2017.
 */

public abstract class Bluetooth extends Activity  implements OnItemClickListener{

    public static void gethandler(Handler handler){
        mHandler = handler;
    }
    static Handler mHandler = new Handler();

    static ConnectedThread connectedThread;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    protected static final int SUCCESS_CONNECTED = 0;
    protected static final int MESSAGE_READ = 1;

    ListView listview;
    ArrayAdapter<String> listAdapter;
    static BluetoothAdapter btAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;
    IntentFilter filter;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        init();
        if (btAdapter==null){
            Toast.makeText(getApplicationContext(), "No BT detected", 0).show();
           finish();
        }else {
            if(!btAdapter.isEnabled()){
                turnOnBT();
            }
            getPairedDevices();
            startDisvovery();
        }
    }

    private void getPairedDevices(){
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();

    }
    private void startDisvovery(){
        devicesArray = btAdapter.getBondedDevices();
        if(devicesArray.size()>0){
            for(BluetoothDevice device:devicesArray){
                pairedDevices.add(device.getName());
            }
        }

    }
    private void turnOnBT(){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent,1);

    }

    private void init() {


        listview = (ListView) findViewById(R.id.listView);
        listview.setOnClickListener((View.OnClickListener) this);

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
        listview.setAdapter(listAdapter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        devices = new ArrayList<BluetoothDevice>();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    devices.add(device);
                    String s = "";
                    for (int a = 0; a < pairedDevices.size(); a++) {
                        if (device.getName().equals(pairedDevices.get(a))) {
                            s = "(Paired)";
                            break;
                        }
                    }

                    listAdapter.add(device.getName() + "" + "\n" + device.getAddress());
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if (btAdapter.getState() == btAdapter.STATE_OFF) {
                        turnOnBT();
                    }
                }

            }
        };
    }





    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp=device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch(IOException e){}

            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            mHandler.obtainMessage(SUCCESS_CONNECTED,mmSocket).sendToTarget();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer ;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    try{
                        sleep(30);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    buffer = new byte[1024];
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String income) {
            try {
                mmOutStream.write(income.getBytes());
                try{
                    Thread.sleep(20);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}
