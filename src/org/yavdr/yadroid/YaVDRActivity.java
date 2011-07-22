package org.yavdr.yadroid;

import org.yavdr.yadroid.core.YaVDRApplication;
import org.yavdr.yadroid.services.PushService;
import org.yavdr.yadroid.services.VdrService;
import org.yavdr.yadroid.services.VdrService.VdrBinder;
import org.yavdr.yadroid.services.ZeroConfService;
import org.yavdr.yadroid.services.ZeroConfService.ZeroConfBinder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public abstract class YaVDRActivity extends Activity {

	protected VdrService vdrService;
	protected boolean vdrBound = false;

	protected ZeroConfService zcService;
	protected boolean zcBound = false;

	protected ServiceConnection vdrConnection;
	protected ServiceConnection zcConnection;

	@Override
	protected void onStart() {
		super.onStart();

		zcConnection = getZCServiceConnection();
		// Bind to LocalService
		Intent zcIntent = new Intent(this, ZeroConfService.class);
		bindService(zcIntent, zcConnection, Context.BIND_AUTO_CREATE);

		vdrConnection = getVdrServiceConnection();
		// Bind to LocalService
		Intent intent = new Intent(this, VdrService.class);
		bindService(intent, vdrConnection, Context.BIND_AUTO_CREATE);
		
		if (((YaVDRApplication)getApplicationContext()).getRestfulPrefix() != null)
			PushService.actionStart(getApplicationContext());
	}

	/** Defines callbacks for service binding, passed to bindService() */
	protected ServiceConnection getVdrServiceConnection() {
		return new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				// We've bound to LocalService, cast the IBinder and get
				// LocalService instance
				VdrBinder binder = (VdrBinder) service;
				vdrService = binder.getService();
				vdrBound = true;
			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				vdrBound = false;
			}
		};
	}

	/** Defines callbacks for service binding, passed to bindService() */
	protected ServiceConnection getZCServiceConnection() {
		
		return new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				// We've bound to LocalService, cast the IBinder and get
				// LocalService instance
				ZeroConfBinder binder = (ZeroConfBinder) service;
				zcService = binder.getService();
				zcBound = true;
			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				zcBound = false;
			}
		};
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		// The activity is no longer visible (it is now "stopped")
		// Unbind from the service
		if (vdrBound) {
			unbindService(vdrConnection);
			vdrBound = false;
		}

		if (zcBound) {
			unbindService(zcConnection);
			zcBound = false;
		}
		
		PushService.actionStop(getApplicationContext());
	}
}
