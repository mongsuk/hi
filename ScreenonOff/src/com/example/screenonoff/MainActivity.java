package com.example.screenonoff;

import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.app.Activity;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		Button button = (Button)findViewById(R.id.ScreenOff);
		button.setOnClickListener(ScreenOff); 
	}
	
	OnClickListener ScreenOff = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			WindowManager.LayoutParams params = getWindow().getAttributes();
	       params.flags |= LayoutParams.FLAG_KEEP_SCREEN_ON;
	       params.screenBrightness = 0;
	       getWindow().setAttributes(params);			
		}
	};
}
