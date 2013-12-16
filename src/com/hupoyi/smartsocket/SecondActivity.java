package com.hupoyi.smartsocket;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SecondActivity extends Activity { 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.second_layout);
		InitUI();
	}
	
	private void InitUI() {
		Button send = (Button) super.findViewById(R.id.send);
    	send.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SmartSocket.mBluethoothService.write("haha i am here");
			}
    		
    	});
    	
    	Button back = (Button) super.findViewById(R.id.back);
    	back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
    		
    	});
	}
}

