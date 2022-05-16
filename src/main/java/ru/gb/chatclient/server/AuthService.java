package ru.gb.chatclient.server;

import java.io.IOException;

public interface AuthService extends Cloneable {
	String getNickByLoginAndPassword(String login, String password);
	
	void run();
	
	void close() throws IOException;
}
