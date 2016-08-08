package net.idey.gcmchat.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;

import net.idey.gcmchat.activities.ChatRoomActivity;
import net.idey.gcmchat.app.Config;
import net.idey.gcmchat.app.GcmChatApplication;
import net.idey.gcmchat.helpers.Tags;
import net.idey.gcmchat.models.Message;
import net.idey.gcmchat.models.User;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by yusuf.abdullaev on 7/30/2016.
 */
public class AppGcmPushReceiver extends GcmListenerService implements Tags {

    private static final String TAG = "logs/PushReceiver";
    private NotificationUtils utils;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        utils = new NotificationUtils(getApplicationContext());
        String title = data.getString(TITLE);
        String message = data.getString(MESSAGE);
        String created_at = data.getString(CREATED_AT);
        String flag = data.getString("flag");
        String image = data.getString(IMAGE);
        String dataString = data.getString("data");
        Boolean isBackground = data.getBoolean(data.getString("is_background"));

        Log.w(TAG, "Bundle:" + data.toString());

        Log.w(TAG, "From: " + from);
        Log.w(TAG, "Title: " + title);
        Log.w(TAG, "Message: " + message);
        Log.w(TAG, "Flag: " + flag);
        Log.w(TAG, "Image: " + image);
        Log.w(TAG, "Time: " + created_at);
        Log.w(TAG, "is background: " + isBackground);

        for (String key:data.keySet()){
            Log.w(TAG, key + " is bundle key");
        }

        if (flag == null){
            return;
        }

        if (GcmChatApplication.getInstance().getPrefManager().getUser() == null){
            Log.w(TAG, "user is not logged, pass the push");
            return;
        }

        switch (Integer.parseInt(flag)){
            case Config.PUSH_CHAT_ROOM:
                processChatRoomMessage(title, isBackground, dataString);
                break;
            case Config.PUSH_USER:
                processUserMessage(title, isBackground, dataString);
                break;
        }


    }

    private void processChatRoomMessage(String title, boolean isBackground, String data){
//        if (!isBackground){
        try {
            JSONObject object = new JSONObject(data);
            String chatRoomId = object.getString(CHAT_ROOM_ID);

            JSONObject mObject = object.getJSONObject(MESSAGE);

            Message message = new Message();
            message.setMessage(mObject.getString(MESSAGE));
            message.setId(mObject.getString(MESSAGE_ID));
            message.setCreatedAt(mObject.getString(CREATED_AT));

            JSONObject userObject = object.getJSONObject(USER);
            if (userObject.getString(USER_ID).equals(GcmChatApplication.getInstance().getPrefManager().getUser().getId())){
                Log.w(TAG, "Skipping own message's push");
                return;
            }
            User user = new User();
            user.setId(userObject.getString(USER_ID));
            user.setEmail(userObject.getString(EMAIL));
            user.setName(userObject.getString(NAME));
            message.setUser(user);

            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())){
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra(TYPE, Config.PUSH_CHAT_ROOM);
                pushNotification.putExtra(MESSAGE, message);
                pushNotification.putExtra(CHAT_ROOM_ID, chatRoomId);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                utils.playNotificationSound();
            }else {
                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                intent.putExtra(CHAT_ROOM_ID, chatRoomId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                utils.showNotificationMessage(title, user.getName() + " : " + message.getMessage(), message.getCreatedAt(), intent, null);
            }
        } catch (JSONException e) {
            Log.w(TAG, "JSON parsing error: "+ e.getMessage());
//            Toast.makeText(getApplicationContext(), "JSON parsing error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void processUserMessage(String title, boolean isBackground, String data){
//            if (!isBackground){
        try {
            JSONObject object = new JSONObject(data);
            String imageUrl = object.getString(IMAGE);

            JSONObject mObject = object.getJSONObject(MESSAGE);

            Message message = new Message();
            message.setMessage(mObject.getString(MESSAGE));
            message.setId(mObject.getString(MESSAGE_ID));
            message.setCreatedAt(mObject.getString(CREATED_AT));

            JSONObject userObject = object.getJSONObject(USER);
            User user = new User();
            user.setId(userObject.getString(USER_ID));
            user.setEmail(userObject.getString(EMAIL));
            user.setName(userObject.getString(NAME));
            message.setUser(user);

            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())){
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra(TYPE, Config.PUSH_USER);
                pushNotification.putExtra(MESSAGE, message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                utils.playNotificationSound();
            }else {
                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                if (TextUtils.isEmpty(imageUrl)){
                    utils.showNotificationMessage(title, user.getName() + " : " + message.getMessage(), message.getCreatedAt(), intent, null);
                }else {
                    utils.showNotificationMessage(title, message.getMessage(), message.getCreatedAt(), intent, imageUrl);
                }
            }
        } catch (JSONException e) {
            Log.w(TAG, "JSON parsing error: "+ e.getMessage());
            Toast.makeText(getApplicationContext(), "JSON parsing error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
}


