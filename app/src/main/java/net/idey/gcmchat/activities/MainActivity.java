package net.idey.gcmchat.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import net.idey.gcmchat.R;
import net.idey.gcmchat.adapters.ChatRoomsAdapter;
import net.idey.gcmchat.app.Config;
import net.idey.gcmchat.app.EndPoints;
import net.idey.gcmchat.app.GcmChatApplication;
import net.idey.gcmchat.gcm.GcmIntentService;
import net.idey.gcmchat.gcm.NotificationUtils;
import net.idey.gcmchat.helpers.SimpleDividerItemDecoration;
import net.idey.gcmchat.helpers.Tags;
import net.idey.gcmchat.models.ChatRoom;
import net.idey.gcmchat.models.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Tags{

    private static final String TAG = "logs/main";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9900;
    private BroadcastReceiver mBroadcastReceiver;

    private RecyclerView recyclerView;
    private ArrayList<ChatRoom> chatRoomArrayList;
    private ChatRoomsAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (GcmChatApplication.getInstance().getPrefManager().getUser() == null){
            launchLoginActivity();
        }
        setContentView(R.layout.activity_main);
        initializeViews();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETED)){
                    subscribeToGlobalTopic();
                }else if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)){
                    Log.w(TAG, "Gcm registration token stored in server");
                }else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)){
                    handlePushNotification(intent);
                }
            }
        };

        chatRoomArrayList = new ArrayList<>();
        mAdapter = new ChatRoomsAdapter(this, chatRoomArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        if (checkPlayServices()){
            registerGcm();
            fetchChatRooms();
        }
    }

    private void registerGcm(){
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra(KEY, REGISTER);
        startService(intent);
    }

    private boolean checkPlayServices(){
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS){
            if (availability.isUserResolvableError(resultCode)){
                availability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }else {
                Log.w(TAG, "Play services not installed!");
                Toast.makeText(getApplicationContext(), "Play services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void handlePushNotification(Intent intent){
        int type = intent.getIntExtra(TYPE, -1);
        Message message = (Message)intent.getSerializableExtra(MESSAGE);
        if (type == Config.PUSH_CHAT_ROOM){
            String chatRoomID = intent.getStringExtra(CHAT_ROOM_ID);

            if (message != null && chatRoomID != null){
                updateRow(chatRoomID, message);
            }
        }else if (type == Config.PUSH_USER){
            //TODO show notification in title bar
            Toast.makeText(getApplicationContext(), "New push: " + message.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateRow(String chatRoomID, Message message) {
        for (ChatRoom c : chatRoomArrayList){
            if (c.getId().equals(chatRoomID)){
                int index = chatRoomArrayList.indexOf(c);
                c.setLastMessage(message.getMessage());
                c.setUnreadCount(c.getUnreadCount() + 1);
                chatRoomArrayList.remove(index);
                chatRoomArrayList.add(index, c);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void fetchChatRooms(){
        StringRequest request = new StringRequest(Request.Method.GET, EndPoints.CHAT_ROOMS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);

                    if (!object.getBoolean(ERROR)) {
                        JSONArray chatsArray = object.getJSONArray(CHAT_ROOMS);
                        for (int c = 0; c < chatsArray.length(); c++) {
                            JSONObject chatRoomObject = chatsArray.getJSONObject(c);
                            ChatRoom chR = new ChatRoom();
                            chR.setId(chatRoomObject.getString(CHAT_ROOM_ID));
                            chR.setName(chatRoomObject.getString(NAME));
                            chR.setLastMessage("");
                            chR.setUnreadCount(0);
                            chR.setTimestamp(chatRoomObject.getString(CREATED_AT));

                            chatRoomArrayList.add(chR);
                        }
                    }else {
                        Toast.makeText(getApplicationContext(), "" + object.getJSONObject(ERROR).getString(MESSAGE),
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.w(TAG, "JSON parsing error: "+ e.getMessage());
                    Toast.makeText(getApplicationContext(), "JSON parsing error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
                }

                mAdapter.notifyDataSetChanged();
                subscribeToAllTopics();
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

    private void subscribeToGlobalTopic(){
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
        intent.putExtra(GcmIntentService.TOPIC, Config.GLOBAL_TOPIC);
        startService(intent);
    }

    private void subscribeToAllTopics(){
        for(ChatRoom chatRoom : chatRoomArrayList){
            Intent intent = new Intent(this, GcmIntentService.class);
            intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
            intent.putExtra(GcmIntentService.TOPIC, "topic_" + chatRoom.getId());
            startService(intent);
        }
    }

    private void launchLoginActivity(){
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(Config.REGISTRATION_COMPLETED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));
        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    private void initializeViews(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NewChatActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout:
                GcmChatApplication.getInstance().logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
