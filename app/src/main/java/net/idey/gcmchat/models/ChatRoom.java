package net.idey.gcmchat.models;

import java.io.Serializable;

/**
 * Created by yusuf.abdullaev on 7/31/2016.
 */
public class ChatRoom implements Serializable{
    String mId, mName, mLastMessage, mTimestamp;
    int mUnreadCount;

    public ChatRoom() {
    }

    public ChatRoom(String id, String name, String lastMessage, String timestamp, int unreadCount) {
        mId = id;
        mName = name;
        mLastMessage = lastMessage;
        mTimestamp = timestamp;
        mUnreadCount = unreadCount;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getLastMessage() {
        return mLastMessage;
    }

    public void setLastMessage(String lastMessage) {
        mLastMessage = lastMessage;
    }

    public String getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(String timestamp) {
        mTimestamp = timestamp;
    }

    public int getUnreadCount() {
        return mUnreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        mUnreadCount = unreadCount;
    }
}
