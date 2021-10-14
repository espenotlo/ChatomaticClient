package norseninja;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.time.LocalTime;
import java.util.*;

public class TcpClient {
    private final String host;
    private final int port;
    private Socket clientSocket;
    private PrintWriter outToServer;
    private BufferedReader inFromServer;

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
            this.inFromServer = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void stop() {
        try {
            if (this.inFromServer != null) {
                this.outToServer.println("end");
                this.outToServer.close();
                this.inFromServer.close();
                this.clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Send a message to the server
     *
     * @param recipient the recipient of the message.
     * @param message the message to be sent. Do NOT include the newline in the message!
     * @return True when message successfully sent, false on error.
     */
    public boolean sendMessage(String recipient, String message) {
        if (this.clientSocket.isClosed()) {
            return false;
        } else {
            this.outToServer.println("message/%" + recipient + "/%" + message);
            return readResponseFromServer().startsWith("ok");
        }
    }

    public String getMe() {
        String response = null;
         if (!this.clientSocket.isClosed()) {
            this.outToServer.println("getme");
            response = readResponseFromServer();
        }
        return response;
    }

    public List<String> getActiveUsers() {
        ArrayList<String> activeUsers = null;
        this.outToServer.println("getactive");
        String response = readResponseFromServer();
        if (null!= response && (response.startsWith("ok"))) {
            String[] array = response.split("/%");
            activeUsers = new ArrayList<>(Arrays.asList(array));
            activeUsers.remove(0);
        }
        return activeUsers;
    }

    public List<String> getAllUsers() {
        ArrayList<String> allUsers = null;
        if (!this.clientSocket.isClosed()) {
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

    public boolean checkPassword(String password) {
        if (!this.clientSocket.isClosed()) {
            this.outToServer.println("password/%" + password);
            return readResponseFromServer().startsWith("ok");
        }
        return false;
    }

    public boolean changeDisplayName(String name) {
        if (!this.clientSocket.isClosed()) {
            this.outToServer.println("editname/%" + name);
            return readResponseFromServer().startsWith("ok");
        }
        return false;
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (!this.clientSocket.isClosed()) {
            this.outToServer.println("editpw/%" + oldPassword + "/%" + newPassword);
            return readResponseFromServer().startsWith("ok");
        }
        return false;
    }

    public List<Message> getMessages(LocalTime lastReceived) throws ConnectException {
        ArrayList<Message> messages = new ArrayList<>();
        if (!this.clientSocket.isClosed()) {
            outToServer.println("getmsg/%" + lastReceived);
            String response = readResponseFromServer();
            if (null == response) {
                throw new ConnectException("Connection error");
            } else {
                if (response.startsWith("ok")) {
                    String[] array = response.split("/%");
                    ArrayList<String> stringList = new ArrayList<>(Arrays.asList(array));
                    if (stringList.size() > 1) {
                        stringList.remove(0);
                    }
                    int i = 0;
                    while (stringList.size() > i + 3) {
                        messages.add(new Message(LocalTime.parse(stringList.get(i)), stringList.get(i + 1), stringList.get(i + 2), stringList.get(i + 3)));
                        i += 4;
                    }
                } else {
                    System.out.println("Error: server could not interpret request");
                }
            }
        } else {
            System.out.println("client socket is closed.");
        }
        return messages;
    }

    public boolean login(String username, String password) {
        if (!this.clientSocket.isClosed()) {
            this.outToServer.println("login/%" + username + "/%" + password);
            String response = readResponseFromServer();
            if (null != response) {
                return response.startsWith("ok");
            }
        }
        return false;
    }

    public boolean logout() {
        if (!this.clientSocket.isClosed()) {
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
     * @return The response received from the server, null on error. The newline character is stripped away
     * (not included in the returned value).
     */
    public String readResponseFromServer() {
        try {
            if (!pingServer()) {
                return null;
            } else {
                return this.inFromServer.readLine();
            }
        } catch (IOException e) {
            return null;
        }
    }

    public boolean pingServer() {
        try {
            return (this.clientSocket.getInetAddress().isReachable(1000));
        } catch (IOException e) {
            return false;
        }
    }
}
