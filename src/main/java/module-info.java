module ru.gb.chatclient {
	requires javafx.controls;
	requires javafx.fxml;
	
	
	opens ru.gb.chatclient to javafx.fxml;
	exports ru.gb.chatclient;
}