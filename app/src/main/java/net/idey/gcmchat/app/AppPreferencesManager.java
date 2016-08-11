package net.idey.gcmchat.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.idey.gcmchat.models.User;

/**
 * Created by yusuf.abdullaev on 7/30/2016.
 */
public class AppPreferencesManager {

    private String TAG = "logs/pref";

    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private static final String pref_name = "gcm_chat_android";
    private int MODE_PRIVATE = 0;

    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_GCM_ID = "gcm_registration_id";
    private static final String KEY_NOTIFICATIONS = "notifications";

    public AppPreferencesManager(Context context) {
        this.mContext = context;
        pref = mContext.getSharedPreferences(pref_name, 0);
        editor = pref.edit();
    }

    public void storeUser(User user){
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.apply();

        Log.w(TAG, "User " + user.getName() + " with ID " + user.getId() + " and email " + user.getEmail() + " is stored in preferences");
    }

    public User getUser(){
        if (pref.getString(KEY_USER_ID, null) != null){
            return new User(pref.getString(KEY_USER_ID, null),
                            pref.getString(KEY_USER_NAME, null),
                            pref.getString(KEY_USER_EMAIL, null),
                            pref.getString(KEY_USER_GCM_ID, null));
        }
        return null;
    }

    public void addNotification(String notification){
        String oldNotification = getNotification();

        if (oldNotification != null){
            oldNotification = oldNotification + " | " + notification;
        }else {
            oldNotification = notification;
        }
        editor.putString(KEY_NOTIFICATIONS, oldNotification);
        editor.apply();
    }

    public String getNotification(){
        return pref.getString(KEY_NOTIFICATIONS, null);
    }

    public void cleanAll(){
        editor.clear();
        editor.apply();
    }
}
