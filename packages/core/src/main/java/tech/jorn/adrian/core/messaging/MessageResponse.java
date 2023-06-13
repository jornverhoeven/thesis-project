package tech.jorn.adrian.core.messaging;

public class MessageResponse<T> {
    private final T data;
    private final String replyId;

    public MessageResponse(T data) {
        this.data = data;
        this.replyId = null;
    }
    public MessageResponse(T data, String replyId) {
        this.data = data;
        this.replyId = replyId;
    }

    public T getData() {
        return data;
    }

    public String getReplyId() {
        return replyId;
    }
}
