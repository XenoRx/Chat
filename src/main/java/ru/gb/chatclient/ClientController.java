package ru.gb.chatclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ClientController {
	
	private final ChatClient client;
	@FXML
	private ListView<String> clientList;
	@FXML
	private HBox loginBox;
	@FXML
	private TextField loginField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private Button authButton;
	@FXML
	private HBox messageBox;
	@FXML
	private TextArea messageArea;
	@FXML
	private TextField textField;
	@FXML
	private Button sendButton;
	
	public ClientController() {
		this.client = new ChatClient(this);
		try {
			client.openConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void authButtonClick(ActionEvent actionEvent) {
		client.sendMessage(Command.AUTH, loginField.getText(), passwordField.getText());
	}
	
	public void sendButtonClick(ActionEvent actionEvent) {
		final String text = textField.getText();
		if (text.trim().isEmpty()) {
			return;
		}
		client.sendMessage(text);
		textField.clear();
		textField.requestFocus();
	}
	
	public void addMessage(String message) {
		messageArea.appendText(message + "\n");
	}
	
	public void setAuth(boolean isSuccess) {
		loginBox.setVisible(! isSuccess);
		messageBox.setVisible(isSuccess);
	}
	
	public void showError(String[] error) {
		final Alert alert = new Alert(Alert.AlertType.ERROR, error[0], new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
		alert.setTitle("Ошибка");
		alert.showAndWait();
	}
	
	public void selectClient(MouseEvent mouseEvent) {
		if (mouseEvent.getClickCount() == 2) { // /w nick1 private message
			final String message = textField.getText();
			final String nick = clientList.getSelectionModel().getSelectedItem();
			textField.setText(Command.PRIVATE_MESSAGE.collectMessage(nick, message));
			textField.requestFocus();
			textField.selectEnd();
		}
	}
	
	public void updateClientList(String[] params) {
		clientList.getItems().clear();
		clientList.getItems().addAll(params);
	}
}