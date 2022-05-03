package ru.gb.chatclient.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
	private final AuthService authService;
	private final Map<String, ClientHandler> clients;
	
	public ChatServer() {
		this.clients = new HashMap<>();
		authService = new InMemoryAuthService();
		authService.run();
	}
	
	public void run() {
		try (ServerSocket serverSocket = new ServerSocket(8189)) {
			while (true) {
				System.out.println("Ожидаем подключения клиента");
				final Socket socket = serverSocket.accept();
				new ClientHandler(socket, this, authService);
				System.out.println("Клиент подключился");
			}
		} catch (IOException e) {
			throw new RuntimeException("Ошибка сервера", e);
		}
	}
	
	public boolean isNickBusy(String nick) {
		return clients.containsKey(nick);
		/*for (ClientHandler client : clients) { //old ver
			if (client.getNick().equals(nick)) {
				return true;
			}*/
		}
	
	public void broadCast(String message) {
		clients.values().forEach(client->client.sendMessage(message));
		/*
		for (ClientHandler client : clients) { //old ver
			client.sendMessage(message);
		}
		*/
	}
	
	public void subscribe(ClientHandler client) {
		clients.put(client.getNick(), client);
	}
	
	public void unsubscribe(ClientHandler client) {
		clients.remove(client.getNick());
	}
}
