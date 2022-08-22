package ru.gb.chatclient.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthService implements AuthService {
	private List<UserData> users;
	
	private class UserData {
		private String login;
		private String password;
		private String nick;
		
		public UserData(String login, String password, String nick) {
			this.login = login;
			this.password = password;
			this.nick = nick;
		}
	}
	
	@Override
	public String getNickByLoginAndPassword(String login, String password) {
		for (UserData user : users) {
			if (user.login.equals(login) && user.password.equals(password)) {
				return user.nick;
			}
		}
		return null;
	}
	
	@Override
	public void run() {
		users = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			users.add(new UserData("login" + i, "password" + i, "nick" + i));
		}
	}
	
	@Override
	public void close() throws IOException {
		System.out.println("Сервис аутентификации остановлен");
	}
}
