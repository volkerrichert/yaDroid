package org.yavdr.yadroid;

import java.net.Inet4Address;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.yavdr.yadroid.core.YaVDRApplication;
import org.yavdr.yadroid.core.json.JSONRPCException;
import org.yavdr.yadroid.dao.YaDroidDBHelper;
import org.yavdr.yadroid.dao.pojo.Vdr;
import org.yavdr.yadroid.services.VdrService;
import org.yavdr.yadroid.services.ZeroConfService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

public class Splash extends YaVDRActivity {

	public static final String ACTION = "Splash";
	public static final String TAG = Splash.class.toString();

	private AnimationSet animationSet;
	private Thread mSplashThread;
	private ZeroConfReceiver zeroConfIntentReceiver;

	private String startChannel;
	private YaDroidDBHelper dbHelper;
	private Dao<Vdr, String> dao;
	private Vdr onlineVdr = null;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Splash screen view
		setContentView(R.layout.splash);

		// setup animation
		animationSet = new AnimationSet(true);
		Animation animation = AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in);
		animation.setDuration(2000);
		animationSet.addAnimation(animation);
		animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
		animation.setDuration(2000);
		animation.setStartOffset(2000);
		animationSet.addAnimation(animation);
		animationSet.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation arg0) {
			}

			public void onAnimationRepeat(Animation arg0) {
			}

			public void onAnimationEnd(Animation arg0) {
				arg0.reset();
				arg0.start();
			}
		});

		// The thread to wait for splash screen events
		mSplashThread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (this) {
						// Wait given period of time or exit on touch
						wait(10000);
					}

					finish();
					if (onlineVdr != null) {
						Intent intent = new Intent(
								"org.yavdr.yadroid.intent.action.START");
						intent.putExtra(EpgOverview.STARTACTIVITY, true);
						startActivity(intent);
					} else {
						//Toast.makeText(getApplicationContext(), "keinen VDR gefunden", Toast.LENGTH_SHORT). show();
						Intent intent = new Intent(
								"org.yavdr.yadroid.intent.action.MANAGEVDR");
						//intent.putExtra("offline", true);
						startActivity(intent);
					}
				} catch (InterruptedException ex) {
				}

			}
		};

		mSplashThread.start();

		dbHelper = new YaDroidDBHelper(getApplicationContext());

		zeroConfIntentReceiver = new ZeroConfReceiver();
		IntentFilter intentSFilter = new IntentFilter(
				ZeroConfService.YADROID_ZEROCONF_INTENT);
		registerReceiver(zeroConfIntentReceiver, intentSFilter);

		try {
			dao = dbHelper.getVdrDao();
			List<Vdr> knownVdr = dao.queryForAll();
			for (Iterator<Vdr> iterator = knownVdr.iterator(); iterator
					.hasNext();) {
				Vdr vdr = (Vdr) iterator.next();

				if (vdr.isOnline()) {
					((YaVDRApplication) getApplication()).setCurrentVdr(vdr);
					onlineVdr = vdr;
					
					synchronized (mSplashThread) {
						mSplashThread.notifyAll();
					}
					break;
				}
			}
		} catch (SQLException e) {
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		this.findViewById(R.id.SplashImageGlow).startAnimation(animationSet);
	}

	@Override
	protected void onPause() {
		super.onPause();
		animationSet.reset();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (zeroConfIntentReceiver != null)
			unregisterReceiver(zeroConfIntentReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.splash, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.manage_vdr:
			Intent intent = new Intent(
					"org.yavdr.yadroid.intent.action.MANAGEVDR");
			startActivity(intent);
			return true;
		case R.id.help:
			help();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected class ZeroConfReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle notificationData = intent.getExtras();
			Inet4Address host = (Inet4Address) notificationData
					.getSerializable(ZeroConfService.YADROID_ZEROCONF_INFO_ADDRESS);
			int port = notificationData
					.getInt(ZeroConfService.YADROID_ZEROCONF_INFO_PORT);

			onlineVdr = new Vdr(host.getHostName(), host.getHostAddress(),
					notificationData
							.getInt(ZeroConfService.YADROID_ZEROCONF_INFO_PORT));
			((YaVDRApplication) getApplication()).setCurrentVdr(onlineVdr);
			try {
				dao.createOrUpdate(new Vdr(host.getHostName(), host
						.getHostAddress(), port));
			} catch (SQLException e) {
				Log.e(TAG, e.toString());
			}

			// TODO remove it
			startChannel = notificationData.getString("channel");

			synchronized (mSplashThread) {
				mSplashThread.notifyAll();
			}
		}
	}
}