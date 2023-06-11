package swe4.gui.server;

import swe4.gui.database.Database;
import swe4.gui.database.DatabaseService;
import swe4.gui.model.Chatroom;
import swe4.gui.model.Message;
import swe4.gui.model.User;
import swe4.gui.observer.ClientObserver;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ChatServer implements ServerService {
    private static DatabaseService database;
    private final ArrayList<ClientObserver> clientObservers = new ArrayList<>();
    private final ArrayList<User> loggedInUsers = new ArrayList<>();

    public static void main(String[] args) throws RemoteException, MalformedURLException {
        int registryPort = Registry.REGISTRY_PORT;
        String serverHostName = "localhost";
        if (args.length > 0) {
            String[] hostAndPort = args[0].split(":");
            if (hostAndPort.length > 0) serverHostName = hostAndPort[0];
            if (hostAndPort.length > 1) registryPort = Integer.parseInt(hostAndPort[1]);
        }

        String internalUrl = "rmi://localhost:%d/ChatServer".formatted(registryPort);
        String externalUrl = "rmi://%s:%d/ChatServer".formatted(serverHostName, registryPort);
        System.setProperty("java.rmi.server.hostname", serverHostName);

        String connectionString = "jdbc:mysql://localhost/ChatBptDb?autoReconnect=true&useSSL=false";
        String userName = "root";

        ServerService service = new ChatServer();
        Remote serviceStub = UnicastRemoteObject.exportObject(service, registryPort);

        LocateRegistry.createRegistry(registryPort);
        Naming.rebind(internalUrl, serviceStub);

        database = new Database(connectionString, userName);

        System.out.printf("Service available at %s%n", externalUrl);
    }

    @Override
    public void addUser(String username, String shortname, String password) {
        database.addUser(username, shortname, password);
    }

    @Override
    public User getUser(String username) {
        return database.getUser(username);
    }

    @Override
    public void addMessage(String selectedChat, Message message) throws RemoteException {
        database.addMessage(selectedChat, message);
        for (ClientObserver client : clientObservers) {
            client.updateMessagesOnNewMessage(database.getChatroom(selectedChat), message);
        }
    }

    @Override
    public void addChatroom(Chatroom chatroom, String user) throws RemoteException {
        database.addChatroom(chatroom, user);
    }

    @Override
    public void updateChatListAndMessageArea(String userName, String chatroom) throws RemoteException {
        for (ClientObserver client : clientObservers) {
            client.updateChatListAndMessageArea(userName, chatroom);
        }
    }

    @Override
    public void addUserToChatroom(User user, String chatroom) throws RemoteException {
        database.addUserToChatroom(user, chatroom);
        for (ClientObserver client : clientObservers) {
            client.updateChatRoomsOnJoin(database.getChatroom(chatroom), user);
        }
    }

    @Override
    public void removeChatRoom(Chatroom chatroom) throws RemoteException {
        ArrayList<User> users = database.getChatroomUsers(chatroom.getName());

        database.removeChatroom(chatroom);
        for (ClientObserver client : clientObservers) {
            client.updateChatroomsOnRemove(chatroom, users);
        }
    }

    @Override
    public boolean chatroomExists(String chatroom) throws RemoteException {
        return database.chatroomExists(chatroom);
    }

    @Override
    public void banUserFromChatroom(User user, String chatroom) throws RemoteException {
        database.banUserFromChatroom(user, chatroom);

        for (ClientObserver client : clientObservers) {
            client.updateChatroomsOnBan(database.getChatroom(chatroom), user);
        }
    }

    @Override
    public void unbanUserFromChatroom(User user, String chatroom) throws RemoteException {
        database.unbanUserFromChatroom(user, chatroom);

        for (ClientObserver client : clientObservers) {
            client.updateChatroomsOnUnban(database.getChatroom(chatroom), user);
        }
    }

    @Override
    public ArrayList<User> getBannedUsersFromChatroom(String roomName) throws RemoteException {
        return database.getBannedUsersFromChatroom(roomName);
    }

    @Override
    public Chatroom getChatroom(String roomName) throws RemoteException {
        return database.getChatroom(roomName);
    }

    @Override
    public ArrayList<Chatroom> getChatrooms() throws RemoteException {
        return database.getChatrooms();
    }

    @Override
    public ArrayList<User> getChatRoomUsers(String chatroom) throws RemoteException {
        return database.getChatroomUsers(chatroom);
    }

    @Override
    public ArrayList<Message> getMessages(String chatroom) throws RemoteException {
        return database.getMessages(chatroom);
    }

    @Override
    public boolean userExists(String user) {
        return database.userExists(user);
    }

    @Override
    public boolean passwordCorrect(String user, String password) {
        return database.passwordCorrect(user, password);
    }

    @Override
    public void registerClient(ClientObserver client, User user) throws RemoteException {
        clientObservers.add(client);
        loggedInUsers.add(user);
    }

    @Override
    public void unregisterClient(ClientObserver client, User user) throws RemoteException {
        clientObservers.remove(client);
        loggedInUsers.remove(user);
    }

    @Override
    public ArrayList<User> getLoggedInUsers() throws RemoteException {
        return loggedInUsers;
    }
}
