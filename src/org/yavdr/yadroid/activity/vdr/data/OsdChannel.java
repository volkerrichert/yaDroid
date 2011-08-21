package org.yavdr.yadroid.activity.vdr.data;

import java.io.Serializable;
import java.util.Date;

public class OsdChannel implements Serializable {
	private static final long serialVersionUID = -2257859926332707736L;
	
	private String title;
	
	public OsdChannel() {}
	
	public OsdChannel(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
