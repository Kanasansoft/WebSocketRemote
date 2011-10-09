package com.kanasansoft.WebSocketRemote;

import org.eclipse.jetty.websocket.WebSocket.Connection;

public interface OnMessageObserver {
	void onMessage(Connection connection, String data);
}
