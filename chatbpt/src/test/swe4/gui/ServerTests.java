package swe4.gui;

import org.junit.jupiter.api.*;

import swe4.gui.model.Chatroom;
import swe4.gui.model.Message;

import swe4.gui.server.ServerService;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerTests {
    private static ServerService server;

    @BeforeAll
    public static void setUp() {
        String hostAndPort = "localhost";
        String serviceUrl = "rmi://" + hostAndPort + "/ChatServer";

        System.out.println("Connecting to server at " + serviceUrl);

        try {
            server = (ServerService) java.rmi.Naming.lookup(serviceUrl);
        } catch (Exception e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            System.exit(1);
        }
    }

    @Test
    @Order(1)
    public void assertThatServerIsNotNull() {
        assertNotNull(server);
    }

    @Test
    @Order(2)
    public void confirmThatExistingUserIsNotNull() throws RemoteException {
        assertNotNull(server.getUser("Admin"));
    }

    @Test
    @Order(3)
    public void checkIfUserExists() throws RemoteException {
        assertTrue(server.userExists("Admin"));
    }

    @Test
    @Order(4)
    public void checkIfUserDoesNotExist() throws RemoteException {
        assertFalse(server.userExists("notexistinguser"));
    }

    @Test
    @Order(5)
    public void testIfPasswordIsCorrect() throws RemoteException {
        assertTrue(server.passwordCorrect("Admin", "pass"));
    }

    @Test
    @Order(6)
    public void checkIfPasswordIsNotCorrect() throws RemoteException {
        assertFalse(server.passwordCorrect("Admin", "wrongpassword"));
    }

    @Test
    @Order(7)
    public void chatroomIsNotNullAfterAddChatroom() throws RemoteException {
        if (server.chatroomExists("testChatroom")) server.removeChatroom(server.getChatroom("testChatroom"));
        server.addChatroom(new Chatroom("testChatroom", server.getUser("Admin")), "Admin");
        server.addUserToChatroom(server.getUser("Admin"), "testChatroom");
        server.addUserToChatroom(server.getUser("Tobias"), "testChatroom");
        assertNotNull(server.getChatroom("testChatroom"));
    }

    @Test
    @Order(8)
    public void chatroomExists_returnsFalseIfChatDoesNotExist() throws RemoteException {
        assertFalse(server.chatroomExists("notexistingchatroom"));
    }

    @Test
    @Order(9)
    public void getChatroom_returnsChatroomIfChatroomExists() throws RemoteException {
        assertNotNull(server.getChatroom("testChatroom"));
    }

    @Test
    @Order(10)
    public void getChatroom_returnsNullIfChatroomDoesNotExist() throws RemoteException {
        assertNull(server.getChatroom("notexistingchatroom"));
    }

    @Test
    @Order(11)
    public void getChatrooms_isNotNull() throws RemoteException {
        assertFalse(server.getChatrooms().isEmpty());
    }

    @Test
    @Order(12)
    public void getChatroomUsers_isNotNull() throws RemoteException {
        assertNotNull(server.getChatroomUsers("testChatroom"));
    }

    @Test
    @Order(13)
    public void addedUsersAreInChatroom() throws RemoteException {
        assertTrue(server.getChatroomUsers("testChatroom").contains(server.getUser("Tobias")));
    }

    @Test
    @Order(14)
    public void getMessages_returnsMessages() throws RemoteException {
        server.addMessage("testChatroom", new Message(server.getUser("Admin"), "testMessage", Timestamp.valueOf(LocalDateTime.now())));
        assertFalse(server.getMessages("testChatroom").isEmpty());
    }

    @Test
    @Order(15)
    public void bannedUsersAreInBannedUsersList() throws RemoteException {
        server.banUserFromChatroom(server.getUser("Tobias"), "testChatroom");
        assertTrue(server.getBannedUsersFromChatroom("testChatroom").contains(server.getUser("Tobias")));
    }

    @Test
    @Order(16)
    public void unbanUserFromChatroom_removesUserFromBannedUsersList() throws RemoteException {
        server.unbanUserFromChatroom(server.getUser("Tobias"), "testChatroom");
        assertFalse(server.getBannedUsersFromChatroom("testChatroom").contains(server.getUser("Tobias")));
    }

    @Test
    @Order(17)
    public void removeChatroom_removesChatroom() throws RemoteException {
        server.removeChatroom(server.getChatroom("testChatroom"));
        assertNull(server.getChatroom("testChatroom"));
    }
}
