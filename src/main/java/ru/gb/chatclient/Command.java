package ru.gb.chatclient;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

public enum Command { // перечисления
	/**
	 * Имплементируем абстрактный метод  "parse"
	 * Оверрайдим метод для каждого из перечислений enum
	 */
	AUTH("/auth") {
		@Override
		public String[] parse(String commandText) {// /auth login1 password1
			final String[] split = commandText.split(COMMAND_DELIMITER);
			return new String[]{split[1], split[2]};
		}
	},
	AUTHOK("/authok") {
		@Override
		public String[] parse(String commandText) { // /authok nick1
			return new String[]{commandText.split(COMMAND_DELIMITER)[1]};
		}
	},
	PRIVATE_MESSAGE("/w") {
		@Override
		public String[] parse(String commandText) {// /w nick1 сообщение для пользователя
			final String[] split = commandText.split(COMMAND_DELIMITER, 3);
			return new String[]{split[1], split[2]};
		}
	},
	END("/end") {
		@Override
		public String[] parse(String commandText) { // /end
			return new String[0];
		}
	},
	ERROR("/error") {
		@Override
		public String[] parse(String commandText) { //error Сообщение об ошибке
			final String errorMsg = commandText.split(COMMAND_DELIMITER, 2)[1];
			return new String[]{errorMsg};
		}
	},
	CLIENTS("/clients") {
		@Override
		public String[] parse(String commandText) { // /clients nick1 nick2 nick3 ...
			final String[] split = commandText.split(COMMAND_DELIMITER);
			final String[] nicks = new String[split.length - 1];
			return Arrays.stream(split).skip(1).toArray(String[]::new); //Вариант 1 через стрим
//			System.arraycopy(split, 0, nicks, - 1, split.length); //Вариант 2
			/*for (int i = 0; i < split.length; i++) {  // Вариант 3 не работает
				nicks[i - 1] = split[i];
			}
//			return nicks;*/
		}
	};
	
	private static final Map<String, Command> map = Stream.of(Command.values())
			.collect(Collectors.toMap(Command::getCommand, Function.identity()));
	
	private String command;
	private String[] params = new String[0];
	
	static final String COMMAND_DELIMITER = "\\s+";   //разделитель
	
	Command(String command) {
		this.command = command;
	}
	
	public static boolean isCommand(String message) {
		return message.startsWith("/");
	}
	
	public String getCommand() {
		return command;
	}
	
	public String[] getParams() {
		return params;
	}
	
	public static Command getCommand(String message) {
		message = message.trim();
		if (! isCommand(message)) {
			throw new RuntimeException("'" + message + "' is not a command");
		}
		final int index = message.indexOf(" ");
		final String cmd = index > 0 ? message.substring(0, index) : message; //Вариант 2 тернарное выражение если тру то выполнится часть после ? если ложь то часть после :
		final Command command = map.get(cmd);
		if (command == null) {
			throw new RuntimeException("'" + cmd + "' unknown command");
		}
		return command;
	}
	
	public abstract String[] parse(String commandText);
	
	public String collectMessage(String... params) {
		final String command = this.getCommand();
		return command + (params == null ? "" : " " + String.join(" ", params));
	}
	
	/*
    // вариант Мап 1
    private static final Map<String,Command> cmdMap = new HashMap<>(){{
        put("/auth",Command.AUTH);
        put("/authok",Command.AUTHOK);
        put("/w",Command.PRIVATE_MESSAGE);
        put("/end",Command.END);
    }};
    // вариант Мап 2 (immutable? ) - неизменяемый, быстрый
    private static final Map<String,Command> cmdMap = Map.of(
        "/auth",Command.AUTH,
        "/authok",Command.AUTHOK,
        "/w",Command.PRIVATE_MESSAGE,
        "/end",Command.END
    );
*/
}
