package net.idey.gcmchat.app;

/**
 * Created by yusuf.abdullaev on 7/30/2016.
 */
public class Config {

    //флажок, чтобы выбрать как должно уведомление выглядеть в списке - одно- или многострочным образом
    public static boolean appendNotificationMessages = true;

    //тег глобальный, чтобы получить уведомления всех сообщений
    public static final String GLOBAL_TOPIC = "global";

    //темы для фильтрации интентов бродкаст ресивера
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETED = "registrationCompleted";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    //тип полученного пуш-уведомления
    public static final int PUSH_CHAT_ROOM = 1;
    public static final int PUSH_USER = 2;

    //тип уведомления в списке - с картинкой или без
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

}
