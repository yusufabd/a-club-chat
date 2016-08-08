package net.idey.gcmchat.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.idey.gcmchat.R;
import net.idey.gcmchat.adapters.ChatRoomThreadAdapter;
import net.idey.gcmchat.app.Config;
import net.idey.gcmchat.app.EndPoints;
import net.idey.gcmchat.app.GcmChatApplication;
import net.idey.gcmchat.gcm.NotificationUtils;
import net.idey.gcmchat.helpers.Tags;
import net.idey.gcmchat.models.Message;
import net.idey.gcmchat.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity implements Tags{

    private static final String TAG = "logs/chatRoom";


    private String chatRoomId;
    private Button buttonSend;
    private EditText inputMessage;
    private RecyclerView recyclerView;
    private ChatRoomThreadAdapter mAdapter;
    private ArrayList<Message> messageArrayList;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra(NAME));
        inputMessage = (EditText)findViewById(R.id.input_message);
        buttonSend = (Button)findViewById(R.id.btn_send);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_messages);

        chatRoomId = getIntent().getStringExtra(CHAT_ROOM_ID);
        if (chatRoomId == null){
            Toast.makeText(this, "Chat room not found!", Toast.LENGTH_LONG).show();
            finish();
        }

        messageArrayList = new ArrayList<>();
        mAdapter = new ChatRoomThreadAdapter(GcmChatApplication.getInstance().getPrefManager().getUser().getId(),
                this,
                messageArrayList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)){
                    handleNotification(intent);
                }
            }
        };
        fetchChatThread();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));
        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    private void handleNotification(Intent data){
        Message message = (Message)data.getSerializableExtra(MESSAGE);
        String chatRoomId = data.getStringExtra(CHAT_ROOM_ID);

        if (message != null && chatRoomId != null){
            messageArrayList.add(message);
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() > 1){
                recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
            }
        }
    }

    public void sendMessage(View view){
        final String messageText = inputMessage.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(getApplicationContext(), "Please, enter message", Toast.LENGTH_LONG).show();
            return;
        }

        String endPoint = EndPoints.CHAT_ROOM_MESSAGE.replace("_ID_", chatRoomId);
        Log.w(TAG, "End Point: " + endPoint);
        inputMessage.setText("");

        StringRequest request = new StringRequest(Request.Method.POST, endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.w(TAG, response);
                try {
                    JSONObject object = new JSONObject(response);

                    if (!object.getBoolean(ERROR)){
                    JSONObject commentObj = object.getJSONObject(MESSAGE);

                    JSONObject userObj = object.getJSONObject(USER);

                    Message message = new Message();
                    message.setId(commentObj.getString(MESSAGE_ID));
                    message.setMessage(commentObj.getString(MESSAGE));
                    message.setCreatedAt(commentObj.getString(CREATED_AT));
                    message.setUser(new User(userObj.getString(USER_ID),
                                             userObj.getString(NAME),
                                             userObj.getString(EMAIL),
                                             null));

                    messageArrayList.add(message);
                    mAdapter.notifyDataSetChanged();
                    if (mAdapter.getItemCount() > 1){
                        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                        }
                    }else {
                        Toast.makeText(getApplicationContext(), "" + object.getString(MESSAGE), Toast.LENGTH_LONG).show();
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
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(USER_ID, GcmChatApplication.getInstance().getPrefManager().getUser().getId());
                params.put(MESSAGE, messageText);

                Log.w(TAG, params.toString());
                return params;
            }
        };

        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        GcmChatApplication.getInstance().addToRequestQueue(request);
    }

    private void fetchChatThread(){
        String endPoint = EndPoints.CHAT_ROOM_THREAD.replace("_ID_", chatRoomId);
        Log.w(TAG, "End Point: " + endPoint);

        StringRequest request = new StringRequest(Request.Method.GET,
                endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.w(TAG, "Response: " + response);

                try {
                    JSONObject object = new JSONObject(response);

                    if (!object.getBoolean(ERROR)){

                    JSONArray threadArray = object.getJSONArray("messages");
                    for (int i = 0; i < threadArray.length(); i++) {
                        JSONObject arrayItem = threadArray.getJSONObject(i);
                        String mess_id = arrayItem.getString(MESSAGE_ID);
                        String mess = arrayItem.getString(MESSAGE);
                        String mess_time = arrayItem.getString(CREATED_AT);

                        JSONObject userObj = arrayItem.getJSONObject(USER);
                        String user_id = userObj.getString(USER_ID);
                        String username = userObj.getString("username");
                        User user = new User(user_id, username, null, null);

                        Message message = new Message();
                        message.setId(mess_id);
                        message.setMessage(mess);
                        message.setCreatedAt(mess_time);
                        message.setUser(user);

                        messageArrayList.add(message);
                    }
                    mAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(mAdapter);
                    if (mAdapter.getItemCount() > 1){
                        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                    }

                    }else{
                        Toast.makeText(getApplicationContext(), "" + object.getJSONObject(ERROR).getString(MESSAGE),
                                Toast.LENGTH_LONG).show();
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
        });
        GcmChatApplication.getInstance().addToRequestQueue(request);
    }

}
