package norseninja;

import java.time.LocalTime;

public class Message implements Comparable<Message> {
    private final String fromUser;
    private final String toUser;
    private final String messageText;
    private LocalTime timeStamp;

    /**
     * Creates a new Message with given parameters.
     * @param timeStamp timestamp of when the message was received
     * @param fromUser the sender
     * @param toUser the recipient
     * @param messageText the message text
     */
    public Message(LocalTime timeStamp, String fromUser, String toUser, String messageText) {
        this.fromUser = fromUser;
        this.messageText = messageText;
        this.timeStamp = timeStamp;
        this.toUser = toUser;
    }

    /**
     * Returns the name of the sender.
     * @return name of sender
     */
    public String getFromUser() {
        return this.fromUser;
    }

    /**
     * Returns the name of the recipient.
     * @return name of recipient
     */
    public String getToUser() {
        return this.toUser;
    }

    /**
     * Returns the content of the message.
     * @return text content of message
     */
    public String getMessageText() {
        return this.messageText;
    }

    /**
     * Returns the timestamp of the message.
     * @return timestamp of message
     */
    public LocalTime getTimeStamp() {
        return (this.timeStamp);
    }

    @Override
    public int compareTo(Message message) {
        return this.timeStamp.compareTo(message.timeStamp);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Message) {
            return ((Message) object).getTimeStamp().equals(this.timeStamp)
                    && ((Message) object).getMessageText().equals(this.messageText);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}