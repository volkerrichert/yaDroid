package org.yavdr.yadroid.adapter;

import java.io.Serializable;

public class Channel implements Serializable {

	private static final long serialVersionUID = -985014645647566219L;
	
	private String name;
	private int number;
	private String channelId;
	private String group;
	private int transponter;
	private String stream;
	private boolean isAtsc;
	private boolean isCable;
	private boolean isTerr;
	private boolean isSat;
	private boolean isRadio;
	private String imageUrl;

	public boolean isRadio() {
		return isRadio;
	}

	public void setRadio(boolean isRadio) {
		this.isRadio = isRadio;
	}

	public Channel(String channelId) {
		this.channelId = channelId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getChannelId() {
		return channelId;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getTransponter() {
		return transponter;
	}

	public void setTransponter(int transponter) {
		this.transponter = transponter;
	}

	public String getStream() {
		return stream;
	}

	public void setStream(String stream) {
		this.stream = stream;
	}

	public boolean isAtsc() {
		return isAtsc;
	}

	public void setAtsc(boolean isAtsc) {
		this.isAtsc = isAtsc;
	}

	public boolean isCable() {
		return isCable;
	}

	public void setCable(boolean isCable) {
		this.isCable = isCable;
	}

	public boolean isTerr() {
		return isTerr;
	}

	public void setTerr(boolean isTerr) {
		this.isTerr = isTerr;
	}

	public boolean isSat() {
		return isSat;
	}

	public void setSat(boolean isSat) {
		this.isSat = isSat;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

}