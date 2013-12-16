package com.hupoyi.smartsocket;
/*
 * �Զ�������������  
 * ����: shangtao@HuPoYi.com 
 * ����: 2013.12.14 
 * ͨ�����������õ� (BluetoothAdapter)mBluetoothAdapter�����������Լ�(BluetoothDevice)deviceԶ���豸����
 * �õ�(BluetoothSocket)mmSocket��������
 * Ȼ���һ�����µ����̵߳õ�(InputStream)mmInStream��������(OutputStream)mmOutStream���������
 * ʹ����Ϣ�����ϱ���UI�����Լ�������������
 * 
 * ʹ�÷���: Step1: BluetoothService mBluetoothService = new BluetoothService(this, mHandler);
 *        Step2: mBluetoothService.connect(device);   // ���ӵ�Զ�������豸 �����������߳����ڶ�ȡ������Ϣ ͨ��mHandler������Ϣ����
 *         
 *        ���÷���1: public void write(byte[] out);      
 *           ����:  ��Զ�������豸����һ���ֽ�
 *            
 *        ����ʵ��:  String message = "hello android!";  
 *                 byte[] send = message.getBytes();
 *                 mBluetoothService.write(send);    
 *        
 *        ���÷���2: public void write(String message);  
 *          ����:  ֱ�Ӹ�Զ���豸�����ַ���
 *        ����ʵ��:  mBluetoothService.write("hello android!");                                          
 * */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;


public class BluetoothService { 
	private static final String TAG = "BlueToothService"; 
	// Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_INSECURE =
        UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    // Member fields
    private BluetoothAdapter mAdapter = null;
    private Handler mHandler = null;
    private ConnectThread mConnectThread = null;
    private ConnectedThread mConnectedThread = null;
    
/*=========================================BluetoothService���캯�� begin==============================*/   
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothService(Context context, Handler handler) {
    	mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
    }
/*=========================================BluetoothService���캯�� end==============================*/
    
    
/*=========================================start���� begin==========================================*/       
    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
         Log.d(TAG, "BluetoothService.start()");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
    }
/*=========================================start���� end==========================================*/
    
    
/*=========================================connect���� begin==========================================*/
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device.getName()+ ":" + device.getAddress());

        // Cancel any thread attempting to make a connection
        
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }
/*=========================================connect���� end==========================================*/  
 

/*=========================================stop���� begin===========================================*/  
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "BluetoothService.stop()");

        if (mConnectThread != null) { mConnectThread.cancel(); mConnectThread = null; }

        if (mConnectedThread != null) { mConnectedThread.cancel(); mConnectedThread = null; }
    }
/*=========================================stop���� end=============================================*/

    
/*=========================================write byte���� begin==========================================*/
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
        	if (mConnectThread == null) { Log.d(TAG, "mConnectThread is null"); }
            if (mConnectedThread == null) {Log.d(TAG, "mConnectedThread is null,write failed"); return;}
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
/*=========================================write byte���� end=============================================*/
 

/*=========================================write string���� begin==========================================*/
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(String message) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
        	if (mConnectThread == null) { Log.d(TAG, "mConnectThread is null"); }
            if (mConnectedThread == null) {Log.d(TAG, "mConnectedThread is null,write failed"); return;}
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(message.getBytes());
    }
/*=========================================write string���� end=============================================*/    
    
    
/*=========================================ConnectThread �ڲ���start=================================*/
/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
/* �������߳� */
private class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    
    /* �������߳� */
    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
 
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            Log.d(TAG, "device.createRfcommSocketToServiceRecord OK");
        } catch (IOException e) { Log.e(TAG, "device.createRfcommSocketToServiceRecord failed", e); }
        mmSocket = tmp;
    }
    /* �����������߳�,������ɺ�������ݹ����߳� */
    public void run() {
        // Cancel discovery because it will slow down the connection
    	mAdapter.cancelDiscovery();
 
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
            Log.d(TAG, " mmSocket.connect() OK");
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            Log.d(TAG, "Unable to connect");
            try {
                mmSocket.close();
            } catch (IOException closeException) {
            	 Log.e(TAG, "unable to close socket during connection failure", closeException);
            }
            //connectionFailed();
            return;
        }
        
        // Reset the ConnectThread because we're done
        synchronized (BluetoothService.this) {
           // Cancel any thread currently running a connection
           if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        
           // Do work to manage the connection (in a separate thread) 
           // Start the thread to manage the connection and perform transmissions
           mConnectedThread = new ConnectedThread(mmSocket);;
           mConnectedThread.start();
          
           // Send the name of the connected device back to the UI Activity
           //Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
           //Bundle bundle = new Bundle();
           //bundle.putString(DEVICE_NAME, device.getName());
           //msg.setData(bundle);
           //mHandler.sendMessage(msg);
        }
    }
 
    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { Log.e(TAG, "close connect socket failed", e); }
    }
}
/*=========================================ConnectThread �ڲ���end==============================*/



/*=========================================ConnectedThread �ڲ���start==========================*/
/* This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 * ���ӳɹ������߳��ڲ��� 
 * ��Ҫ����Ϊ����Ϣ���߳������� ������Ҫ�¿��߳̽�����Ϣ��,д��Ϣû���½��߳�
 */
private class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    // Ϊ�˷������ݶ�ȡ,ʹ��BufferedReader��װ�����ַ���ȡ
    private final BufferedReader In;   
    /* �Զ������Ӻ����� */
    public ConnectedThread(BluetoothSocket socket) {
        Log.d(TAG, "create ConnectedThread");
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
 
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {  Log.e(TAG, "temp sockets not created", e); }
 
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        In = new BufferedReader(new InputStreamReader(tmpIn));
    }
    /* �µĶ���Ϣ���߳� */
    public void run() {
    	Log.i(TAG, "begin mConnectedThread");
        //byte[] buffer = new byte[128];  // buffer store for the stream
        //int bytes; // bytes returned from read()
        String str = null;
        
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                //bytes = mmInStream.read(buffer);
            	str = In.readLine();
            	
                // Send the obtained bytes to the UI activity
                //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
            	mHandler.obtainMessage(MESSAGE_READ, -1, -1, str).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "connectionLost", e);
                //connectionLost();
                // Start the service over to restart listening mode
                //BluetoothService.this.start();
                break;
            }
        }
    }
 
    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
            // Share the sent message back to the UI Activity
            //mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
        } catch (IOException e) { Log.e(TAG, "mmOutStream.write(bytes) failed", e); }
    }
 
    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { Log.e(TAG, "mmSocket.close() failed", e); }
    }
}
/*=========================================ConnectedThread �ڲ���end==============================*/


}