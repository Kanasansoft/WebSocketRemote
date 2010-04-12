package com.kanasansoft.WebSocketRemote;

import java.awt.Rectangle;
import java.util.Date;

class ScreenData {
	static long imageIdSequence=0;
	long imageId;
	Date date;
	byte[] base64 = null;
	Rectangle rect = null;
	public ScreenData() {
		this.imageId = ++imageIdSequence;
		this.date = new Date();
		this.base64 = null;
		this.rect = null;
	}
	private ScreenData(long imageId, Date date, byte[] base64, Rectangle rect) {
		this.imageId = imageId;
		this.date = new Date(date.getTime());
		this.base64 = base64.clone();
		this.rect = new Rectangle(rect.x, rect.y, rect.width, rect.height);
	}
	synchronized void set(byte[] base64, Rectangle rect) {
		this.imageId = ++imageIdSequence;
		this.date = new Date();
		this.base64 = base64;
		this.rect = rect;
	}
	synchronized ScreenData get() {
		if(this.base64==null||this.rect==null){
			return null;
		}
		return new ScreenData(this.imageId, this.date, this.base64, this.rect);
	}
}
