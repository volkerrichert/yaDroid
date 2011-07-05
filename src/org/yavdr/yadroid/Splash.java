package org.yavdr.yadroid;

import java.net.Inet4Address;

import org.yavdr.yadroid.core.YaVDRApplication;
import org.yavdr.yadroid.services.ZeroConfService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

public class Splash extends YaVDRActivity {

	public static final String ACTION = "Splash";
	public static final String TAG = Splash.class.toString();

	private AnimationSet animationSet;
	private Thread mSplashThread;
	private ZeroConfReceiver zeroConfIntentReceiver;

    private String startChannel;
	
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

			public void onAnimationStart(Animation arg0) {}

			public void onAnimationRepeat(Animation arg0) {}

			public void onAnimationEnd(Animation arg0) {
				arg0.reset();
				arg0.start();
			}
		});
		
        
        // The thread to wait for splash screen events
        mSplashThread =  new Thread() {
            @Override
            public void run(){
                try {
                    synchronized(this) {
                        // Wait given period of time or exit on touch
                        wait();
                    }
                }
                catch(InterruptedException ex) {}

                finish();
                
        		Intent intent = new Intent("org.yavdr.yadroid.intent.action.START");
        		intent.putExtra(EpgOverview.STARTACTIVITY, true);
        		intent.putExtra("channel", startChannel);
        		startActivity(intent);
        		
                stop();                    
            }
        };
        
        mSplashThread.start();
        
        zeroConfIntentReceiver = new ZeroConfReceiver();
        IntentFilter intentSFilter = new IntentFilter(ZeroConfService.YADROID_ZEROCONF_INTENT);
        registerReceiver(zeroConfIntentReceiver, intentSFilter);
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
	
	protected class ZeroConfReceiver extends BroadcastReceiver
	{
		@Override
	    public void onReceive(Context context, Intent intent)
	    {
	        Bundle notificationData = intent.getExtras();
	        Inet4Address host = (Inet4Address) notificationData.getSerializable(ZeroConfService.YADROID_ZEROCONF_INFO_ADDRESS);
	        int port = notificationData.getInt(ZeroConfService.YADROID_ZEROCONF_INFO_PORT);
	    	
	        ((YaVDRApplication)getApplication()).setUrlPrefix("http://" + host.getHostAddress() + ":" + port);

	        //TODO remove it
	        startChannel = notificationData.getString("channel");
	        
	        synchronized(mSplashThread){
                mSplashThread.notifyAll();
            }
	    }
	}
}