package com.kanasansoft.WebSocketRemote;

import org.eclipse.jetty.websocket.WebSocket.Outbound;

public interface OnMessageObserver {
	void onMessage(Outbound outbound, byte frame, String data);
	void onMessage(Outbound outbound, byte frame, byte[] data, int offset, int length);
}
