package ru.gb.chatclient.server;

import ru.gb.chatclient.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
	
	public void subscribe(ClientHandler client) {
		clients.put(client.getNick(), client);
		broadcastClientList();
	}
	
	public void unsubscribe(ClientHandler client) {
		clients.remove(client.getNick());
		broadcastClientList();
	}
	
	private void broadcastClientList() {
		StringBuilder nicks = new StringBuilder();  //Вариант с циклом
		for (ClientHandler value : clients.values()) {
			nicks.append(value.getNick()).append(" ");
		}
		broadcast(Command.CLIENTS, nicks.toString().trim());
		/*final String nicks = clients.values().stream()  //Вариант со стримом
				.map(ClientHandler::getNick)
				.collect(Collectors.joining(" "));
		broadcast(Command.CLIENTS, nicks);*/
	}
	
	private void broadcast(Command command, String nicks) {
		for (ClientHandler client : clients.values()) {
			client.sendMessage(command, nicks);
		}
	}
	
	public void broadCast(String message) {
		clients.values().forEach(client -> client.sendMessage(message));
		/*
		for (ClientHandler client : clients) { //old ver
			client.sendMessage(message);
		}
		*/
	}
	
	public void sendMessageToClient(ClientHandler sender, String to, String message) {
		final ClientHandler receiver = clients.get(to);
		if (receiver != null) {
			receiver.sendMessage("от " + sender.getNick() + ": " + message);
			sender.sendMessage(to + ": " + message);
		} else {
			sender.sendMessage(Command.ERROR, "Участника с ником " + to + " нет в чате!");
		}
	}
}
