package swe4.gui;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import swe4.gui.model.Chatroom;
import swe4.gui.model.Message;

import swe4.gui.server.ServerService;

import java.rmi.RemoteException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

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
    public void assertThatServerIsNotNull() {
        assertNotNull(server);
    }

    @Test
    public void confirmThatUserIsNotNullAfterAdd() throws RemoteException {
        server.addUser("testUser", "tU", "testPassword");
        assertNotNull(server.getUser("testUser"));
    }

    @Test
    public void checkIfUserExists() throws RemoteException {
        server.addUser("testUser", "tU", "testPassword");
        assertTrue(server.userExists("testUser"));
    }

    @Test
    public void checkIfUserDoesNotExist() throws RemoteException {
        assertFalse(server.userExists("notexistinguser"));
    }

    @Test
    public void testIfPasswordIsCorrect() throws RemoteException {
        assertTrue(server.passwordCorrect("testUser", "testPassword"));
    }

    @Test
    public void checkIfPasswordIsNotCorrect() throws RemoteException {
        assertFalse(server.passwordCorrect("testUser", "wrongpassword"));
    }

    @Test
    public void chatroomIsNotNullAfterAddChatroom() throws RemoteException {
        server.addChatroom(new Chatroom("testChatroom", server.getUser("testUser")));
        assertNotNull(server.getChatroom("testChatroom"));
    }

    @Test
    public void chatroomExists_returnsFalseIfChatDoesNotExist() throws RemoteException {
        assertFalse(server.chatroomExists("notexistingchatroom"));
    }

    @Test
    public void getChatroom_returnsChatroomIfChatroomExists() throws RemoteException {
        assertNotNull(server.getChatroom("testChatroom"));
    }

    @Test
    public void getChatroom_returnsNullIfChatroomDoesNotExist() throws RemoteException {
        assertNull(server.getChatroom("notexistingchatroom"));
    }

    @Test
    public void getChatrooms_returnsChatrooms() throws RemoteException {
        assertFalse(server.getChatrooms().isEmpty());
    }


    @Test
    public void getMessages_returnsMessages() throws RemoteException {
        server.addMessage("testChatroom", new Message(server.getUser("testUser"), "testMessage", LocalDateTime.now()));
        assertFalse(server.getMessages("testChatroom").isEmpty());
    }
}
