package eslamfaisal.srahahmessenger.modules;

public class Message {

    private String message;
    private String type;
    private long time;
    private boolean seen;
    private String imageUrl;
    private String from;


    public Message() {
    }

    public Message(String message, String type, long time, boolean seen, String imageUrl, String from) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
        this.imageUrl = imageUrl;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }


}
