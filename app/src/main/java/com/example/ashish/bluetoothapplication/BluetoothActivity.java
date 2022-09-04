package com.example.ashish.bluetoothapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class BluetoothActivity extends AppCompatActivity {
    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private static final boolean D = true;


    private TextView mWelcomeText;

    // State variables
    private boolean paused = false;
    private boolean connected = false;


    // controlled by user settings
    private boolean recordingEnabled;
    private String defaultEmail;
    private boolean mockDevicesEnabled;

    private final StringBuilder recording = new StringBuilder();


    // Toolbar
    private ImageButton mToolbarConnectButton;
    private ImageButton mToolbarDisconnectButton;
    private ImageButton mToolbarPauseButton;
    private ImageButton mToolbarPlayButton;

    // Layout Views
    private TextView mStatusView;
    private ListView mConversationView;
    private EditText mOutEditText;
    private View mSendTextContainer;

    private String deviceName;

    private ArrayAdapter<String> mConversationArrayAdapter;
    private DeviceConnector mDeviceConnector = new NullDeviceConnector();

    private Button mScan;
    private BluetoothAdapter mBluetoothAdapter;
    private UUID applicationUUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluettoth_activty);
        mScan = (Button) findViewById(R.id.button);
        mScan.setOnClickListener(mView -> {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Toast.makeText(BluetoothActivity.this, "Message1", Toast.LENGTH_SHORT).show();
            } else {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivityForResult(enableBtIntent,
                            REQUEST_ENABLE_BT);
                } else {
                    ListPairedDevices();
                    Intent connectIntent = new Intent(BluetoothActivity.this,
                            DeviceListActivity.class);
                    startActivityForResult(connectIntent,
                            REQUEST_CONNECT_DEVICE);
                }
            }
        });
    }

    private final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageHandler.Constants.MSG_CONNECTED:
                    mWelcomeText.setVisibility(View.GONE);
                    connected = true;
                    mStatusView.setText(formatStatusMessage(R.string.btstatus_connected_to_fmt, msg.obj));
                    onBluetoothStateChanged();
                    recording.setLength(0);
                    deviceName = msg.obj.toString();
                    break;
                case MessageHandler.Constants.MSG_CONNECTING:
                    connected = false;
                    mStatusView.setText(formatStatusMessage(R.string.btstatus_connecting_to_fmt, msg.obj));
                    onBluetoothStateChanged();
                    break;
                case MessageHandler.Constants.MSG_NOT_CONNECTED:
                case MessageHandler.Constants.MSG_CONNECTION_FAILED:
                case MessageHandler.Constants.MSG_CONNECTION_LOST:
                    connected = false;
                    mStatusView.setText(R.string.btstatus_not_connected);
                    onBluetoothStateChanged();
                    break;
                case MessageHandler.Constants.MSG_BYTES_WRITTEN:
                    String written = new String((byte[]) msg.obj);
                    mConversationArrayAdapter.add(">>> " + written);
                    Log.i(TAG, "written = '" + written + "'");
                    break;
                case MessageHandler.Constants.MSG_LINE_READ:
                    if (paused) break;
                    String line = (String) msg.obj;
                    if (D) Log.d(TAG, line);
                    mConversationArrayAdapter.add(line);
                    if (recordingEnabled) {
                        recording.append(line).append("\n");
                    }
                    break;
                default:
                    Log.d(TAG, "Unkown message: " + msg.what + ", arg1= " + msg.arg1 + ", arg2= " + msg.arg2);
            }
        }

        private String formatStatusMessage(int formatResId, Object deviceName) {
            return getString(formatResId, (String) deviceName);
        }
    };

    private void ListPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice mDevice : mPairedDevices) {
                Log.v(TAG, "PairedDevices: " + mDevice.getName() + "  "
                        + mDevice.getAddress());
            }
        }
    }
    @Override
    public void onActivityResult(int mRequestCode, int mResultCode,
                                 Intent mDataIntent) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent);

        switch (mRequestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (mResultCode == Activity.RESULT_OK) {
                    Bundle mExtra = mDataIntent.getExtras();
                    String mDeviceAddress = mExtra.getString("DeviceAddress");
                    Log.v(TAG, "Coming incoming address " + mDeviceAddress);
                    mDeviceConnector =DeviceListActivity.createDeviceConnector(mDataIntent, getAssets());
                    if (mDeviceConnector != null) {
                        mDeviceConnector.connect();
                    } else {
                        Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
                    }
                }
                break;

            case REQUEST_ENABLE_BT:
                if (mResultCode == Activity.RESULT_OK) {
                    ListPairedDevices();
                    Intent connectIntent = new Intent(BluetoothActivity.this,
                            DeviceListActivity.class);
                    startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(BluetoothActivity.this, "Message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    private void onBluetoothStateChanged() {
        if (connected) {
            mToolbarConnectButton.setVisibility(View.GONE);
            mToolbarDisconnectButton.setVisibility(View.VISIBLE);
            mSendTextContainer.setVisibility(View.VISIBLE);
            mWelcomeText.setVisibility(View.GONE);
            mConversationView.setVisibility(View.VISIBLE);
        } else {
            mToolbarConnectButton.setVisibility(View.VISIBLE);
            mToolbarDisconnectButton.setVisibility(View.GONE);
            mSendTextContainer.setVisibility(View.GONE);
        }
        paused = false;
        onPausedStateChanged();
    }

    private void onPausedStateChanged() {
        if (connected) {
            if (paused) {
                mToolbarPlayButton.setVisibility(View.VISIBLE);
                mToolbarPauseButton.setVisibility(View.GONE);
            } else {
                mToolbarPlayButton.setVisibility(View.GONE);
                mToolbarPauseButton.setVisibility(View.VISIBLE);
            }
        } else {
            mToolbarPlayButton.setVisibility(View.GONE);
            mToolbarPauseButton.setVisibility(View.GONE);
        }
    }




}
