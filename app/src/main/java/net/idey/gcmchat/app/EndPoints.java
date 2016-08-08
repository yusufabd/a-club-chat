package net.idey.gcmchat.app;

/**
 * Created by yusuf.abdullaev on 7/31/2016.
 */
public class EndPoints {
    public static final String BASE_URL = "http://androidclub.uz/api/gcm_chat/v1/index.php";
    public static final String LOGIN = BASE_URL + "/user/login";
    public static final String USERS = BASE_URL + "/users";
    public static final String USER = BASE_URL + "/user/_ID_";
    public static final String CHAT_ROOMS = BASE_URL + "/chat_rooms";
    public static final String CHAT_ROOM_THREAD = BASE_URL + "/chat_rooms/_ID_";
    public static final String CHAT_ROOM_MESSAGE = BASE_URL + "/chat_rooms/_ID_/message";
}
