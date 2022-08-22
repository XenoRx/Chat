package ru.gb.chatclient.server;

import ru.gb.chatclient.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
	private final ChatServer chatServer;
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;
	private String nick;
	private AuthService authService;
	
	public boolean authTimeout;
	public boolean isUserAuthenticated;
	
	public String getNick() {
		return nick;
	}
	
	public ClientHandler(Socket socket, ChatServer chatServer, AuthService authService) {
		try {
			this.chatServer = chatServer;
			this.socket = socket;
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
			this.nick = "";
			this.authService = authService;
			this.authTimeout = false;
			this.isUserAuthenticated = false;
			
			
			new Thread(() -> {
				try {
					authentication();
					if (authTimeout) {
						closeConnection();
					}
					readMessages();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					closeConnection();
				}
			}).start();
		} catch (IOException e) {
			throw new RuntimeException("Ошибка треда");
		}
	}
	
	public void authentication() throws IOException {
		
		Thread timeoutThread = new Thread(() -> {
			for (int i = 0; i < 2; i++) {
				System.out.println("Минут до отключения: " + (2 - i)); // подрезал идею
				
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				if (this.isUserAuthenticated) break;
			}
			
			if (! this.isUserAuthenticated) {
				System.out.println("Пользователь " + nick + " отключён");
				this.authTimeout = true;
			}
			
		});
		timeoutThread.start();
		
		while (true) {
			String msg = in.readUTF();
			if (authTimeout) {
				sendMessage(Command.ERROR, "Время для входа вышло");
				break;
			}
			;
			
			if (Command.isCommand(msg)) {
				Command command = Command.getCommand(msg);
				String[] params = command.parse(msg);
				if (command == Command.AUTH) {
					String login = params[0];
					String password = params[1];
					String nick = authService.getNickByLoginAndPassword(login, password);
					
					if (nick != null) {
						if (chatServer.isNickBusy(nick)) {
							this.isUserAuthenticated = true;
							sendMessage(Command.ERROR, "Пользователь уже авторизован");
							continue;
						}
						sendMessage(Command.AUTHOK, nick);
						this.nick = nick;
						chatServer.broadCast("Сервер: " + nick + " вошёл в чат");
						chatServer.subscribe(this);
						break;
					} else {
						sendMessage(Command.ERROR, "Неверные логин или пароль");
					}
				}
			}
		}
	}
	
	public void readMessages() throws IOException {
		while (true) {
			String msg = in.readUTF();
			System.out.println("Сообщение получено от " + nick + ": " + msg);
			
			if (Command.isCommand(msg)) {  // если msg начинается с / то это команда и мы проваливаемся ниже
				
				Command cmd = Command.getCommand(msg);
				String[] params = cmd.parse(msg);
				
				if (cmd == Command.END) { // /end
					break;
				}
				if (cmd == Command.PRIVATE_MESSAGE) { // /w приватное сообщение
					chatServer.sendMessageToClient(this, params[0], params[1]);
					continue;
				}
			}
			chatServer.broadCast(nick + ": " + msg);
			
		}
	}
	
	public void sendMessage(Command command, String... params) {
		sendMessage(command.collectMessage(params));
		
	}
	
	public void sendMessage(String msg) {
		
		try {
			out.writeUTF(msg);
			System.out.println("Sending: " + msg);
		} catch (IOException e) {
			throw new RuntimeException("Сообщение об ошибке");
		}
	}
	
	public void closeConnection() {
		sendMessage(Command.END);
		chatServer.unsubscribe(this);
		chatServer.broadCast("Server: " + nick + " вышел из чата");
		
		try {
			if (in != null) in.close();
		} catch (IOException e) {
			throw new RuntimeException("Ошибка входящего потока");
		}
		
		try {
			out.close();
		} catch (IOException e) {
			throw new RuntimeException("Ошибка выводящего потока");
		}
		
		try {
			socket.close();
			chatServer.unsubscribe(this);
		} catch (IOException e) {
			throw new RuntimeException("Ошибка закрытия сокета");
		}
	}
	
	
}
