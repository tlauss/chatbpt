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
import java.time.LocalDateTime;
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

    public void setProgramController(ProgramController programController) {
        this.programController = programController;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Client View");

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

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
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
                    return;
                }

                if (chatroom.getBannedUsers().contains(programController.getLoggedInUser())) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Fehler");
                    alert.setHeaderText("Sie können keine Nachrichten in diesen Chatroom senden");
                    alert.setContentText("Sie wurden aus diesem Chatroom verbannt");
                    alert.showAndWait();
                    return;
                }

                if (selectedChat != null) {
                    String text = inputField.getText().trim();
                    if (!text.isEmpty()) {
                        LocalDateTime now = LocalDateTime.now();
                        Message message = new Message(programController.getLoggedInUser(), text, now);
                        try {
                            programController.getServer().addMessage(selectedChat, message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
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

    @Override
    public void updateMessagesOnNewMessage(Chatroom chatroom, Message message) {
        Platform.runLater(() -> {
            if (!chatroom.getBannedUsers().contains(programController.getLoggedInUser())) {
                if (!chatroom.getUsers().contains(programController.getLoggedInUser())) {
                    return;
                }
                ListView<Message> messageArea = getOrCreateMessageArea(chatroom.getName());
                messageArea.getItems().add(message);
                messageArea.scrollTo(messageArea.getItems().size() - 1);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fehler");
                alert.setHeaderText("Sie können keine Nachrichten in diesen Chatroom senden");
                alert.setContentText("Sie wurden aus diesem Chatroom verbannt");
                alert.showAndWait();
            }
        });

    }

    @Override
    public void updateChatRoomsOnJoin(Chatroom chatroom, User user) throws RemoteException {
        Platform.runLater(() -> {
            if (chatroom.getUsers().contains(programController.getLoggedInUser())) {
                systemMessageList.getItems().add(user.getName() + " ist dem Chatroom " + chatroom.getName() + " beigetreten");
            }
        });
    }

    @Override
    public void updateChatroomsOnBan(Chatroom chatroom, User user) throws RemoteException {
        Platform.runLater(() -> {
            if (chatroom.getUsers().contains(programController.getLoggedInUser())) {
                systemMessageList.getItems().add(user.getName() + " wurde aus dem Chatroom " + chatroom.getName() + " verbannt");
            }
        });
    }

    @Override
    public void updateChatroomsOnUnban(Chatroom chatroom, User user) throws RemoteException {
        Platform.runLater(() -> {
            if (chatroom.getUsers().contains(programController.getLoggedInUser())) {
                systemMessageList.getItems().add(user.getName() + " wurde im Chatroom " + chatroom.getName() + " entbannt");
            }
        });
    }

    @Override
    public void updateChatroomsOnLeave(Chatroom chatroom, User user) throws RemoteException {
        Platform.runLater(() -> {
            if (chatroom.getUsers().contains(programController.getLoggedInUser())) {
                systemMessageList.getItems().add(user.getName() + " hat den Chatroom " + chatroom.getName() + " verlassen");
            }
        });
    }

    @Override
    public void updateChatroomsOnRemove(Chatroom chatroom) throws RemoteException {
        Platform.runLater(() -> {
            if (chatroom.getUsers().contains(programController.getLoggedInUser())) {
                systemMessageList.getItems().add("Der Chatroom " + chatroom.getName() + " wurde gelöscht");
            }
        });
    }

    @Override
    public void updateChatListAndMessageArea(String userName, String chatroomName) throws RemoteException {
        Platform.runLater(() -> {
            if (userName.equals(programController.getLoggedInUser().getName())) {
                chatList.getItems().add(chatroomName);
                chatList.getSelectionModel().select(chatroomName);
                ListView<Message> messageArea = getOrCreateMessageArea(chatroomName);
                try {
                    Chatroom chatroom = programController.getServer().getChatroom(chatroomName);
                    for (Message message : chatroom.getMessages()) {
                        messageArea.getItems().add(message);
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
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
                    chatList.getItems().add(chatroom.getName());

                    ListView<Message> messageArea = getOrCreateMessageArea(chatroom.getName());
                    for (Message message : chatroom.getMessages()) {
                        messageArea.getItems().add(message);
                    }
                }
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private MenuBar createMenuBar() {
        Menu chatRoomMenu = new Menu("Chat-Raum");
        Menu chatPrivateMenu = new Menu("Privat-Chat");
        Menu testNotificationMenu = new Menu("Test-Benachrichtigung");

        MenuItem newChatRoomItem = new MenuItem("Neuer Chatraum");
        newChatRoomItem.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Neuer Chatraum");
            dialog.setHeaderText("Chatraum erstellen");
            dialog.setContentText("Geben Sie den Namen des Chatraums ein:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(roomName -> {
                Chatroom chatroom = new Chatroom(roomName, programController.getLoggedInUser());
                try {
                    programController.getServer().addChatroom(chatroom);
                    programController.getServer().addUserToChatroom(programController.getLoggedInUser(), roomName);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                chatList.getItems().add(roomName);
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
                            if (programController.getServer().getChatRoomUsers(roomName).contains(programController.getLoggedInUser())) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Chatraum beitreten");
                                alert.setHeaderText("Chatraum beitreten");
                                alert.setContentText("Sie sind bereits in diesem Chatraum");

                                alert.showAndWait();
                            } else {
                                programController.getServer().addUserToChatroom(programController.getLoggedInUser(), roomName);
                                chatList.getItems().add(roomName);
                                chatList.getSelectionModel().select(roomName);
                                ListView<Message> messageArea = getOrCreateMessageArea(roomName);

                                for (Message message : programController.getServer().getChatroom(roomName).getMessages()) {
                                    messageArea.getItems().add(message);
                                }
                                messageArea.scrollTo(messageArea.getItems().size() - 1);
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
                                " und " + programController.getLoggedInUser().getName()))
                            {
                                String chatroomName = "Privat: " + programController.getLoggedInUser().getName() +
                                        " und " + userName;
                                Chatroom chatroom = new Chatroom(chatroomName, programController.getLoggedInUser());
                                programController.getServer().addChatroom(chatroom);
                                programController.getServer().addUserToChatroom(programController.getLoggedInUser(), chatroom.getName());
                                programController.getServer().addUserToChatroom(programController.getServer().getUser(userName), chatroom.getName());

                                chatList.getItems().add(chatroomName);
                                chatList.getSelectionModel().select(chatroomName);

                                ListView<Message> messageArea = getOrCreateMessageArea(chatroomName);
                                for (Message message : chatroom.getMessages()) {
                                    messageArea.getItems().add(message);
                                }
                                messageArea.scrollTo(messageArea.getItems().size() - 1);

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

        MenuItem showNotificationItem = new MenuItem("Benachrichtigung anzeigen");
        testNotificationMenu.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Benachrichtigung");
            alert.setHeaderText("Benachrichtigung");
            alert.setContentText("Dies ist eine Benachrichtigung");

            alert.showAndWait();
        });

        chatRoomMenu.getItems().addAll(newChatRoomItem, joinChatRoomItem);
        chatPrivateMenu.getItems().addAll(newPrivateChatItem);
        testNotificationMenu.getItems().addAll(showNotificationItem);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(chatRoomMenu, chatPrivateMenu, testNotificationMenu);

        return menuBar;
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
                    programController.getServer().removeChatRoom(selectedChatToRemove);

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
                e.printStackTrace();
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
                        boolean userExists = false;
                        for (User user : chatroom.getUsers()) {
                            if (user.getName().equals(userName)) {
                                if (chatroom.isBanned(userName)) {
                                    programController.getServer().unbanUserFromChatroom(userToBan, selectedChat);
                                    systemMessageList.getItems().add(userName + " wurde aus dem Chatraum " + selectedChat + " entbannt");
                                } else {
                                    programController.getServer().banUserFromChatroom(userToBan, selectedChat);
                                    systemMessageList.getItems().add(userName + " wurde aus dem Chatraum " + selectedChat + " verbannt");
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
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Unban/Ban User");
                        alert.setHeaderText("Unban/Ban User");
                        alert.setContentText("Sie sind nicht der Besitzer des Chatraums");
                        alert.showAndWait();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        });
        contextMenu.getItems().add(banUser);

        MenuItem leaveChat = new MenuItem("Leave Chat");
        leaveChat.setOnAction(event -> {
            String selectedChat = chatList.getSelectionModel().getSelectedItem();
            try {
                programController.getServer().removeUserFromChatroom(programController.getLoggedInUser(), selectedChat);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            chatList.getItems().remove(selectedChat);
            chatMessageAreas.remove(selectedChat);
        });

        contextMenu.getItems().add(leaveChat);

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
}