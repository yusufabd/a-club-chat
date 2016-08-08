package net.idey.gcmchat.models;

import java.io.Serializable;

/**
 * Created by yusuf.abdullaev on 7/31/2016.
 */
public class User implements Serializable{
    private String mId, mName, mEmail, mGcmRegistrationId;

    public User() {
    }

    public User(String id, String name, String email, String gcmRegistrationId) {
        mId = id;
        mName = name;
        mEmail = email;
        mGcmRegistrationId = gcmRegistrationId;
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

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getGcmRegistrationId() {
        return mGcmRegistrationId;
    }

    public void setGcmRegistrationId(String gcmRegistrationId) {
        mGcmRegistrationId = gcmRegistrationId;
    }
}
