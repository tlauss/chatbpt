package swe4.gui.database;

import swe4.gui.model.Chatroom;
import swe4.gui.model.Message;
import swe4.gui.model.User;

import java.sql.*;
import java.util.ArrayList;

public class Database implements DatabaseService {
    private Connection connection;
    private final String connectionString;
    private final String userName;

    public Database(String connectionString, String userName) {
        this.connectionString = connectionString;
        this.userName = userName;
    }

    public Connection getConnection() {
        try {
            if (connection == null) {
                connection = DriverManager.getConnection(connectionString, userName, null);
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addUser(String username, String password, String shortname) {
        try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO User (name, password, shortname) VALUES (?, ?, ?)")) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, shortname);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User getUser(String username) {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM User WHERE name = ?")) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");
                String shortname = resultSet.getString("shortname");
                return new User(name, password, shortname);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User getUser(int id) {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM User WHERE uid = ?")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");
                String shortname = resultSet.getString("shortname");
                return new User(name, password, shortname);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean userExists(String user) {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM User WHERE name = ?")) {
            statement.setString(1, user);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean passwordCorrect(String user, String password) {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM User WHERE name = ? AND password = ?")) {
            statement.setString(1, user);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ArrayList<Chatroom> getChatrooms() {
        try (Statement statement = getConnection().createStatement()) {
            ArrayList<Chatroom> chatrooms = new ArrayList<>();
            statement.execute("SELECT * FROM Chatroom");
            ResultSet resultSet = statement.getResultSet();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                User owner = getUserFromId(resultSet.getInt("owner_id"));

                ArrayList<User> users = getUsersFromChatroom(name);
                ArrayList<User> bannedUsers = getBannedUsersFromChatroom(name);
                ArrayList<Message> messages = getMessagesFromChatroom(name);

                chatrooms.add(new Chatroom(name, owner, users, bannedUsers, messages));
            }
            return chatrooms;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addChatroom(Chatroom chatroom, String user) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "INSERT INTO Chatroom (name, owner_id) VALUES (?, ?)")) {
            statement.setString(1, chatroom.getName());
            statement.setString(2, getUserId(getUser(user)));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Chatroom getChatroom(String roomName) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "SELECT * FROM Chatroom WHERE name = ?")) {
            statement.setString(1, roomName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                int ownerId = resultSet.getInt("owner_id");
                User owner = getUser(ownerId);
                return new Chatroom(name, owner);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeChatroom(Chatroom chatroom) {
        // Remove banned users from the chatroom
        try (PreparedStatement deleteBannedUsers = connection.prepareStatement(
                "DELETE FROM BannedUser_Chatroom WHERE chatroom_name = ?")) {
            deleteBannedUsers.setString(1, chatroom.getName());
            deleteBannedUsers.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Remove user-chatroom associations
        try (PreparedStatement deleteUserChatroom = connection.prepareStatement(
                "DELETE FROM User_Chatroom WHERE chatroom_name = ?")) {
            deleteUserChatroom.setString(1, chatroom.getName());
            deleteUserChatroom.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Remove messages from the chatroom
        try (PreparedStatement deleteMessages = connection.prepareStatement(
                "DELETE FROM Message WHERE chatroom_name = ?")) {
            deleteMessages.setString(1, chatroom.getName());
            deleteMessages.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Remove the chatroom
        try (PreparedStatement deleteChatroom = connection.prepareStatement(
                "DELETE FROM Chatroom WHERE name = ?")) {
            deleteChatroom.setString(1, chatroom.getName());
            deleteChatroom.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean chatroomExists(String roomName) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "SELECT * FROM Chatroom WHERE name = ?")) {
            statement.setString(1, roomName);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addUserToChatroom(User user, String chatroom) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "INSERT INTO User_Chatroom (user_id, chatroom_name) VALUES (?, ?)")) {
            statement.setString(1, getUserId(user));
            statement.setString(2, chatroom);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addMessage(String selectedChat, Message message) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "INSERT INTO Message (user_id, chatroom_name, text, timestamp) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, getUserId(message.getSender()));
            statement.setString(2, selectedChat);
            statement.setString(3, message.getText());
            statement.setTimestamp(4, message.getTimestamp());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void banUserFromChatroom(User user, String chatroom) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "INSERT INTO BannedUser_Chatroom (user_id, chatroom_name) VALUES (?, ?)")) {
            statement.setString(1, getUserId(user));
            statement.setString(2, chatroom);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unbanUserFromChatroom(User user, String chatroom) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "DELETE FROM BannedUser_Chatroom WHERE user_id = ? AND chatroom_name = ?")) {
            statement.setString(1, getUserId(user));
            statement.setString(2, chatroom);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ArrayList<User> getChatroomUsers(String chatroom) {
        ArrayList<User> users = new ArrayList<>();
        users.addAll(getUsersFromChatroom(chatroom));
        users.addAll(getBannedUsersFromChatroom(chatroom));
        return users;
    }

    @Override
    public ArrayList<Message> getMessages(String chatroom) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "SELECT * FROM Message WHERE chatroom_name = ?")) {
            statement.setString(1, chatroom);
            ResultSet resultSet = statement.executeQuery();

            ArrayList<Message> messages = new ArrayList<>();
            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                User sender = getUser(userId);
                String text = resultSet.getString("text");
                Timestamp timestamp = resultSet.getTimestamp("timestamp");
                messages.add(new Message(sender, text, timestamp));
            }
            return messages;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUserId(User user) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "SELECT * FROM User WHERE name = ?")) {
            statement.setString(1, user.getName());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("uid");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<User> getUsersFromResultSet(ResultSet resultSet) {
        ArrayList<User> users = new ArrayList<>();
        try {
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");
                String shortname = resultSet.getString("shortname");
                users.add(new User(name, password, shortname));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    private ArrayList<User> getUsersFromChatroom(String name) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "SELECT * " +
                        "FROM User " +
                        "INNER JOIN User_Chatroom ON User.uid = User_Chatroom.user_id " +
                        "INNER JOIN Chatroom ON User_Chatroom.chatroom_name = Chatroom.name " +
                    "WHERE Chatroom.name = ?"))
        {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();

            return getUsersFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public ArrayList<User> getBannedUsersFromChatroom(String name) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "SELECT * " +
                        "FROM User " +
                        "INNER JOIN BannedUser_Chatroom ON User.uid = BannedUser_Chatroom.user_id " +
                        "INNER JOIN Chatroom ON BannedUser_Chatroom.chatroom_name = Chatroom.name " +
                        "WHERE Chatroom.name = ?"))
        {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();

            return getUsersFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<Message> getMessagesFromChatroom(String name) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "SELECT * " +
                        "FROM Message " +
                        "INNER JOIN User ON Message.user_id = User.uid " +
                        "INNER JOIN Chatroom ON Message.chatroom_name = Chatroom.name " +
                        "WHERE Chatroom.name = ?"))
        {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();

            ArrayList<Message> messages = new ArrayList<>();

            while (resultSet.next()) {
                User user = new User(resultSet.getString("name"),
                        resultSet.getString("password"),
                        resultSet.getString("shortname"));
                Message message = new Message(user,
                        resultSet.getString("text"),
                        resultSet.getTimestamp("timestamp"));

                messages.add(message);
            }

            return messages;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User getUserFromId(int ownerId) {
        try (PreparedStatement statement = getConnection().prepareStatement(
                "SELECT * FROM User WHERE uid = ?")) {
            statement.setInt(1, ownerId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new User(resultSet.getString("name"),
                        resultSet.getString("password"),
                        resultSet.getString("shortname"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
