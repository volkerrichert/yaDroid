package org.yavdr.yadroid;

import org.yavdr.yadroid.services.VdrService;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public abstract class YaVDRListActivity extends ListActivity {

	protected VdrService mService;
	protected boolean mBound = false;
	protected ServiceConnection mConnection;

	@Override
	protected void onStart() {
		super.onStart();

		if (mConnection != null) {
			// Bind to LocalService
			Intent intent = new Intent(this, VdrService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		// The activity is no longer visible (it is now "stopped")
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}
}
