package swe4.gui.server;

import swe4.gui.backend.MockDatabase;
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
import java.util.List;

public class ChatServer implements ServerService {
    private static MockDatabase database;
    private final List<ClientObserver> clientObservers = new ArrayList<>();
    private final List<User> loggedInUsers = new ArrayList<>();

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

        ServerService service = new ChatServer();
        Remote serviceStub = UnicastRemoteObject.exportObject(service, registryPort);

        LocateRegistry.createRegistry(registryPort);
        Naming.rebind(internalUrl, serviceStub);

        database = MockDatabase.getInstance();

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
        database.getChatroom(selectedChat).addMessage(message);
        for (ClientObserver client : clientObservers) {
            client.updateMessagesOnNewMessage(database.getChatroom(selectedChat), message);
        }
    }

    @Override
    public void addChatroom(Chatroom chatroom) throws RemoteException {
        database.addChatroom(chatroom);
    }

    @Override
    public void updateChatListAndMessageArea(String userName, String chatroomName) throws RemoteException {
        for (ClientObserver client : clientObservers) {
            client.updateChatListAndMessageArea(userName, chatroomName);
        }
    }

    @Override
    public void addUserToChatroom(User user, String roomName) throws RemoteException {
        database.getChatroom(roomName).addUser(user);
        for (ClientObserver client : clientObservers) {
            client.updateChatRoomsOnJoin(database.getChatroom(roomName), user);
        }
    }

    @Override
    public void removeChatRoom(Chatroom chatroom) throws RemoteException {
        database.removeChatroom(chatroom);

        for (ClientObserver client : clientObservers) {
            client.updateChatroomsOnRemove(chatroom);
        }
    }

    @Override
    public boolean chatroomExists(String roomName) throws RemoteException {
        return database.chatroomExists(roomName);
    }

    @Override
    public void removeUserFromChatroom(User loggedInUser, String selectedChat) throws RemoteException {
        database.getChatroom(selectedChat).removeUser(loggedInUser);
        for (ClientObserver client : clientObservers) {
            client.updateChatroomsOnLeave(database.getChatroom(selectedChat), loggedInUser);
        }
    }

    @Override
    public void banUserFromChatroom(User user, String roomName) throws RemoteException {
        database.getChatroom(roomName).banUser(user.getName());

        for (ClientObserver client : clientObservers) {
            client.updateChatroomsOnBan(database.getChatroom(roomName), user);
        }
    }

    @Override
    public void unbanUserFromChatroom(User user, String roomName) throws RemoteException {
        database.getChatroom(roomName).unbanUser(user.getName());

        for (ClientObserver client : clientObservers) {
            client.updateChatroomsOnUnban(database.getChatroom(roomName), user);
        }
    }

    @Override
    public List<User> getUsers() throws RemoteException {
        return database.getUsers();
    }

    @Override
    public Chatroom getChatroom(String roomName) throws RemoteException {
        return database.getChatroom(roomName);
    }

    @Override
    public List<Chatroom> getChatrooms() throws RemoteException {
        return database.getChatrooms();
    }

    @Override
    public List<User> getChatRoomUsers(String roomName) throws RemoteException {
        return database.getChatroom(roomName).getUsers();
    }

    @Override
    public List<Message> getMessages(String chatroom) throws RemoteException {
        return database.getChatroom(chatroom).getMessages();
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
    public List<User> getLoggedInUsers() throws RemoteException {
        return loggedInUsers;
    }
}
