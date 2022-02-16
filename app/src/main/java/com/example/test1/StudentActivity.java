package com.example.test1;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.provider.Settings;
import android.text.InputFilter;
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

public class StudentActivity<ActivityResultLauncher> extends AppCompatActivity {

    private Button On,Off,Visible,list,change,compare,input_n;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private ListView lv_p,lv_a;
    private TextView tv_search;
    private String stu_id = "";
    private static final int REQUEST_CODE_GPS = 1;
    final long lTimeToGiveUp_ms = System.currentTimeMillis() + 10000;
    String sNewName = "00000000";

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
        tv_search = (TextView)findViewById(R.id.tv_search);

        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(getApplicationContext(),"Your device doesn't support Bluetooth",
                    Toast.LENGTH_LONG).show();
        }

    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(Lreceiver);
        super.onDestroy();
    }
    // Create a BroadcastReceiver for ACTION_FOUND.
    private BroadcastReceiver Lreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("BTlist", " run"+intent.getAction());
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device.getName() + ": " + device.getAddress() + "\n");
                Log.i("BTlist", device.getName() + ": " + device.getAddress() + "\n");
                lv_a.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mDeviceList));
                tv_search.setText("Devides searched");
//                textView.setText(mDeviceList.toString());
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GPS) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "用户打开定位服务", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "用户关闭定位服务", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void listAvailable(View view){
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        // https://developer.android.google.cn/guide/components/intents-filters?hl=zh-cn#ExampleSend
        // https://developer.android.google.cn/reference/android/content/Intent?hl=zh-cn#resolveActivity(android.content.pm.PackageManager)
        // 判断是否有合适的应用能够处理该 Intent，并且可以安全调用 startActivity()。
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_GPS);
        } else {
            Toast.makeText(this, "该设备不支持位置服务", Toast.LENGTH_SHORT).show();
        }

        tv_search.setText("Searching...");
        Toast.makeText(getApplicationContext(),"Searching...",
                Toast.LENGTH_SHORT).show();
        Log.i("BTlist", "start list");
        permissionRequire();
        Log.i("BTlist", "start search");
        BA.startDiscovery();
        // Register for broadcasts when a device is discovered.
        IntentFilter Lfilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(Lreceiver, Lfilter);

    }

    private void permissionRequire() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        Log.i("BTlist", "permission get");
    }
    public void off(View view){
        BA.disable();
        Toast.makeText(getApplicationContext(),"Turned off" ,
                Toast.LENGTH_LONG).show();
    }

    public void change(View view){
        Log.i("BTchange", "start change");
        if (BA != null)
        {
            Log.i("BTchange", "BA exist");
            String sOldName = BA.getName();
            if (sOldName.equalsIgnoreCase(sNewName) == false)
            {
                final Handler myTimerHandler = new Handler();
                BA.enable();
                myTimerHandler.postDelayed(
                        new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Log.i("BTchange", "start run");
                                if (BA.isEnabled())
                                {
                                    BA.setName(sNewName);
                                    Log.i("BTchange", "start set");
                                    if (sNewName.equalsIgnoreCase(BA.getName()))
                                    {
                                        Log.i("BTchange", "Updated BT Name to " + BA.getName());
                                        Toast.makeText(getApplicationContext(),"new name" + BA.getName(),
                                                Toast.LENGTH_LONG).show();
//                                        BA.disable();
                                    }
                                }
                                Log.i("BTchange", "Waiting1...");
                                if ((sNewName.equalsIgnoreCase(BA.getName()) == false) && (System.currentTimeMillis() < lTimeToGiveUp_ms))
                                {
                                    Log.i("BTchange", "Waiting2...");
                                    myTimerHandler.postDelayed(this, 500);
                                    if (BA.isEnabled())
                                        Log.i("BTchange", "Update BT Name: waiting on BT Enable");
                                    else
                                        Log.i("BTchange", "Update BT Name: waiting for Name (" + sNewName + ") to set in");
                                }
                            }
                        } , 500);
            }
        }
    }

    public void compare(View view){
        Toast.makeText(getApplicationContext(),"your "+sNewName
                ,Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),"system "+BA.getName()
                ,Toast.LENGTH_SHORT).show();

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
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stu_id = input.getText().toString();
                sNewName = stu_id;
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