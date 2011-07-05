package org.yavdr.yadroid.adapter;

import java.io.Serializable;

public class EpgElement implements Serializable {
	
	private static final long serialVersionUID = -5027971604893596505L;
	
	private int id;
	private String title;
	private String shortText;
	private String description;
	private long startTime;
	private int duration;
	private String imageUrl;
	private int imageCount;

	public EpgElement(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getShortText() {
		return shortText;
	}
	public void setShortText(String shortText) {
		this.shortText = shortText;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setImageCount(int images) {
		this.imageCount = images;	
	}
	
	public int getImageCount() {
		return imageCount;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}
}
