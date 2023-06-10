package swe4.gui.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class Chatroom implements Serializable {
    private final String name;
    private final ArrayList<User> users;
    private final ArrayList<User> bannedUsers;
    private final ArrayList<Message> messages;
    private final User owner;

    @Serial
    private static final long serialVersionUID = -7875509922081022453L;

    public Chatroom(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.users = new ArrayList<>();
        this.bannedUsers = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public ArrayList<User> getBannedUsers() {
        return bannedUsers;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public User getOwner() {
        return owner;
    }

    @Override
    public String toString() {
            return "Chatroom{" +
                    "name='" + name + '\'' +
                    ", users=" + users +
                    ", bannedUsers=" + bannedUsers +
                    ", messages=" + messages +
                    ", owner=" + owner +
                    '}';
    }

    public boolean isBanned(String userName) {
        for (User user : bannedUsers) {
            if (user.getName().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    public void unbanUser(String userName) {
        for (User user : bannedUsers) {
            if (user.getName().equals(userName)) {
                bannedUsers.remove(user);
                return;
            }
        }
    }

    public void banUser(String userName) {
        for (User user : users) {
            if (user.getName().equals(userName)) {
                bannedUsers.add(user);
                return;
            }
        }
    }

    public void removeUser(User loggedInUser) {
        for (User user : users) {
            if (user.getName().equals(loggedInUser.getName())) {
                users.remove(user);
                return;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Chatroom chatroom) {
            return chatroom.getName().equals(this.getName());
        }
        return false;
    }
}
