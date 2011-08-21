package org.yavdr.yadroid.activity.vdr.data;

import java.io.Serializable;
import java.util.Date;

public class OsdProgramme implements Serializable {
	private static final long serialVersionUID = 2819774861045730461L;
	
	private Date presentTime;
	private String presentTitle;
	private String presentSubtitle;
	private Date folloingTime;
	private String folloingTitle;
	private String folloingSubtitle;
	
	public OsdProgramme() {}

	public Date getPresentTime() {
		return presentTime;
	}

	public void setPresentTime(Date presentTime) {
		this.presentTime = presentTime;
	}

	public String getPresentTitle() {
		return presentTitle;
	}

	public void setPresentTitle(String presentTitle) {
		this.presentTitle = presentTitle;
	}

	public String getPresentSubtitle() {
		return presentSubtitle;
	}

	public void setPresentSubtitle(String presentSubtitle) {
		this.presentSubtitle = presentSubtitle;
	}

	public Date getFolloingTime() {
		return folloingTime;
	}

	public void setFolloingTime(Date folloingTime) {
		this.folloingTime = folloingTime;
	}

	public String getFolloingTitle() {
		return folloingTitle;
	}

	public void setFolloingTitle(String folloingTitle) {
		this.folloingTitle = folloingTitle;
	}

	public String getFolloingSubtitle() {
		return folloingSubtitle;
	}

	public void setFolloingSubtitle(String folloingSubtitle) {
		this.folloingSubtitle = folloingSubtitle;
	}
	
	
}
