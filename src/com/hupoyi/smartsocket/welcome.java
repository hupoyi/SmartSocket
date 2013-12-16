package com.hupoyi.smartsocket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class welcome extends Activity {

	Context context = welcome.this;
	private static final String TAG = "welcome";
	private static final boolean D = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
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
	    if(D) Log.e(TAG, "-- ON DESTORY --");
	 };
	    
	 
	private void InitUI() {
		Button qrcode_button = (Button) super.findViewById(R.id.qrcode_button);
		qrcode_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(welcome.this, SmartSocket.class);
				welcome.this.startActivity(intent);   // 启动其它Activity
				overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
				//finish();
			}
			
		});
	}
	
}
