package swe4.gui.model;

import java.io.Serial;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private final User sender;
    private final String text;
    private final LocalDateTime time;

    @Serial
    private static final long serialVersionUID = -1807385026432397002L;

    public Message(User sender, String text, LocalDateTime time) {
        this.sender = sender;
        this.text = text;
        this.time = time;
    }

    public User getSender() {
        return sender;
    }

    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return time.format(formatter);
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString(){
        return sender.getShortName() +  " am " + getFormattedTime() + "\n" + text;
    }
}
