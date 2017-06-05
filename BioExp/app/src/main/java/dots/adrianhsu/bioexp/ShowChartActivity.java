package dots.adrianhsu.bioexp;

/**
 * Created by adrianhsu on 2017/4/4.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class ShowChartActivity extends AppCompatActivity {
    private static final String TAG = "bluetooth";
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private ConnectedThread mConnectedThread;
    private ArrayList<Integer> mydata;
    private int CURRENT_INDEX = 0;
    final private int BIAS_PARAMETER = 100;
    private LineChart mChart;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "98:D3:31:40:0E:48";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_chart);

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
        mydata = new ArrayList<>();
        mChart = (LineChart) findViewById(R.id.chart1);

        mChart.setHighlightPerTapEnabled(true);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(Color.LTGRAY);
        Description des = new Description();
        des.setText("ECG/EEG Line Chart");
        mChart.setDescription(des);
        mChart.setNoDataText("no data");

        LineData d = new LineData();
        d.setValueTextColor(Color.WHITE);
        mChart.setData(d);

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis x1 = mChart.getXAxis();
        x1.setTextColor(Color.WHITE);
        x1.setDrawGridLines(false);
        x1.setAvoidFirstLastClipping(true);

        YAxis y1 = mChart.getAxisLeft();
        y1.setTextColor(Color.WHITE);
//        y1.setAxisMaximum(1000f);
        y1.setDrawGridLines(true);

        YAxis y12 = mChart.getAxisRight();
        y12.setEnabled(false);
    }
    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.

//        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.e(TAG,"Connected");
        } catch (IOException e) {
            Log.e("",e.getMessage());
            try {
                Log.e(TAG,"trying fallback...");

                btSocket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                btSocket.connect();

                Log.e(TAG,"Connected");
            }
            catch (Exception e2) {
                Log.e(TAG, "Couldn't establish Bluetooth connection!");
            }
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean tmp = addEntry();
                        }
                    });
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "...In onPause()...");
        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }
    private boolean addEntry() {
        if(mydata.isEmpty()) {
            Log.d(TAG, "azzzz");
            return false;
        }
        LineData d = mChart.getData();
        if(d != null) {
            ILineDataSet set = d.getDataSetByIndex(0);
            if(set == null) {
                set = createSet();
                d.addDataSet(set);
            } else if (mydata.size() == CURRENT_INDEX) {
                return false;
            }
            int result = mydata.get(CURRENT_INDEX++);
            Log.d(TAG, "result is :" + result);
            if(result < BIAS_PARAMETER) { // bias
                return false;
            }
            d.addEntry(new Entry(set.getEntryCount(), result), 0);
            mChart.notifyDataSetChanged();
            Log.d(TAG, "CURRENT_INDEX: " + CURRENT_INDEX + ", Entry count: " + set.getEntryCount());
            mChart.setVisibleXRange(0, 200);
            mChart.moveViewToX(d.getEntryCount() - 201);
            return true;
        }
        return false;
    }
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "CHANNEL 0");
        set.setCubicIntensity(0.2f);
        set.setDrawCircles(false); //
        set.setDrawValues(false); //
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244,117,177));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(10f);
        return set;
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            Log.d(TAG, "1. in ConnectedThread");
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }

            Log.d(TAG, "2. in ConnectedThread");
            mmInStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int [] bytes = new int[2]; // bytes returned from read()
            Log.d(TAG, "run");

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    // bytes[0] = mmInStream.read(buffer);
                    // bytes[1] = mmInStream.read(buffer);
                    int len = mmInStream.read(buffer,0,2);
                    if (len < 2)
                        mmInStream.read(buffer,1,1);
                    bytes[0] = buffer[0];
                    bytes[1] = buffer[1];
                    int diff = bytes[0];
                    int mol = bytes[1];
                    Log.d(TAG, "len = " + len);
                    Log.d(TAG, "diff: " + diff);
                    Log.d(TAG, "mol: " + mol);
                    int result = diff * 128 + mol;
//                    boolean isvalid = false;
//                    try {
//                        result = bytes;
//                        isvalid = true;
//                    } catch(NumberFormatException nfe) {
//                        System.out.println("Could not parse " + nfe);
//                    }
                    if(result < 1024) {
                        mydata.add(result);
                    } else {
                    }
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage());
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}