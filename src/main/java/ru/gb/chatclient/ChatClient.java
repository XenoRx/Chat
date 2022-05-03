package ru.gb.chatclient;

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
				waitAuth();
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
				final String msg = in.readUTF();
				if ("/end".equals(msg)) {
					controller.toggleBoxesVisibility(false);
					break;
				}
				controller.addMessage(msg);
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
	
	private void waitAuth() {
		while (true) {
			try {
				final String msg = in.readUTF();// /authok <- nick
				if (msg.startsWith("/authok")) {
					final String[] split = msg.split(" ");
					final String nick = split[1];
					controller.addMessage("Успешная авторизация под ником " + nick);
					controller.toggleBoxesVisibility(true);
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
