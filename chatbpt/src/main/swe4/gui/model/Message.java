package swe4.gui.model;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private final User sender;
    private final String text;
    private final Timestamp time;

    @Serial
    private static final long serialVersionUID = -1807385026432397002L;

    public Message(User sender, String text, Timestamp time) {
        this.sender = sender;
        this.text = text;
        this.time = time;
    }

    public User getSender() {
        return sender;
    }

    public String getFormattedTime() {
        return time.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString(){
        return sender.getName() +  " am " + getFormattedTime() + ": " + text + "\n";
    }

    public Timestamp getTimestamp() {
        return time;
    }
}
