package org.yavdr.yadroid;

import org.yavdr.yadroid.core.YaVDRApplication;
import org.yavdr.yadroid.dao.YaDroidDBHelper;
import org.yavdr.yadroid.services.VdrService;
import org.yavdr.yadroid.services.VdrService.VdrBinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;

public class Menu extends Activity {

    private VdrService mService;
    private boolean mBound = false;
	private String urlPrefix;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		urlPrefix = ((YaVDRApplication)getApplication()).getRestfulPrefix();
        // Bind to LocalService
        Intent intent = new Intent(this, VdrService.class);
        intent.putExtra("urlPrefix", urlPrefix);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mBound) 
				mService.keyBack();
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (mBound) 
				mService.keyVolUp();
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (mBound) 
				mService.keyVolDown();
			return true;
		case KeyEvent.KEYCODE_MENU:
			if (mBound) 
				mService.keyMenu();
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            VdrBinder binder = (VdrBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
