package com.example.emulator;

import java.io.File;
import java.io.IOException;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;


@SuppressLint("NewApi")
public class EmulatorService extends Service {
	
	final RemoteCallbackList<EmulatorAIDLCallback> mCallbacks = new RemoteCallbackList<EmulatorAIDLCallback>();
	NotificationManager mNM;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	private final EmulatorAIDL.Stub mBinder = new EmulatorAIDL.Stub() {
		
		@Override
		public void unregisterCallback(EmulatorAIDLCallback cb)
				throws RemoteException {
			// TODO Auto-generated method stub
			if(cb != null) mCallbacks.unregister(cb);
			
		}
		
		@Override
		public void registerCallback(EmulatorAIDLCallback cb)
				throws RemoteException {
			// TODO Auto-generated method stub
			if(cb != null) mCallbacks.register(cb);
			
		}

		@Override
		public void openfile() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void closefile() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification();
		NanoHttpd();
	}
	
	private void NanoHttpd() {
		// TODO Auto-generated method stub
		Log.e("onCreate","onCreate");
	        File path = Environment.getExternalStorageDirectory();
	        //File path = new File("/mnt/asec");
	        Log.e("Nano_Server", "############## path = " + path);
	        File wwwroot = path.getAbsoluteFile();
	        //File file = new File(path, "filename");
	        /**/
	        try {
				new NanoHTTPD(8091, wwwroot);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		mNM.cancel(R.string.remote_service_started);
		mCallbacks.kill();
	}

	private void showNotification() {
		// TODO Auto-generated method stub
		CharSequence text = getText(R.string.remote_service_started);
		
		Notification notification = new Notification(R.drawable.stat_sample, text, 
				System.currentTimeMillis());
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, 
				new Intent(this, Emulator.class), 0);	
		
		notification.setLatestEventInfo(this, getText(R.string.remote_service_label),
				text, contentIntent);
		
		mNM.notify(R.string.remote_service_started, notification);
	}
}
