package ru.gb.chatclient;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	
	private ClientController controller;
	
	public ChatClient(ClientController controller) {
		this.controller = controller;
	}
	
	public void openConnection() throws Exception {
		socket = new Socket("localhost", 8189);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		final Thread readThread = new Thread(() -> {
			try {
				waitAuthenticate();
				readMessage();
			} finally {
				closeConnection();
			}
		});
		readThread.setDaemon(true);
		readThread.start();
	}
	
	private void readMessage() {
		while (true) {
			try {
				final String message = in.readUTF();
				System.out.println("Получено сообщение: " + message);
				if (Command.isCommand(message)) {
					final Command command = Command.getCommand(message);
					final String[] params = command.parse(message);
					if (command == Command.END) {
						controller.setAuth(false);
						break;
					}
					if (command == Command.ERROR) {
//						controller.showError(params);
						Platform.runLater(() -> controller.showError(params));
						continue;
					}
					if (command == Command.CLIENTS){
						controller.updateClientList(params);
						continue;
					}
				}
				controller.addMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void closeConnection() {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendMessage(String message) {
		try {
			System.out.println("Отправлено сообщение: " + message);
			out.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void waitAuthenticate() {
		while (true) {
			try {
				final String msgAuth = in.readUTF();// /authok <- nick
				if (Command.isCommand(msgAuth)) {
					final Command command = Command.getCommand(msgAuth);
					final String[] params = command.parse(msgAuth);
					if (command == Command.AUTHOK) {
						final String nick = params[0];
						controller.addMessage("Успешная авторизация под ником " + nick);
						controller.setAuth(true);
						break;
					}
					if (command == Command.ERROR) {
						Platform.runLater(() -> controller.showError(params));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendMessage(Command command, String... params) {
		sendMessage(command.collectMessage(params));
	}
}
