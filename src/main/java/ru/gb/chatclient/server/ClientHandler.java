package ru.gb.chatclient.server;

import ru.gb.chatclient.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
	private final Socket socket;
	private final ChatServer server;
	private String nick;
	private final DataInputStream in;
	private final DataOutputStream out;
	private final AuthService authService;
	
	public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
		try {
			this.nick = "";
			this.socket = socket;
			this.server = server;
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
			this.authService = authService;
			new Thread(() -> {
				try {
					authenticate();
					readMessage();
				} finally {
					closeConnection();
				}
			}).start();
		} catch (IOException e) {
			throw new RuntimeException("Ошибка создания подключения к клиенту", e);
		}
	}
	
	private void authenticate() {
		while (true) {
			try {
				final String str = in.readUTF();// /auth login1 password1
				if (Command.isCommand(str)) {
					final Command command = Command.getCommand(str);
					final String[] params = command.parse(str);
					if (command == Command.AUTH) {
						final String login = params[0];
						final String password = params[1];
						final String nick = authService.getNickByLoginAndPassword(login, password);
						if (nick != null) {
							if (server.isNickBusy(nick)) {
								sendMessage(Command.ERROR, "Пользователь уже авторизован");
								continue;
							}
							sendMessage(Command.AUTHOK, nick);// /authok nick1
							this.nick = nick;
							server.broadCast("Пользователь " + nick + " вошёл в чат");
							server.subscribe(this);
							break;
						} else {
							sendMessage(Command.ERROR, "Неверные логин или пароль");
						}
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
	
	public void sendMessage(String message) {
		try {
			System.out.println("SERVER: send message to: " + nick);
			out.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void closeConnection() {
		sendMessage(Command.END);
		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("Ошибка отключения", e);
		}
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("Ошибка отключения", e);
		}
		try {
			if (socket != null) {
				server.unsubscribe(this);
				socket.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("Ошибка отключения", e);
		}
	}
	
	private void readMessage() {
		try {
			while (true) {
				final String msg = in.readUTF();
				System.out.println("Получено сообщение: " + msg);
				if (Command.isCommand(msg)) {
					final Command command = Command.getCommand(msg);
					final String[] params = command.parse(msg);
					if (command == Command.END) {
						break;
					}
					if (command == Command.PRIVATE_MESSAGE) {
						server.sendMessageToClient(this, params[0], params[1]);
						continue;
					}
				}
				server.broadCast(nick + ": " + msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public String getNick() {
		return nick;
	}
}
