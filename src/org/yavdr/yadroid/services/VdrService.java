package org.yavdr.yadroid.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class VdrService extends Service {
	private static final DefaultHttpClient client = new DefaultHttpClient();

	private static final String TAG = VdrService.class.toString();
	private String urlPrefix;
	private LinkedList<String> requests = new LinkedList<String>();
	private AtomicBoolean active = new AtomicBoolean(true);

	@Override
	public void onCreate() {
		super.onCreate();

		new Thread(threadBody).start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		active.set(false);
		synchronized (requests) {
			requests.clear();
			requests.notifyAll();
		}
	}

	// Binder given to clients
	private final IBinder mBinder = new VdrBinder();

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class VdrBinder extends Binder {
		public VdrService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return VdrService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void keyVolUp() {
		synchronized (requests) {
			requests.add(this.urlPrefix + "/remote/volup");
			requests.notify();
		}
	}

	public void keyVolDown() {
		synchronized (requests) {
			requests.add(this.urlPrefix + "/remote/voldn");
			requests.notify();
		}
	}

	public void keyMenu() {
		synchronized (requests) {
			requests.add(this.urlPrefix + "/remote/menu");
			requests.notify();
		}
	}

	public void keyBack() {
		synchronized (requests) {
			requests.add(this.urlPrefix + "/remote/back");
			requests.notify();
		}
	}
	
	public static boolean isOnline(String vdr, int port) {
		try {
			HttpResponse response = client.execute(new HttpGet(new URI("http://" + vdr + ":" + port + "/info.json")));
			return response.getStatusLine().getStatusCode() == 200;
		} catch (Exception e) {
			return false;
		}
	}

	private Runnable threadBody = new Runnable() {
		public void run() {
			while (active.get()) {
				try {
					synchronized (requests) {
						requests.wait();
					}

					synchronized (requests) {
						for (String url : requests) {
							try {
								client.execute(new HttpPost(new URI(url)));
							} catch (Exception e) {
								Log.e(TAG, e.getMessage());
							}
						}
						requests.clear();
					}

				} catch (InterruptedException e) {
				}
			}
		}
	};

}
