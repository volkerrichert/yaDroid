package org.yavdr.yadroid.receiver;

import java.net.Inet4Address;
import java.sql.SQLException;

import org.yavdr.yadroid.core.YaVDRApplication;
import org.yavdr.yadroid.dao.pojo.Vdr;
import org.yavdr.yadroid.services.ZeroConfService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ZeroConfReceiver extends BroadcastReceiver {

	private YaVDRApplication application;

	public ZeroConfReceiver(YaVDRApplication application) {
		this.application = application;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle notificationData = intent.getExtras();
		Inet4Address host = (Inet4Address) notificationData
				.getSerializable(ZeroConfService.YADROID_ZEROCONF_INFO_ADDRESS);
		int port = notificationData
				.getInt(ZeroConfService.YADROID_ZEROCONF_INFO_PORT);

		application.setCurrentVdr(new Vdr(host.getHostName(), host
				.getHostAddress(), notificationData
				.getInt(ZeroConfService.YADROID_ZEROCONF_INFO_PORT)));

		/*
		 * onlineVdr = ; try { dao.createOrUpdate(new Vdr(host.getHostName(),
		 * host .getHostAddress(), port)); } catch (SQLException e) { Log.e(TAG,
		 * e.toString()); }
		 * 
		 * // TODO remove it startChannel =
		 * notificationData.getString("channel");
		 * 
		 * synchronized (mSplashThread) { mSplashThread.notifyAll(); }
		 */
	}
}