package net.idey.gcmchat.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import net.idey.gcmchat.R;
import net.idey.gcmchat.helpers.Tags;
import net.idey.gcmchat.app.Config;
import net.idey.gcmchat.app.EndPoints;
import net.idey.gcmchat.app.GcmChatApplication;
import net.idey.gcmchat.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yusuf.abdullaev on 7/30/2016.
 */
public class GcmIntentService extends IntentService implements Tags{
    private static final String TAG = "logs/gcmService";


    public static final String KEY = "key";
    public static final String TOPIC = "topic";
    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";

    public GcmIntentService() {
        super(GcmIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String key = intent.getStringExtra(KEY);
        switch (key){
            case SUBSCRIBE:
                subscribe(intent.getStringExtra(TOPIC));
                break;
            case UNSUBSCRIBE:
                unsubscribe(intent.getStringExtra(TOPIC));
                break;
            default:
                registerGcm();
                break;
        }

    }

    private void registerGcm(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String token = null;
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            //token is Gcm registration Id
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.w(TAG, "GCM token: " + token);

            sendTokenToServer(token);
            pref.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (IOException e) {

            Log.w(TAG, "Failed to get GCM token: " + e.getMessage());
            pref.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, false).apply();
        }
        Intent registrationCompleted = new Intent(Config.REGISTRATION_COMPLETED);
        registrationCompleted.putExtra(TOKEN, token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationCompleted);
    }

    private void sendTokenToServer(final String token){
        User user = GcmChatApplication.getInstance().getPrefManager().getUser();
        if (user==null)
            return;

        String endPoint = EndPoints.USER.replace("_ID_", user.getId());

        Log.w(TAG, "end point: " + endPoint);
        StringRequest request = new StringRequest(Request.Method.PUT, endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);

                    if (!object.getBoolean(ERROR)){
                        Intent sentToken = new Intent(Config.SENT_TOKEN_TO_SERVER);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sentToken);
                    }else {
                        Toast.makeText(getApplicationContext(), "Unable to send token to server", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.w(TAG, "JSON parsing error: "+ e.getMessage());
                    Toast.makeText(getApplicationContext(), "JSON parsing error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.w(TAG, "Volley error: " + error.getMessage() + ". Code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage() + ". Code: " + networkResponse,
                        Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(GCM_ID, token);

                Log.w(TAG, "Params: " + params.toString());
                return params;
            }
        };
        GcmChatApplication.getInstance().addToRequestQueue(request);
    }

    private void subscribe(String topic){
        GcmPubSub pubSub = GcmPubSub.getInstance(getApplicationContext());
        String token = null;
        InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
        try{
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            if (token!=null){
                pubSub.subscribe(token, "/topics/" + topic, null);
                Log.w(TAG, "Subscribed to topic: " + topic);
            }else {
                Log.w(TAG, "Gcm token is null!");
            }
        }catch (IOException e){
            Log.w(TAG, topic + " topic subscribe error: " + e.getMessage());
            Toast.makeText(getApplicationContext(), topic + " topic subscribe error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void unsubscribe(String topic){
        GcmPubSub pubSub = GcmPubSub.getInstance(getApplicationContext());
        String token = null;
        InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
        try{
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            if (token!=null){
                pubSub.unsubscribe(token, "/topics/" + topic);
                Log.w(TAG, "Unsubscribed from topic: " + topic);
            }else {
                Log.w(TAG, "Gcm token is null!");
            }
        }catch (IOException e){
            Log.w(TAG, topic + " topic unsubscribe error: " + e.getMessage());
            Toast.makeText(getApplicationContext(), topic + " topic unsubscribe error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
