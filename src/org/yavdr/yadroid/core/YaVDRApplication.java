package org.yavdr.yadroid.core;

import android.app.Application;

public class YaVDRApplication extends Application {

	private String host;
	private int port;

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public String getRestfulPrefix() {
		return "http://" + host + ":" + port;
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
