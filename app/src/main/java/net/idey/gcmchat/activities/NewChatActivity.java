package net.idey.gcmchat.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.idey.gcmchat.R;
import net.idey.gcmchat.adapters.UsersAdapter;
import net.idey.gcmchat.app.Config;
import net.idey.gcmchat.app.EndPoints;
import net.idey.gcmchat.app.GcmChatApplication;
import net.idey.gcmchat.helpers.SimpleDividerItemDecoration;
import net.idey.gcmchat.helpers.Tags;
import net.idey.gcmchat.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewChatActivity extends AppCompatActivity implements Tags{

    private static final String TAG = "logs/newChat";
    private RecyclerView recyclerView;
    private ArrayList<User> userArrayList;
    private UsersAdapter adapter;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_contacts);
        userArrayList = new ArrayList<>();
        adapter = new UsersAdapter(this, userArrayList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)){
                    //TODO add method to handle push
                }
            }
        };

        fetchUsersList();
    }

    private void fetchUsersList(){
        StringRequest request = new StringRequest(Request.Method.GET, EndPoints.USERS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.w(TAG, "Response: " + response);
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(ERROR)){
                        JSONArray usersArray = object.getJSONArray(USERS);
                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject userObj = usersArray.getJSONObject(i);
                            User user = new User(userObj.getString(USER_ID),
                                                 userObj.getString(NAME),
                                                 userObj.getString(EMAIL),
                                                 userObj.getString(GCM_ID));
                            userArrayList.add(user);
                        }
                    }else {
                        Toast.makeText(getApplicationContext(), "" + object.getJSONObject(ERROR).getString(MESSAGE),
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.w(TAG, "JSON parsing error: "+ e.getMessage());
                    Toast.makeText(getApplicationContext(), "JSON parsing error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
                }
                adapter.notifyDataSetChanged();
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

    public void openNewChatRoomActivity(View view){
        startActivity(new Intent(this, NewChatRoomActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
