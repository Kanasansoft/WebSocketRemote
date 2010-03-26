package com.kanasansoft.WebSocketRemote;

import java.awt.Rectangle;

class ScreenData {
	String encoded = null;
	Rectangle rect = null;
	public ScreenData() {
		this.encoded = null;
		this.rect = null;
	}
	public ScreenData(String encoded, Rectangle rect) {
		this.encoded = new String(encoded);
		this.rect = new Rectangle(rect.x, rect.y, rect.width, rect.height);
	}
	synchronized void set(String base64, Rectangle rect) {
		this.encoded = base64;
		this.rect = rect;
	}
	synchronized ScreenData get() {
		if(this.encoded==null||this.rect==null){
			return null;
		}
		return new ScreenData(this.encoded, this.rect);
	}
}
