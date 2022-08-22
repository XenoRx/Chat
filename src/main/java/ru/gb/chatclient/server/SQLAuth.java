package ru.gb.chatclient.server;

import java.sql.*;

public class SQLAuth implements AuthService {
	private Connection connection;
	
	public SQLAuth() {
		try {
			this.connection = DriverManager.getConnection("jdbc:sqlite:chatdb.db");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getNickByLoginAndPassword(String login, String password) {
		try (PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT nick FROM authenticated_users WHERE login = ? AND password = ?")) {
			preparedStatement.setString(1, login);
			preparedStatement.setString(2, password);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				String nickDB = resultSet.getString("nick");
				String passwordDB = resultSet.getString("password");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return login;
	}
	
	@Override
	public void run() {
	
	}
	
	@Override
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Auth is closed");
	}
}
