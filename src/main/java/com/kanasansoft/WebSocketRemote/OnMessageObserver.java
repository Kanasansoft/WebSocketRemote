package com.kanasansoft.WebSocketRemote;

public interface OnMessageObserver {
	void onMessage(byte frame, String data);
	void onMessage(byte frame, byte[] data, int offset, int length);
}
