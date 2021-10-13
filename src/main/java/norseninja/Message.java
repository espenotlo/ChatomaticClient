package norseninja;

import java.time.LocalTime;

public class Message implements Comparable<Message> {
    private final String fromUser;
    private final String toUser;
    private final String messageText;
    private LocalTime timeStamp;

    public Message(LocalTime timeStamp, String fromUser, String toUser, String messageText) {
        this.fromUser = fromUser;
        this.messageText = messageText;
        this.timeStamp = timeStamp;
        this.toUser = toUser;
    }

    public String getFromUser() {
        return this.fromUser;
    }

    public String getToUser() {
        return this.toUser;
    }

    public String getMessageText() {
        return this.messageText;
    }

    public LocalTime getTimeStamp() {
        return (this.timeStamp);
    }

    @Override
    public int compareTo(Message message) {
        return this.timeStamp.compareTo(message.timeStamp);
    }
}