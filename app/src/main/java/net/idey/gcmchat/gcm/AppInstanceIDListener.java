package net.idey.gcmchat.gcm;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by yusuf.abdullaev on 7/30/2016.
 */
public class AppInstanceIDListener extends InstanceIDListenerService{

    private static final String TAG = "logs/IIDListener";

    /*
    Вызывается когда токен защиты обновляется. Это может произойти, если предыдущий не может обеспечить безопасность
     */

    @Override
    public void onTokenRefresh() {
        Log.w(TAG, "onTokenRefresh");
        startService(new Intent(this, GcmIntentService.class));
    }
}
