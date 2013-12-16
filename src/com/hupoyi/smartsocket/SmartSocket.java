package com.hupoyi.smartsocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SmartSocket extends Activity {
	private static final String TAG = "SmartSocket";
	private static final boolean D = true;    // 开启调试模式,输出日志信息
	private static final int REQUEST_ENABLE_BT = 0;
	private static String address = "00:15:FF:F3:1E:EE"; // <==要连接的蓝牙设备MAC地址
	
	public static BluetoothService mBluethoothService = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	// Array adapter for the PairedDevices
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    // UI 
    private TextView info = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		//初始化蓝牙相关
		InitBlueTooth();
		//初始化UI
		InitUI();
	}
	
	 @Override
	 public void onStart() {
	    super.onStart();
	    if(D) Log.e(TAG, "++ ON START ++");
	 };

	    
	 @Override
	 public synchronized void onResume() {
	    super.onResume();
	    if(D) Log.e(TAG, "+ ON RESUME +");
	    if (mBluethoothService == null) { Log.d(TAG, "mBluethoothService is null, Please connect again!"); }
	    else { Log.d(TAG, "mBluethoothService is not null"); }
	 };
	    
	 @Override
	 public synchronized void onPause() {
	    super.onPause();
	    if(D) Log.e(TAG, "- ON PAUSE -");
	 };

	 @Override
	 public void onStop() {
	    super.onStop();
	    if(D) Log.e(TAG, "-- ON STOP --");
	 };

	 @Override
	 public void onDestroy() {
	    super.onDestroy();
	    mBluethoothService.stop();
	    if(D) Log.e(TAG, "-- ON DESTORY --");
	 };
	    
	    
	/* 初始化蓝牙 */
	private void InitBlueTooth() {
        /* 获取蓝牙适配器 */
	    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
			Toast.makeText(getApplication(), "当前设备不支持蓝牙功能", Toast.LENGTH_SHORT).show();
		    this.finish();
		}
		/* 检查蓝牙是否开启 */
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		/* 获取已经匹配的设备集 */
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView
		    	//mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		    }
		}
		/* found device */
		/* 搜索蓝牙设备广播消息接收器  */
		// Create a BroadcastReceiver for ACTION_FOUND
		final BroadcastReceiver mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
			    String action = intent.getAction();
			    // When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			        // Get the BluetoothDevice object from the Intent
			        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			        // Add the name and address to an array adapter to show in a ListView
				    //mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			    }
			}
		};
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		//registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
	    mBluethoothService = new BluetoothService(this, mHandler);
	}
	
    private void InitUI() {
    	info = (TextView) super.findViewById(R.id.info);
    	Button link = (Button) super.findViewById(R.id.link);
    	link.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBluethoothService.connect(mBluetoothAdapter.getRemoteDevice(address));
			}
    		
    	});
    	Button btn = (Button) super.findViewById(R.id.btn);
    	btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBluethoothService.write("Hello HuPoYi!");
			}
    		
    	});
    	Button next = (Button) super.findViewById(R.id.next);
    	next.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SmartSocket.this, SecondActivity.class);
				SmartSocket.this.startActivity(intent);  // 启动其它Activity
				overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
			}
    		
    	});
    }
    
	 // The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BluetoothService.MESSAGE_READ:
                //byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                //String readMessage = new String(readBuf, 0, msg.arg1);
            	String readMessage = (String) msg.obj;
                info.setText(readMessage);
                break;
            }
        }
    };

}
