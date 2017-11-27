package com.example.taek.removepaireddevicesample;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public final static int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public ArrayList<String> items = new ArrayList<>();
    public ArrayAdapter<String> adapter;
    public ListView listView;
    public BluetoothManager bluetoothManager;
    public BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 리스트뷰 관련
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, items);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        // BLE 관련 Permission 부여
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect BLE devices.");
                builder.setPositiveButton("Ok", null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M | Build.VERSION_CODES.N | Build.VERSION_CODES.N_MR1 | Build.VERSION_CODES.O)
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            setListItem(device.getAddress());
        }
        notifyAddedList();
    }

    public void setListItem(String str) {
        items.add(str);
    }

    public void notifyAddedList() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 페어링 디바이스 해제
            case R.id.remove_paired_device: {
                int position = listView.getCheckedItemPosition();
                if (position != ListView.INVALID_POSITION) {
                    // 페어링 해제
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice bluetoothDevice : pairedDevices) {
                        if (bluetoothDevice.getAddress().contains(items.get(position))) {
                            try {
                                Method method = bluetoothDevice.getClass().getMethod("removeBond", (Class[]) null);
                                method.invoke(bluetoothDevice, (Object[]) null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // 리스트뷰에서 삭제
                    items.remove(position);
                    listView.clearChoices();
                    adapter.notifyDataSetChanged();
                }
                break;
            }

            // 페어링 목록 가져오기/새로고침
            case R.id.get_paired_devices_list: {
                items.clear();
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : pairedDevices) {
                    setListItem(device.getAddress());
                }
                notifyAddedList();
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permission[], int[] grantResults){
        switch (requestCode){
            case PERMISSION_REQUEST_COARSE_LOCATION:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("permission", "coarse location permission granted");
                }else{
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, " +
                            "this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton("Ok", null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }
}
