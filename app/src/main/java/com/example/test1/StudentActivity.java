package com.example.test1;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StudentActivity extends AppCompatActivity {

    private Button On,Off,Visible,list,change,compare,input_n;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private ListView lv_p,lv_a;
    private String stu_id = "";
    final int lTimeToGiveUp_ms = 999;
    String sNewName = "YourNewName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        On = (Button)findViewById(R.id.button_on);
        Off = (Button)findViewById(R.id.button_off);
        Visible = (Button)findViewById(R.id.button_get_vis);
        list = (Button)findViewById(R.id.button_list);
        change = (Button)findViewById(R.id.button_change);
        compare = (Button)findViewById(R.id.button_compare);
        input_n = (Button)findViewById(R.id.button_input);

        lv_p = (ListView)findViewById(R.id.listView_p);
        lv_a = (ListView)findViewById(R.id.listView_a);

        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(getApplicationContext(),"Your device doesn't support Bluetooth",
                    Toast.LENGTH_LONG).show();
        }
        // Register for broadcasts when a device is discovered.
        IntentFilter Lfilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(Lreceiver, Lfilter);

    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(Lreceiver);
        super.onDestroy();
    }
    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver Lreceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
//                mDeviceList.add(device.getName() + "\n" + device.getAddress());
                mDeviceList.add(device.getName());
                Log.i("BT", device.getName() + "\n" + device.getAddress());
                lv_a.setAdapter(new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, mDeviceList));
            }
        }
    };
    public void on(View view){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(),"Turned on"
                    ,Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Already on",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void listConnected(View view){
        pairedDevices = BA.getBondedDevices();
        BA.cancelDiscovery();
        ArrayList list = new ArrayList();
        for(BluetoothDevice bt : pairedDevices)
            list.add(bt.getName());

        Toast.makeText(getApplicationContext(),"Showing Paired Devices",
                Toast.LENGTH_SHORT).show();
        final ArrayAdapter adapter = new ArrayAdapter
                (this,android.R.layout.simple_list_item_1, list);
        lv_p.setAdapter(adapter);
    }
/**
        * 蓝牙广播过滤器
 * 蓝牙状态改变
 * 找到设备
 * 搜索完成
 * 开始扫描
 * 状态改变
 *
         * @return
         */
    public IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙状态改变的广播
        filter.addAction(BluetoothDevice.ACTION_FOUND);//找到设备的广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//搜索完成的广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始扫描的广播
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        return filter;
    }



    public void off(View view){
        BA.disable();
        Toast.makeText(getApplicationContext(),"Turned off" ,
                Toast.LENGTH_LONG).show();
    }

    public void change(View view){
        Toast.makeText(getApplicationContext(),"before now: " + BA.getName()
                ,Toast.LENGTH_LONG).show();
        if(BA.getState() == BluetoothAdapter.STATE_ON){
            BA.setName(sNewName);
            Toast.makeText(getApplicationContext(),"tried"
                    ,Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getApplicationContext(),"then now: " + BA.getName()
                ,Toast.LENGTH_LONG).show();
    }

    public void compare(View view){
        if (sNewName.equalsIgnoreCase(BA.getName()))
        {
            Toast.makeText(getApplicationContext(),"same"
                    ,Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getApplicationContext(), "not"
                    , Toast.LENGTH_LONG).show();
        }
    }

    public void inputName(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your Student ID");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PHONETIC);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stu_id = input.getText().toString();
                TextView tv_stu_id = (TextView)findViewById(R.id.stu_id);
                tv_stu_id.setText("Your ID: "+stu_id);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        builder.show();




    }

    public void visible(View view){
        Intent getVisible = new Intent(BluetoothAdapter.
                ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);

    }

}