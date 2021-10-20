package norseninja;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TcpClient {
    private final String host;
    private final int port;
    private Socket clientSocket;
    private PrintWriter outToServer;
    private BufferedReader inFromServer;
    private boolean connected = false;

    /**
     * Creates a new instance of the class.
     * @param host the host url
     * @param port the connection port
     */
    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Try to establish TCP connection to the server (the three-way handshake).
     *
     * @return True when connection established, false on error
     */
    public boolean connectToServer() {
        try {
            this.clientSocket = new Socket(host, port);
            this.outToServer = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.inFromServer = new BufferedReader(
                    new InputStreamReader(this.clientSocket.getInputStream()));
            this.connected = true;
            return true;
        } catch (IOException e) {
            this.connected = false;
            return false;
        }
    }

    /**
     * Calls server to end the connection, and closes the connection locally.
     */
    public void stop() {
        try {
            if (this.inFromServer != null) {
                this.outToServer.println("end");
                this.outToServer.close();
                this.inFromServer.close();
                this.clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a message to the server.
     *
     * @param recipient the recipient of the message.
     * @param message the message to be sent. Do NOT include the newline in the message!
     * @return True when message successfully sent, false on error.
     */
    public boolean sendMessage(String recipient, String message) {
        if (connected) {
            this.outToServer.println("message/%" + recipient + "/%" + message);
            return readResponseFromServer().startsWith("ok");
        } else {
            return false;
        }
    }

    /**
     * Requests server to respond with the display name of the current user.
     * @return {@code String} displayName of current user.
     */
    public String getMe() {
        String response = null;
        if (connected) {
            this.outToServer.println("getme");
            response = readResponseFromServer();
        }
        return response;
    }

    /**
     * Requests server to respond with a list of currently active users.
     * @return {@code List<String>} of currently active users.
     */
    public List<String> getActiveUsers() {
        ArrayList<String> activeUsers = null;
        if (connected) {
            this.outToServer.println("getactive");
            String response = readResponseFromServer();
            if (response.startsWith("ok")) {
                String[] array = response.split("/%");
                activeUsers = new ArrayList<>(Arrays.asList(array));
                activeUsers.remove(0);
            }
        }
        return activeUsers;
    }

    /**
     * Requests server to respond with a list of all users (both online and offline).
     * @return {@code List<String>} of all registered users.
     */
    public List<String> getAllUsers() {
        ArrayList<String> allUsers = null;
        if (connected) {
            this.outToServer.println("getusers");
            String response = readResponseFromServer();
            if (null != response && (response.startsWith("ok"))) {
                String[] array = response.split("/%");
                allUsers = new ArrayList<>(Arrays.asList(array));
                allUsers.remove(0);
            }
        }
        return allUsers;
    }

    /**
     * Requests the server to check if given password
     * matches the one registered to this user.
     *
     * @param password the password to check.
     * @return {@code boolean} true if match, false if not.
     */
    public boolean checkPassword(String password) {
        if (connected) {
            this.outToServer.println("password/%" + password);
            return readResponseFromServer().startsWith("ok");
        }
        return false;
    }

    /**
     * Requests the server to change the display name of the current user to given value.
     *
     * @param name new displayName
     * @return {@code boolean} true on successful change, or false if not.
     */
    public boolean changeDisplayName(String name) {
        if (connected) {
            this.outToServer.println("editname/%" + name);
            return readResponseFromServer().startsWith("ok");
        }
        return false;
    }

    /**
     * Requests the server to change the current users password.
     *
     * @param oldPassword password to change from
     * @param newPassword password to change to
     * @return {@code boolean} true if change was successful; false if not.
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (connected) {
            this.outToServer.println("editpw/%" + oldPassword + "/%" + newPassword);
            return readResponseFromServer().startsWith("ok");
        }
        return false;
    }

    /**
     * Requests server to send messages addressed to this user.
     * @param lastReceived timestamp of the last received message.
     * @return {@code List<Message>} of new messages.
     */
    public List<Message> getMessages(LocalTime lastReceived) {
        ArrayList<Message> messages = new ArrayList<>();
        if (connected) {
            outToServer.println("getmsg/%" + lastReceived);
            String response = readResponseFromServer();
            if (null != response && response.startsWith("ok")) {
                String[] array = response.split("/%");
                ArrayList<String> stringList = new ArrayList<>(Arrays.asList(array));
                if (stringList.size() > 1) {
                    stringList.remove(0);
                }
                int i = 0;
                while (stringList.size() > i + 3) {
                    messages.add(new Message(LocalTime.parse(stringList.get(i)),
                            stringList.get(i + 1),
                            stringList.get(i + 2),
                            stringList.get(i + 3)));
                    i += 4;
                }
            }
        }
        return messages;
    }

    /**
     * Requests the server to log in with given username and password.
     * @param username username
     * @param password password
     * @return {@code boolean} true if login successful; false if not.
     */
    public boolean login(String username, String password) {
        if (connected) {
            this.outToServer.println("login/%" + username + "/%" + password);
            String response = readResponseFromServer();
            if (null != response) {
                return response.startsWith("ok");
            }
        }
        return false;
    }

    /**
     * Requests the server to log the user out.
     * @return {@code boolean} true if successful; false if not.
     */
    public boolean logout() {
        if (connected) {
            this.outToServer.println("logout");
            String response = readResponseFromServer();
            if (null != response) {
                return response.startsWith("ok");
            }
        }
        return false;
    }

    /**
     * Wait for one response from the remote server.
     *
     * @return The response received from the server, null on error.
     *          The newline character is stripped away (not included in the returned value).
     */
    public String readResponseFromServer() {
        try {
            if (null == clientSocket) {
                return null;
            } else {
                return inFromServer.readLine();
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Checks if the server connection is active,
     * and attempts to restart the server if not.
     *
     * @return {@code boolean} true if connection was active or successfully restarted;
     *          {@code boolean} false if not.
     */
    public boolean checkConnection() {
        if (null != this.clientSocket) {
            this.outToServer.println("probe");
            String response = readResponseFromServer();
            if (null != response) {
                this.connected = true;
                return true;
            } else {
                return connectToServer();
            }
        } else {
            return connectToServer();
        }
    }

}
