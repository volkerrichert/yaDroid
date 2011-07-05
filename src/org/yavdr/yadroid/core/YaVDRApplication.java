package org.yavdr.yadroid.core;

import android.app.Application;

public class YaVDRApplication extends Application {

	private String urlPrefix;
	
	public String getUrlPrefix() {
		return urlPrefix;
	}

	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}

	@Override
	public void onCreate() {
		super.onCreate();

	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		
	}
}
