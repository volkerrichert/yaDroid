package org.yavdr.yadroid;

import org.yavdr.yadroid.activity.vdr.data.OsdChannel;
import org.yavdr.yadroid.activity.vdr.data.OsdProgramme;
import org.yavdr.yadroid.core.YaVDRApplication;
import org.yavdr.yadroid.services.PushService;
import org.yavdr.yadroid.services.VdrService;
import org.yavdr.yadroid.services.VdrService.VdrBinder;
import org.yavdr.yadroid.services.ZeroConfService;
import org.yavdr.yadroid.services.ZeroConfService.ZeroConfBinder;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public abstract class YaVDRListActivity extends ListActivity {
	public static final String TAG = YaVDRListActivity.class.toString();
	
	protected VdrService vdrService;
	protected boolean vdrBound = false;

	protected ZeroConfService zcService;
	protected boolean zcBound = false;

	protected ServiceConnection vdrConnection;
	protected ServiceConnection zcConnection;

	private Toast toast;
	private Thread thread;
	private BroadcastReceiver osdChannelReceiver;
	private BroadcastReceiver osdClearReceiver;
	private BroadcastReceiver osdProgrammeReceiver;
	private View channelLayout;
	private TextView channelTitle;
	private WakeLock wl;

	public YaVDRListActivity() {
		super();
		Log.d(TAG, "construct");
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onCreate");
		
		super.onCreate(savedInstanceState);
		
		LayoutInflater inflater = getLayoutInflater();
		channelLayout = inflater.inflate(R.layout.osdchannel,
				(ViewGroup) findViewById(R.id.toast_layout_root));
		channelTitle = (TextView) channelLayout.findViewById(R.id.title);

		toast = new Toast(getApplicationContext());
		toast.setView(channelLayout);

		toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
		// toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart1");
		super.onStart();
		Log.d(TAG, "onStart2");
		zcConnection = getZCServiceConnection();
		// Bind to LocalService
		Intent zcIntent = new Intent(this, ZeroConfService.class);
		bindService(zcIntent, zcConnection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "onStart3");
		vdrConnection = getVdrServiceConnection();
		// Bind to LocalService
		Intent intent = new Intent(this, VdrService.class);
		bindService(intent, vdrConnection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "onStart4");
		if (((YaVDRApplication) getApplicationContext()).getCurrentVdr() != null) {
			PushService.actionStart(getApplicationContext());
		}
		Log.d(TAG, "onStart5");
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");

		super.onResume();
		wl.acquire();
		Log.d(TAG, "onResume");

		osdClearReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (thread != null && thread.isAlive())
					synchronized (thread) {
						thread.interrupt();
						//thread = null;
					}
			};
		};
		registerReceiver(osdClearReceiver, new IntentFilter(
				"org.yavdr.yadroid.intent.vdr.OsdClear"));

		osdChannelReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String title = ((OsdChannel) intent
						.getSerializableExtra("data")).getTitle();
				channelTitle.setText(title);
				((TextView) channelLayout.findViewById(R.id.presenttitle)).setText("");
				((TextView) channelLayout.findViewById(R.id.presentsubtitle)).setText("");

				toast.show();
				if (!thread.isAlive()) {
					thread.stop();
				}
			};
		};
		registerReceiver(osdChannelReceiver, new IntentFilter(
				"org.yavdr.yadroid.intent.vdr.OsdChannel"));

		osdProgrammeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				OsdProgramme osdProgramme = ((OsdProgramme) intent
						.getSerializableExtra("data"));
				String text = osdProgramme.getPresentTitle();
				if (text != null)
					((TextView) channelLayout.findViewById(R.id.presenttitle))
							.setText(text);
				else
					((TextView) channelLayout.findViewById(R.id.presenttitle))
							.setText("");
				text = osdProgramme.getPresentSubtitle();
				if (text != null)
					((TextView) channelLayout
							.findViewById(R.id.presentsubtitle)).setText(text);
				else
					((TextView) channelLayout
							.findViewById(R.id.presentsubtitle)).setText("");
			};
		};
		registerReceiver(osdProgrammeReceiver, new IntentFilter(
				"org.yavdr.yadroid.intent.vdr.OsdProgramme"));
		Log.d(TAG, "onResume");

		thread = new Thread() {
			@Override
			public void run() {
				try {

					while (!this.isInterrupted()) {
						synchronized (this) {
							// Wait given period of time or exit on touch
							wait(1000);
						}
						toast.show();
					}

				} catch (InterruptedException ex) {
				}

				toast.cancel();
				stop();
			}
		};
		Log.d(TAG, "onResume");

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		wl.release();
		
		if (thread != null && thread.isAlive())
			synchronized (thread) {
				thread.interrupt();
				thread = null;
			}

		if (osdClearReceiver != null)
			unregisterReceiver(osdClearReceiver);
		if (osdProgrammeReceiver != null)
			unregisterReceiver(osdProgrammeReceiver);
		if (osdChannelReceiver != null)
			unregisterReceiver(osdChannelReceiver);
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

		//PushService.actionStop(getApplicationContext());
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection getVdrServiceConnection() {
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
	private ServiceConnection getZCServiceConnection() {

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
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.help:
			help();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	protected void help() {
		Toast.makeText(getApplicationContext(), R.string.helptext, Toast.LENGTH_SHORT).show();
	}
}
