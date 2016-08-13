package net.idey.gcmchat.activities;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.idey.gcmchat.R;
import net.idey.gcmchat.app.EndPoints;
import net.idey.gcmchat.app.GcmChatApplication;
import net.idey.gcmchat.helpers.Tags;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewChatRoomActivity extends AppCompatActivity implements Tags {
    private static final String TAG = "logs/newChatRoom";
    private EditText inputTitle;
    private TextInputLayout layoutInputTitle;
    private CircleImageView circleImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat_room);
        initializeViews();

    }

    private void initializeViews(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        inputTitle = (EditText)findViewById(R.id.input_cr_title);
        layoutInputTitle = (TextInputLayout)findViewById(R.id.layout_input_cr_title);
        circleImageView = (CircleImageView)findViewById(R.id.circle_image_view_cr_photo);
        inputTitle.addTextChangedListener(new CustomTextWatcher(inputTitle));

    }
    public void addChatRoom(){
        StringRequest request = new StringRequest(Request.Method.POST, EndPoints.ADD_CHAT_ROOM, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

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
                params.put(NAME, inputTitle.getText().toString().trim());

                Log.w(TAG, params.toString());
                return params;
            }
        };
        GcmChatApplication.getInstance().addToRequestQueue(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_confirm:
                addChatRoom();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void requestFocus(View view){
        if (view.requestFocus()){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    private boolean validateName(){
        if (inputTitle.getText().toString().trim().isEmpty()){
            layoutInputTitle.setError(getString(R.string.error_enter_name));
            requestFocus(inputTitle);
            return false;
        }else {
            layoutInputTitle.setErrorEnabled(false);
        }
        return true;
    }

    private class CustomTextWatcher implements TextWatcher {

        private View view;

        public CustomTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (view.getId()){
                case R.id.input_cr_title:
                    validateName();
                    break;

            }
        }
    }
}
