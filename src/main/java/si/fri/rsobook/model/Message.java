package si.fri.rsobook.model;

public class Message {

    public String sender;
    public String content;

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    public Message() {
    }
}
