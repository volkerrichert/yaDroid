package org.yavdr.yadroid.core;

import org.yavdr.yadroid.dao.pojo.Vdr;

import android.app.Application;

public class YaVDRApplication extends Application {

	private Vdr currentVdr;

	public void setCurrentVdr(Vdr vdr) {
		this.currentVdr = vdr;
	}

	public Vdr getCurrentVdr() {
		return currentVdr;
	}
	/*
	public String getRestfulPrefix() {
		return "http://" + host + ":" + port;
	}
*/
	@Override
	public void onCreate() {
		super.onCreate();

	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		
	}
}
