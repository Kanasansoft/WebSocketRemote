package com.kanasansoft.WebSocketRemote;

import java.awt.Rectangle;

class ScreenData {
	byte[] base64 = null;
	Rectangle rect = null;
	public ScreenData() {
		this.base64 = null;
		this.rect = null;
	}
	public ScreenData(byte[] base64, Rectangle rect) {
		this.base64 = base64.clone();
		this.rect = new Rectangle(rect.x, rect.y, rect.width, rect.height);
	}
	synchronized void set(byte[] base64, Rectangle rect) {
		this.base64 = base64;
		this.rect = rect;
	}
	synchronized ScreenData get() {
		if(this.base64==null||this.rect==null){
			return null;
		}
		return new ScreenData(this.base64, this.rect);
	}
}
