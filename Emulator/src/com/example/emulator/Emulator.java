package com.example.emulator;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class Emulator extends Activity {

	public final static int SCREEN_ON=1;
	public final static int SCREEN_OFF=2;
	private IntentFilter screenFilter;
	
	private Button btn_start,btn_stop,btn_screen_on,btn_screen_off;
	private EmulatorAIDL mService = null;
//질문, 여기에 intent 나 powermanager 할당하면, 에러나 ! 왜그럴까
	
	private PowerManager Pm;
//	PowerManager.WakeLock mWakeLock, mWakeLock2;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Toast.makeText(Emulator.this, "onserviceDisConnected" ,Toast.LENGTH_SHORT).show();
		}
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			Toast.makeText(Emulator.this, "onserviceConnected" ,Toast.LENGTH_SHORT).show();
			mService=EmulatorAIDL.Stub.asInterface(arg1);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emulator);

	
		
		//ACQUIRED_로 받으면 또 에러...
	//	mWakeLock = mPm.newWakeLock(PowerManager.FULL_WAKE_LOCK , "power");
//		mWakeLock = mPm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "power");
	//make Button	
		btn_start= (Button) findViewById(R.id.btn_start);
		btn_stop= (Button) findViewById(R.id.btn_stop);
		btn_screen_on=(Button) findViewById(R.id.btn_screen_on);
		btn_screen_off=(Button) findViewById(R.id.btn_screen_off);
	  // PowerManager pm = (PowerManager) getSystemService( Context.POWER_SERVICE );
	  // PowerManager.WakeLock wakeLock = pm.newWakeLock( PowerManager.SCREEN_DIM_WAKE_LOCK, "MY TAG" );
	//BindService	
		btn_start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {		
				Intent intent = new Intent(Emulator.this, EmulatorService.class);
				bindService(intent,mConnection,Context.BIND_AUTO_CREATE);
				Toast.makeText(Emulator.this, "Bind()" ,Toast.LENGTH_SHORT).show();
			}
		});
	//unBindService
		btn_stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				unbindService(mConnection);
				Toast.makeText(Emulator.this, "UnBind()" ,Toast.LENGTH_SHORT).show();
			}
		});

	
	/*
	//Screen_on
		btn_screen_on.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);

				pm.userActivity(SCREEN_ON, true);
				// (long when, boolean noChangeLights)
				
				//mWakeLock.acquire(); 
				//boolean inScreenon= mPm.isScreenOn();
		    	//	getCurrentFocus().setKeepScreenOn(true);
			
				//setKeepScreenOn(true);
				//getWindow().addFlags(WindowManager.)//
			}
		});
	//Screen_off	
		btn_screen_off.setOnClickListener(new View.OnClickListener() {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
			@Override
			public void onClick(View v) {	
				PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
				pm.goToSleep(2000);
		//		pm.wakeUp(2000);
				//API 17이상 지원함, 나중에 wakeup할때 사용해보기
				//time 	The time when the request to go to sleep was issued, in the uptimeMillis() time base.
				
				
				
//				if(mWakeLock==null){Toast.makeText(Emulator.this,"null",Toast.LENGTH_LONG).show();}
//				Toast.makeText(Emulator.this,mWakeLock+":0",Toast.LENGTH_SHORT).show();
//				mWakeLock2.acquire();
//			
//				wakeLock.release();
//				PowerManager pm = (PowerManager) getSystemService( Context.POWER_SERVICE );
//				PowerManager.WakeLock wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, EmulatorService.TAG );		
			}
		});
	*/
		
	}	
	/* 나중에는 하나의 intent로 해서, 쫙 받을거야! (later)
	private BroadcastReceiver scrReceiver= new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
		String action = intent.getAction();
		if(action.equals(EmulatorService.TAG)){
			int cases = intent.getIntExtra("number",0);
			switch(cases){
			case SCREEN_ON:
				break;
			case SCREEN_OFF:
				  PowerManager pm= (PowerManager)getSystemService(Context.POWER_SERVICE);
				pm.goToSleep(2000);
		
				break;
			}
			
		}
			//	screenFilter = new IntentFilter(EmulatorService.TAG);
		}
	};
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.emulator, menu);
		return true;
	}

}
