package org.nosemaj.toronto;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BeaconScanActivity extends Activity {
    public static final String TAG = BeaconScanActivity.class.getName();

    private static final int SCAN_INTERVAL_MS = 250;
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private ArrayList<ScanFilter> mScanFilters = new ArrayList<ScanFilter>();
    private ScanSettings mScanSettings;
    private boolean mScanning = false;
    private Handler mScanHandler = new Handler();

    private TextView mTextView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTextView = (TextView)findViewById(R.id.text_view);
    }

    protected void onResume() {
        super.onResume();

        getBluetoothHandles();
        enableBluetooth();

        if (mBluetoothAdapter.isEnabled()) {
            beginScanning();
        }
    }

    private void getBluetoothHandles() {
        mBluetoothManager =
            (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    private void enableBluetooth() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void beginScanning() {
        mScanSettings = getScanSettings();
        mScanHandler.post(mScanRunnable);
    }

    private ScanSettings getScanSettings() {
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);

        return scanSettingsBuilder.build();
    }

    private Runnable mScanRunnable = new Runnable() {
        @Override
        public void run() {
            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

            if (mScanning) {
                scanner.stopScan(mScanCallback);
            } else {
                scanner.startScan(null, mScanSettings, mScanCallback);
            }

            mScanning = !mScanning;

            mScanHandler.postDelayed(this, SCAN_INTERVAL_MS);
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            logScanResult(result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            logFailure(errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult r : results) {
                logScanResult(r);
            }
        }
    };

    private void logScanResult(ScanResult result) {
        final String deviceString = "device = " + result.getDevice().toString();
        final String scanRecordString = "scanRecord = " + result.getScanRecord();
        final String rssiString = "rssi = " + result.getRssi();

        mTextView.append(deviceString);
        mTextView.append(scanRecordString);
        mTextView.append(rssiString);

        Log.d(TAG, deviceString);
        Log.d(TAG, scanRecordString);
        Log.d(TAG, rssiString);
    }

    private void logFailure(int errorCode) {
        mTextView.append("" + errorCode);
        Log.d(TAG, "scan failure error code =" + errorCode);
    }
}
