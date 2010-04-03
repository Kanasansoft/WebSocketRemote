package com.kanasansoft.WebSocketRemote;

import org.eclipse.jetty.websocket.WebSocket.Outbound;

public interface OnMessageObserver {
	@Deprecated
	void onMessage(byte frame, String data);
	@Deprecated
	void onMessage(byte frame, byte[] data, int offset, int length);
	void onMessage(Outbound outbound, byte frame, String data);
	void onMessage(Outbound outbound, byte frame, byte[] data, int offset, int length);
}
