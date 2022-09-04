package com.example.ashish.bluetoothapplication;

/**
 * Created by hp on 12/23/2016.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.AssetManager;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

import static android.Manifest.permission.BLUETOOTH;


public class DeviceListActivity extends Activity {
    protected static final String TAG = "TAG";
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle mSavedInstanceState) {
        super.onCreate(mSavedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        setResult(Activity.RESULT_CANCELED);
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        ListView mPairedListView = (ListView) findViewById(R.id.paired_devices);
        mPairedListView.setAdapter(mPairedDevicesArrayAdapter);
        mPairedListView.setOnItemClickListener(mDeviceClickListener);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();

        if (mPairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice mDevice : mPairedDevices) {
                mPairedDevicesArrayAdapter.add(mDevice.getName() + "\n" + mDevice.getAddress());
            }
        } else {
            String mNoDevices = "None Paired";//getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(mNoDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> mAdapterView, View mView, int mPosition, long mLong) {

            try {


                mBluetoothAdapter.cancelDiscovery();
                String mDeviceInfo = ((TextView) mView).getText().toString();
                String mDeviceAddress = mDeviceInfo.substring(mDeviceInfo.length() - 17);
                Log.v(TAG, "Device_Address " + mDeviceAddress);

                Bundle mBundle = new Bundle();
                mBundle.putString("DeviceAddress", mDeviceAddress);
                Intent mBackIntent = new Intent();
                mBackIntent.putExtras(mBundle);
                setResult(Activity.RESULT_OK, mBackIntent);
                finish();
            } catch (Exception ex) {

            }
        }
    };
    public enum Message {
        DEVICE_CONNECTOR_TYPE,
        BLUETOOTH_ADDRESS,
        MOCK_TYPE,
        MOCK_FILENAME,
    }
    public enum ConnectorType {
        BLUETOOTH,
        MOCK
    }

    public enum MockType {
        TEXT,
        BINARY
    }
    public static DeviceConnector createDeviceConnector(Intent data, AssetManager assetManager) {


        switch (BLUETOOTH) {
            case BLUETOOTH:

                String address = data.getStringExtra("DeviceAddress");
                return new BluetoothDeviceConnector(new MessageHandler() {
                    @Override
                    public void sendLineRead(String line) {

                    }

                    @Override
                    public void sendBytesWritten(byte[] bytes) {

                    }

                    @Override
                    public void sendConnectingTo(String deviceName) {

                    }

                    @Override
                    public void sendConnectedTo(String deviceName) {

                    }

                    @Override
                    public void sendNotConnected() {

                    }

                    @Override
                    public void sendConnectionFailed() {

                    }

                    @Override
                    public void sendConnectionLost() {

                    }
                }, address);
            default:
                return null;
        }
    }

}
