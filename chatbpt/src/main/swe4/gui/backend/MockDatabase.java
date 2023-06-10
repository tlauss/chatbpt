package swe4.gui.backend;

import swe4.gui.model.Chatroom;
import swe4.gui.model.Message;
import swe4.gui.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MockDatabase {
    private static MockDatabase instance = null;
    private final List<User> users;
    private final List<Chatroom> chatrooms;

    private MockDatabase() {
        User testUser1 = new User("John Doe", "password", "John");
        User testUser2 = new User("Jane Doe", "password", "Jane");
        User admin = new User("a", "a", "admin");
        User b = new User("b", "b", "b");

        users = new ArrayList<>();
        users.add(testUser1);
        users.add(testUser2);
        users.add(admin);
        users.add(b);

        Chatroom testChatroom1 = new Chatroom("Test Chatroom 1", admin);
        Chatroom testChatroom2 = new Chatroom("Test Chatroom 2", testUser2);
        Chatroom testChatroom3 = new Chatroom("Test Chatroom 3", b);

        chatrooms = new ArrayList<>();
        chatrooms.add(testChatroom1);
        chatrooms.add(testChatroom2);
        chatrooms.add(testChatroom3);

        testChatroom1.getUsers().add(testUser1);
        testChatroom1.getUsers().add(testUser2);
        testChatroom1.getUsers().add(admin);

        testChatroom2.getUsers().add(testUser2);

        testChatroom3.getUsers().add(b);

        testChatroom1.getMessages().add(new Message(testUser1, "Hello World!", LocalDateTime.now()));
        testChatroom1.getMessages().add(new Message(testUser2, "Hello Mars!", LocalDateTime.now()));
        testChatroom1.getMessages().add(new Message(testUser1, "Hello Jupyter!", LocalDateTime.now()));

        testChatroom2.getMessages().add(new Message(testUser2, "Hello World!", LocalDateTime.now()));
        testChatroom2.getMessages().add(new Message(admin, "Hello Admin!", LocalDateTime.now()));
    }

    public static MockDatabase getInstance() {
        if (instance == null) {
            instance = new MockDatabase();
        }
        return instance;
    }

    public List<User> getUsers() {
        return users;
    }

    public void addUser(String username, String password, String shortname) {
        User user = new User(username, password, shortname);
        users.add(user);
    }

    public User getUser(String name) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    public boolean userExists(String name) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean passwordCorrect(String name, String password) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return user.getPassword().equals(password);
            }
        }
        return false;
    }

    public List<Chatroom> getChatrooms() {
        return chatrooms;
    }
    public void addChatroom(Chatroom chatroom) {
        chatrooms.add(chatroom);
    }

    public Chatroom getChatroom(String name) {
        return chatrooms.stream().filter(chatroom -> chatroom.getName().equals(name)).findFirst().orElse(null);
    }
    public void removeChatroom(Chatroom chatroom) {
        chatrooms.remove(chatroom);
        chatroom.getOwner().getChatrooms().remove(chatroom);
    }

    public boolean chatroomExists(String roomName) {
        for (Chatroom chatroom : chatrooms) {
            if (chatroom.getName().equals(roomName)) {
                return true;
            }
        }
        return false;
    }
}
