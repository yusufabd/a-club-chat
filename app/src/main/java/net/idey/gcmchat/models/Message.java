package net.idey.gcmchat.models;

import java.io.Serializable;

/**
 * Created by yusuf.abdullaev on 7/31/2016.
 */
public class Message implements Serializable{

    String mId, mMessage, mCreatedAt;
    User mUser;

    public Message() {
    }

    public Message(String id, String message, String createdAt, User user) {
        mId = id;
        mMessage = message;
        mCreatedAt = createdAt;
        mUser = user;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        mCreatedAt = createdAt;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }
}
