package net.idey.gcmchat.app;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import net.idey.gcmchat.activities.LoginActivity;

/**
 * Created by yusuf.abdullaev on 7/30/2016.
 */
public class GcmChatApplication extends Application{

    private String TAG = "logs/app";

    private static GcmChatApplication mInstance;

    private AppPreferencesManager pref;
    private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized GcmChatApplication getInstance(){
        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        if (mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public AppPreferencesManager getPrefManager(){
        if (pref == null){
            pref = new AppPreferencesManager(this);
        }
        return pref;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag){
        req.setTag(TextUtils.isEmpty(tag) ?  GcmChatApplication.class.getSimpleName() : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req){
        req.setTag(GcmChatApplication.class.getSimpleName());
        getRequestQueue().add(req);
    }

    public void cancelPendingIntent(Object tag){
        if (mRequestQueue != null){
            mRequestQueue.cancelAll(tag);
        }
    }

    public void logout(){
        pref.cleanAll();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
