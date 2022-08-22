package ru.gb.chatclient.server;

import java.sql.*;

//1. Добавить в сетевой чат аутентификацию через базу данных SQLite.
//2. * Добавить в сетевой чат возможность смены ника.
public class ChatDbCreation {
	public static void main(String[] args) {
		ChatDbCreation chatDbCreation = new ChatDbCreation();
		chatDbCreation.connectDb();
	}
	
	public void connectDb() {
		try {
			Connection connection = DriverManager.getConnection("jdbc:sqlite:chatdb.db");
			System.out.println("Connection to SQL base success");
			createSqlTable(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createSqlTable(Connection connection) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement("" +
				"CREATE TABLE IF NOT EXISTS authenticated_users (" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"nick TEXT," +
				"login TEXT," +
				"password TEXT" +
				")")) {
			preparedStatement.executeUpdate();
			System.out.println("Base Creation Success");
		}
	}
}