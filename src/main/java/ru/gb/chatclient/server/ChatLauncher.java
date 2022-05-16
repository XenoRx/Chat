package ru.gb.chatclient.server;

public class ChatLauncher {
	public static void main(String[] args) {
		final ChatServer server = new ChatServer();
		server.run();
	}
}
