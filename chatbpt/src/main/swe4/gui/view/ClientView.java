package swe4.gui.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import swe4.gui.controller.ProgramController;
import swe4.gui.model.Chatroom;
import swe4.gui.model.Message;
import swe4.gui.model.User;
import swe4.gui.observer.ClientObserver;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientView extends Application implements ClientObserver {
    private Map<String, ListView<Message>> chatMessageAreas;
    private ListView<String> chatList;
    private TextField searchField;
    private ListView<String> searchResultList;
    private ListView<String> systemMessageList;
    transient private ProgramController programController;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    public void setProgramController(ProgramController programController) {
        this.programController = programController;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Client View: " + programController.getLoggedInUser().getName());

        MenuBar menuBar = createMenuBar();
        chatList = createChatList();
        VBox chatVBox = new VBox(10);
        chatMessageAreas = new HashMap<>();

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(chatList);
        root.setCenter(chatVBox);

        searchField = createSearchField();
        searchResultList = createSearchResultList();

        VBox searchBox = new VBox(10);
        searchBox.getChildren().addAll(searchField, searchResultList);
        root.setRight(searchBox);

        systemMessageList = createSystemMessageList();

        VBox systemMessageBox = new VBox(10);
        systemMessageBox.getChildren().add(systemMessageList);
        root.setBottom(systemMessageList);

        TextField inputField = createInputField();
        Button sendButton = createSendButton();
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.getChildren().addAll(inputField, sendButton);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setMinWidth(WINDOW_WIDTH);
        primaryStage.setMinHeight(WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            programController.logout();
            Platform.exit();
            System.exit(0);
        });

        chatList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            chatVBox.getChildren().clear();

            if (newValue != null) {
                ListView<Message> messageArea = getOrCreateMessageArea(newValue);
                chatVBox.getChildren().add(messageArea);
                chatVBox.getChildren().add(inputBox);
            }
        });

        createInitialChatListsAndMessages();

        searchField.setOnAction(event -> {
            String query = searchField.getText().trim();
            searchMessages(query);
        });

        sendButton.setOnAction(event -> {
            try {
                String selectedChat = chatList.getSelectionModel().getSelectedItem();
                Chatroom chatroom = programController.getServer().getChatroom(selectedChat);

                if (chatroom == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Fehler");
                    alert.setHeaderText("Es wurde kein Chatroom ausgewählt");
                    alert.setContentText("Sie können keine Nachrichten senden, wenn kein Chatroom ausgewählt ist");
                    alert.showAndWait();
                } else if (programController.getServer().getBannedUsersFromChatroom(selectedChat).contains(programController.getLoggedInUser())) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Fehler");
                    alert.setHeaderText("Sie können keine Nachrichten in diesen Chatroom senden");
                    alert.setContentText("Sie wurden aus diesem Chatroom verbannt");
                    alert.showAndWait();
                } else if (selectedChat != null) {
                    String text = inputField.getText().trim();
                    if (!text.isEmpty()) {
                        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                        Message message = new Message(programController.getLoggedInUser(), text, now);
                        try {
                            programController.getServer().addMessage(selectedChat, message);
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }

                        inputField.clear();
                    }
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private ListView<String> createSystemMessageList() {
        systemMessageList = new ListView<>();
        systemMessageList.setMaxHeight(75);
        systemMessageList.setPlaceholder(new Label("Keine Nachrichten vorhanden"));
        return systemMessageList;
    }

    private void createInitialChatListsAndMessages() {
        try {
            for (Chatroom chatroom : programController.getServer().getChatrooms()) {
                if (chatroom.getUsers().contains(programController.getLoggedInUser())) {
                    addChatroomAndMessagesToView(chatroom.getName());
                }
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private MenuBar createMenuBar() {
        Menu chatRoomMenu = new Menu("Chat-Raum");
        Menu chatPrivateMenu = new Menu("Privat-Chat");

        MenuItem newChatRoomItem = new MenuItem("Neuer Chatraum");
        newChatRoomItem.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Neuer Chatraum");
            dialog.setHeaderText("Chatraum erstellen");
            dialog.setContentText("Geben Sie den Namen des Chatraums ein:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(roomName -> {
                if (roomName.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Fehler");
                    alert.setHeaderText("Es wurde kein Name eingegeben");
                    alert.setContentText("Sie müssen einen Namen für den Chatraum eingeben");
                    alert.showAndWait();
                } else if (roomName.contains("Privat")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Fehler");
                    alert.setHeaderText("Der Name des Chatraums darf nicht 'Privat' enthalten");
                    alert.setContentText("Sie müssen einen anderen Namen für den Chatraum eingeben");
                    alert.showAndWait();
                } else {
                    try {
                        if (programController.getServer().chatroomExists(roomName)) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Fehler");
                            alert.setHeaderText("Der Chatraum existiert bereits");
                            alert.setContentText("Sie müssen einen anderen Namen für den Chatraum eingeben");
                            alert.showAndWait();
                        } else {
                            Chatroom chatroom = new Chatroom(roomName, programController.getLoggedInUser());
                            try {
                                programController.getServer().addChatroom(chatroom, programController.getLoggedInUser().getName());
                                programController.getServer().addUserToChatroom(programController.getLoggedInUser(), roomName);
                            } catch (RemoteException e) {
                                throw new RuntimeException(e);
                            }
                            chatList.getItems().add(roomName);
                        }
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });

        MenuItem joinChatRoomItem = new MenuItem("Chatraum beitreten");
        joinChatRoomItem.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Chatraum beitreten");
            dialog.setHeaderText("Chatraum beitreten");
            dialog.setContentText("Geben Sie den Namen des Chatraums ein:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(roomName -> {
                try {
                    if (roomName.contains("Privat")) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Chatraum beitreten");
                        alert.setHeaderText("Chatraum beitreten");
                        alert.setContentText("Sie können keinem privaten Chatraum beitreten");

                        alert.showAndWait();
                    } else {
                        if (programController.getServer().chatroomExists(roomName)) {
                            if (programController.getServer().getChatroomUsers(roomName).contains(programController.getLoggedInUser())) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Chatraum beitreten");
                                alert.setHeaderText("Chatraum beitreten");
                                alert.setContentText("Sie sind bereits in diesem Chatraum");

                                alert.showAndWait();
                            } else {
                                programController.getServer().addUserToChatroom(programController.getLoggedInUser(), roomName);
                                addChatroomAndMessagesToView(roomName);
                            }
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Chatraum beitreten");
                            alert.setHeaderText("Chatraum beitreten");
                            alert.setContentText("Der Chatraum existiert nicht");

                            alert.showAndWait();
                        }
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        MenuItem newPrivateChatItem = new MenuItem("Neuer Privatchat");
        newPrivateChatItem.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Neuer Privatchat");
            dialog.setHeaderText("Privatchat erstellen");
            dialog.setContentText("Geben Sie den Namen des Chatpartners ein:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(userName -> {
                try {
                    if (programController.getServer().userExists(userName)) {
                        if (!userName.equals(programController.getLoggedInUser().getName())) {
                            if (!programController.getServer().chatroomExists(
                                    "Privat: "
                                            + programController.getLoggedInUser().getName() +
                                            " und " + userName)
                                    && !programController.getServer().chatroomExists(
                                    "Privat: "
                                            + userName +
                                            " und " + programController.getLoggedInUser().getName())) {
                                String chatroomName = "Privat: " + programController.getLoggedInUser().getName() +
                                        " und " + userName;
                                Chatroom chatroom = new Chatroom(chatroomName, programController.getLoggedInUser());
                                programController.getServer().addChatroom(chatroom, userName);
                                programController.getServer().addUserToChatroom(programController.getLoggedInUser(), chatroom.getName());
                                programController.getServer().addUserToChatroom(programController.getServer().getUser(userName), chatroom.getName());

                                addChatroomAndMessagesToView(chatroomName);

                                programController.getServer().updateChatListAndMessageArea(userName, chatroom.getName());
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Privatchat erstellen");
                                alert.setHeaderText("Privatchat erstellen");
                                alert.setContentText("Sie haben bereits einen Privatchat mit diesem Benutzer");

                                alert.showAndWait();
                            }
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Privatchat erstellen");
                            alert.setHeaderText("Privatchat erstellen");
                            alert.setContentText("Sie können keinen Privatchat mit sich selbst erstellen");

                            alert.showAndWait();
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Privatchat erstellen");
                        alert.setHeaderText("Privatchat erstellen");
                        alert.setContentText("Der Benutzer existiert nicht");

                        alert.showAndWait();
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        chatRoomMenu.getItems().addAll(newChatRoomItem, joinChatRoomItem);
        chatPrivateMenu.getItems().addAll(newPrivateChatItem);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(chatRoomMenu, chatPrivateMenu);

        return menuBar;
    }

    private void addChatroomAndMessagesToView(String chatroomName) throws RemoteException {
        chatList.getItems().add(chatroomName);
        chatList.getSelectionModel().select(chatroomName);

        addMessagesToView(chatroomName);
    }

    private void addMessagesToView(String chatroomName) throws RemoteException {
        ListView<Message> messageArea = getOrCreateMessageArea(chatroomName);
        for (Message message : programController.getServer().getMessages(chatroomName)) {
            messageArea.getItems().add(message);
        }
        messageArea.scrollTo(messageArea.getItems().size() - 1);
    }

    private ListView<String> createChatList() {
        ListView<String> chatList = new ListView<>();
        chatList.setPrefWidth(200);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            try {
                String selectedChat = chatList.getSelectionModel().getSelectedItem();
                Chatroom selectedChatToRemove = programController.getServer().getChatroom(selectedChat);

                if (selectedChatToRemove.getOwner().equals(programController.getLoggedInUser())) {
                    programController.getServer().removeChatroom(selectedChatToRemove);

                    chatList.getItems().remove(selectedChat);
                    chatMessageAreas.remove(selectedChat);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Chatraum löschen");
                    alert.setHeaderText("Chatraum löschen");
                    alert.setContentText("Sie sind nicht der Besitzer des Chatraums");

                    alert.showAndWait();
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

        });
        contextMenu.getItems().add(deleteItem);

        MenuItem banUser = new MenuItem("Unban/Ban User");
        banUser.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Unban/Ban User");
            dialog.setHeaderText("Unban/Ban User");
            dialog.setContentText("Geben Sie den Namen des Users ein:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(userName -> {
                try {
                    String selectedChat = chatList.getSelectionModel().getSelectedItem();
                    Chatroom chatroom = programController.getServer().getChatroom(selectedChat);
                    User userToBan = programController.getServer().getUser(userName);

                    if (chatroom.getOwner().equals(programController.getLoggedInUser())) {
                        if (chatroom.getOwner().equals(programController.getServer().getUser(userName))) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Unban/Ban User");
                            alert.setHeaderText("Unban/Ban User");
                            alert.setContentText("Sie können sich nicht selbst bannen");

                            alert.showAndWait();
                        } else {
                            boolean userExists = false;

                            for (User user : programController.getServer().getChatroomUsers(selectedChat)) {
                                if (user.getName().equals(userName)) {
                                    if (programController.getServer().getBannedUsersFromChatroom(selectedChat).contains(user)) {
                                        programController.getServer().unbanUserFromChatroom(userToBan, selectedChat);
                                    } else {
                                        programController.getServer().banUserFromChatroom(userToBan, selectedChat);
                                    }
                                    userExists = true;
                                    break;
                                }
                            }

                            if (!userExists) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Unban/Ban User");
                                alert.setHeaderText("Unban/Ban User");
                                alert.setContentText("Der Benutzer existiert nicht");
                                alert.showAndWait();
                            }
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Unban/Ban User");
                        alert.setHeaderText("Unban/Ban User");
                        alert.setContentText("Sie sind nicht der Besitzer des Chatraums");
                        alert.showAndWait();
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        contextMenu.getItems().add(banUser);

        chatList.setContextMenu(contextMenu);

        return chatList;
    }

    private ListView<Message> getOrCreateMessageArea(String chat) {
        ListView<Message> messageArea = chatMessageAreas.get(chat);
        if (messageArea == null) {
            messageArea = createMessageArea();
            chatMessageAreas.put(chat, messageArea);
        }
        return messageArea;
    }

    private ListView<Message> createMessageArea() {
        ListView<Message> messageArea = new ListView<>();
        messageArea.setPrefHeight(400);
        messageArea.setCellFactory(listView -> new MessageListCell());
        return messageArea;
    }

    private TextField createInputField() {
        TextField inputField = new TextField();
        inputField.setPromptText("Type your message...");
        inputField.setPrefWidth(300);
        return inputField;
    }

    private Button createSendButton() {
        Button sendButton = new Button("Send");
        sendButton.setPrefWidth(100);
        return sendButton;
    }

    private TextField createSearchField() {
        TextField searchField = new TextField();
        searchField.setPromptText("Search messages...");
        searchField.setPrefWidth(250);
        return searchField;
    }

    private ListView<String> createSearchResultList() {
        ListView<String> searchResultList = new ListView<>();
        searchResultList.setPrefWidth(250);
        return searchResultList;
    }

    private void searchMessages(String query) {
        searchResultList.getItems().clear();

        for (Map.Entry<String, ListView<Message>> entry : chatMessageAreas.entrySet()) {
            String chat = entry.getKey();
            ListView<Message> messageArea = entry.getValue();
            ObservableList<Message> messages = messageArea.getItems();

            for (Message message : messages) {
                if (message.getText().contains(query)) {
                    searchResultList.getItems().add(chat + ": " + message);
                }
            }
        }
    }

    private class MessageListCell extends ListCell<Message> {
        @Override
        protected void updateItem(Message message, boolean empty) {
            super.updateItem(message, empty);

            if (empty || message == null) {
                setText(null);
                setGraphic(null);
            } else {
                String sender = message.getSender().getName();
                String time = message.getFormattedTime();
                String text = message.getText();

                Label senderLabel = new Label(sender);
                senderLabel.setStyle("-fx-font-weight: bold");

                Label timeLabel = new Label(time);
                timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray");

                Label textLabel = new Label(text);
                textLabel.setWrapText(true);

                VBox vBox = new VBox(5);
                vBox.getChildren().addAll(senderLabel, timeLabel, textLabel);

                if (message.getSender().equals(programController.getLoggedInUser())) {
                    vBox.setAlignment(Pos.CENTER_RIGHT);
                    setStyle("-fx-background-color: #56ace1; -fx-padding: 5px; -fx-background-radius: 10px; -fx-border-radius: 10px;");
                } else {
                    vBox.setAlignment(Pos.CENTER_LEFT);
                    setStyle("-fx-background-color: #84e572; -fx-padding: 5px; -fx-background-radius: 10px; -fx-border-radius: 10px;");
                }

                setGraphic(vBox);
            }
        }
    }


    @Override
    public void updateMessagesOnNewMessage(Chatroom chatroom, Message message) {
        Platform.runLater(() -> {
            try {
                if (!programController.getServer().getChatroomUsers(chatroom.getName()).contains(programController.getLoggedInUser())) {
                    return;
                }
                ListView<Message> messageArea = getOrCreateMessageArea(chatroom.getName());
                messageArea.getItems().add(message);
                messageArea.scrollTo(messageArea.getItems().size() - 1);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void updateChatRoomsOnJoin(Chatroom chatroom, User user) throws RemoteException {
        Platform.runLater(() -> {
            try {
                if (programController.getServer().getChatroomUsers(chatroom.getName()).contains(programController.getLoggedInUser())) {
                    if (programController.getLoggedInUser().equals(user)) {
                        systemMessageList.getItems().add("Du bist dem Chatroom " + chatroom.getName() + " beigetreten");
                    } else {
                        systemMessageList.getItems().add(user.getName() + " ist dem Chatroom " + chatroom.getName() + " beigetreten");
                    }
                    systemMessageList.scrollTo(systemMessageList.getItems().size() - 1);
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void updateChatroomsOnBan(Chatroom chatroom, User user) throws RemoteException {
        Platform.runLater(() -> {
            try {
                if (programController.getServer().getChatroomUsers(chatroom.getName()).contains(programController.getLoggedInUser())) {
                    if (programController.getLoggedInUser().equals(user)) {
                        systemMessageList.getItems().add("Du wurdest aus dem Chatroom " + chatroom.getName() + " verbannt");
                    } else {
                        systemMessageList.getItems().add(user.getName() + " wurde aus dem Chatroom " + chatroom.getName() + " verbannt");
                    }
                    systemMessageList.scrollTo(systemMessageList.getItems().size() - 1);
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void updateChatroomsOnUnban(Chatroom chatroom, User user) throws RemoteException {
        Platform.runLater(() -> {
            try {
                if (programController.getServer().getChatroomUsers(chatroom.getName()).contains(programController.getLoggedInUser())) {
                    if (programController.getLoggedInUser().equals(user)) {
                        systemMessageList.getItems().add("Du wurdest aus dem Chatroom " + chatroom.getName() + " entbannt");
                    } else {
                        systemMessageList.getItems().add(user.getName() + " wurde aus dem Chatroom " + chatroom.getName() + " entbannt");
                    }
                    systemMessageList.scrollTo(systemMessageList.getItems().size() - 1);
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void updateChatroomsOnRemove(Chatroom chatroom, ArrayList<User> users) throws RemoteException {
        Platform.runLater(() -> {
            if (users.contains(programController.getLoggedInUser())) {
                chatMessageAreas.remove(chatroom.getName());
                chatList.getItems().remove(chatroom.getName());
                systemMessageList.getItems().add("Der Chatroom " + chatroom.getName() + " wurde gelöscht");
                systemMessageList.scrollTo(systemMessageList.getItems().size() - 1);
            }
        });
    }

    @Override
    public void updateChatListAndMessageArea(String userName, String chatroomName) throws RemoteException {
        Platform.runLater(() -> {
            if (userName.equals(programController.getLoggedInUser().getName())) {
                try {
                    addChatroomAndMessagesToView(chatroomName);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}