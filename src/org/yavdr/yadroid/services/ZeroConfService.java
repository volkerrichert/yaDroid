package org.yavdr.yadroid.services;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.yavdr.yadroid.EpgOverview;
import org.yavdr.yadroid.R;
import org.yavdr.yadroid.Splash;
import org.yavdr.yadroid.core.YaVDRApplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ZeroConfService extends Service implements ServiceListener {
    // constants used to notify the Activity UI of received messages
    public static final String YADROID_ZEROCONF_INTENT = "org.yavdr.yadroid.services.ZeroConfService";
    public static final String YADROID_ZEROCONF_INFO_ADDRESS  = "org.yavdr.yadroid.services.ZeroConfService_Address";
    public static final String YADROID_ZEROCONF_INFO_PORT  = "org.yavdr.yadroid.services.ZeroConfService_Port";
	public static final String YADROID_ZEROCONF_REMOTE_TYPE = "_restful._tcp.local.";

	private static final String TAG = ZeroConfService.class.toString();
	
	private android.os.Handler handler = new android.os.Handler();

	private static JmDNS zeroConf = null;
	private static MulticastLock mLock = null;

	@Override
	public void onCreate() {
		super.onCreate();

		try {

			handler.postDelayed(new Runnable() {
				public void run() {
					setUp();
				}
			}, 1);

			// this.startProbe();
		} catch (Exception e) {
			Log.d(TAG, String.format("onCreate Error: %s", e.getMessage()));
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (zeroConf != null) {
			zeroConf.removeServiceListener(YADROID_ZEROCONF_REMOTE_TYPE, this);

			try {
				zeroConf.close();
				zeroConf = null;
			} catch (IOException e) {
				Log.d(TAG, String.format("ZeroConf Error: %s", e.getMessage()));
			}
		}

		if (mLock != null) {
			mLock.release();
			mLock = null;
		}

	}

	// Binder given to clients
	private final IBinder mBinder = new ZeroConfBinder();

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class ZeroConfBinder extends Binder {
		public ZeroConfService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return ZeroConfService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void serviceResolved(ServiceEvent ev) {
		//notifyUser("yaVDR found: http://"
		//		+ ev.getInfo().getInet4Addresses()[0].getHostName() + ":"
		//		+ ev.getInfo().getPort());
		
		Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(YADROID_ZEROCONF_INTENT);
        broadcastIntent.putExtra(YADROID_ZEROCONF_INFO_ADDRESS, ev.getInfo().getInet4Addresses()[0]);
        broadcastIntent.putExtra(YADROID_ZEROCONF_INFO_PORT, ev.getInfo().getPort());

        sendBroadcast(broadcastIntent);		
	}

	private void setUp() {
		android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
		mLock = wifi.createMulticastLock("yavdr");
		mLock.setReferenceCounted(true);
		mLock.acquire();
		try {
			zeroConf = JmDNS.create();
			zeroConf.addServiceListener(YADROID_ZEROCONF_REMOTE_TYPE, this);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
/*
	private void notifyUser(final String string) {
		handler.postDelayed(new Runnable() {
			public void run() {
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

				int icon = R.drawable.icon;
				long when = System.currentTimeMillis();

				Notification notification = new Notification(icon, string, when);

				Context context = getApplicationContext();
				CharSequence contentTitle = "yaDriod";
				Intent notificationIntent = new Intent(ZeroConfService.this,
						EpgOverview.class);
				PendingIntent contentIntent = PendingIntent.getActivity(
						ZeroConfService.this, 0, notificationIntent, 0);
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				notification.setLatestEventInfo(context, contentTitle, string,
						contentIntent);

				final int HELLO_ID = 1;

				mNotificationManager.notify(HELLO_ID, notification);
			}
		}, 1);
	}
*/
	public void serviceRemoved(ServiceEvent ev) {
		Log.d(TAG, "Service removed: " + ev.getName());
	}

	public void serviceAdded(ServiceEvent event) {
		// Required to force serviceResolved to be called again
		// (after the first search)
		Log.d(TAG,
				String.format("serviceAdded: %s %s", event.getType(),
						event.getName()));
		zeroConf.requestServiceInfo(event.getType(), event.getName(), 1);
	}
}
