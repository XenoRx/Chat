module ru.gb.chatclient {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	
	
	opens ru.gb.chatclient to javafx.fxml;
	exports ru.gb.chatclient;
}