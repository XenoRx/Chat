package ru.gb.chatclient.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MsgHistoryTracker {
	public void write(String nick, String msg) {
		try (FileOutputStream out = new FileOutputStream(nick + ".txt", true)) {
			byte[] bytes = (msg + "\n").getBytes(StandardCharsets.UTF_8);
			out.write(bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
