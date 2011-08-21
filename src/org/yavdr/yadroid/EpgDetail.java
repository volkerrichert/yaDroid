package org.yavdr.yadroid;

import org.yavdr.yadroid.adapter.EpgElement;
import org.yavdr.yadroid.core.ImageLoader;
import org.yavdr.yadroid.core.YaVDRApplication;
import org.yavdr.yadroid.services.VdrService;
import org.yavdr.yadroid.services.VdrService.VdrBinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class EpgDetail extends Activity {

	private VdrService mService;
	private boolean mBound = false;

	private ImageLoader imageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		imageLoader = new ImageLoader(getApplicationContext());

		setContentView(R.layout.epgdetail);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Bind to LocalService
		Intent intent = new Intent(this, VdrService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

	}

	@Override
	protected void onResume() {
		super.onResume();

		EpgElement e = (EpgElement) getIntent().getExtras().getSerializable(
				"EPGDETAIL");
		((TextView) findViewById(R.id.epgdetailtitle)).setText(e.getTitle());

		SpannableString ss = new SpannableString(e.getDescription());

		//ss.setSpan(new MyLeadingMarginSpan2(3, 30), 0, ss.length(), 0);
		
		/*
		 * 
		 * SpannableString ss = new SpannableString(text); // Выставляем отступ
		 * для первых трех строк абазца ss.setSpan(new MyLeadingMarginSpan2(3,
		 * leftMargin), 0, ss.length(), 0);
		 * 
		 * TextView messageView = (TextView) findViewById(R.id.message_view);
		 * messageView.setText(ss);
		 */

		((TextView) findViewById(R.id.epgdetaildesc)).setText(ss);

		ImageView img;
		if (e.getImageCount() >= 1) {
			img = ((ImageView) findViewById(R.id.epgdetailimage1));

			img.setVisibility(View.VISIBLE);
			img.setTag(e.getImageUrl() + "0");
			imageLoader.DisplayImage(e.getImageUrl() + "0", this, img);

			if (e.getImageCount() >= 2) {
				img = ((ImageView) findViewById(R.id.epgdetailimage2));

				img.setVisibility(View.VISIBLE);
				img.setTag(e.getImageUrl() + "1");
				imageLoader.DisplayImage(e.getImageUrl() + "1", this, img);
				if (e.getImageCount() >= 3) {
					img = ((ImageView) findViewById(R.id.epgdetailimage3));

					img.setVisibility(View.VISIBLE);
					img.setTag(e.getImageUrl() + "2");
					imageLoader.DisplayImage(e.getImageUrl() + "2", this, img);
					if (e.getImageCount() >= 4) {
						img = ((ImageView) findViewById(R.id.epgdetailimage4));

						img.setVisibility(View.VISIBLE);
						img.setTag(e.getImageUrl() + "3");
						imageLoader.DisplayImage(e.getImageUrl() + "3", this,
								img);
						if (e.getImageCount() >= 5) {
							img = ((ImageView) findViewById(R.id.epgdetailimage5));

							img.setVisibility(View.VISIBLE);
							img.setTag(e.getImageUrl() + "4");
							imageLoader.DisplayImage(e.getImageUrl() + "4",
									this, img);
						}

					}

				}

			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
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
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
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
