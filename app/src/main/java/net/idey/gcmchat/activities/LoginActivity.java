package net.idey.gcmchat.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import net.idey.gcmchat.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements Tags{

    private String TAG = "logs/login";
    private EditText input_email, input_name;
    private TextInputLayout layout_input_email, layout_input_name;
    private Button button_enter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeViews();

        input_name.addTextChangedListener(new CustomTextWatcher(input_name));
        input_email.addTextChangedListener(new CustomTextWatcher(input_email));

        button_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    public void login(){
        if (!validateName() || !validateEmail())
            return;

        progressBar.setVisibility(View.VISIBLE);

        final String name = input_name.getText().toString();
        final String email = input_email.getText().toString();

        StringRequest request = new StringRequest(Request.Method.POST, EndPoints.LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(ERROR)){
                        JSONObject userObject = object.getJSONObject(USER);
                        User user = new User(userObject.getString(USER_ID),
                                            userObject.getString(NAME),
                                            userObject.getString(EMAIL),
                                            null);

                        GcmChatApplication.getInstance().getPrefManager().storeUser(user);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "" + object.getJSONObject(ERROR).getString(MESSAGE),
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "JSON parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "JSON parsing error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                NetworkResponse networkResponse = error.networkResponse;
                Log.w(TAG, "Volley error: " + error.getMessage() + ". Code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage() + ". Code: " + networkResponse,
                        Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(NAME, name);
                params.put(EMAIL, email);

                Log.w(TAG, "Params: " + params.toString());
                return params;
            }
        };

        GcmChatApplication.getInstance().addToRequestQueue(request);
    }

    private void initializeViews(){

        input_email = (EditText)findViewById(R.id.input_email);
        input_name = (EditText)findViewById(R.id.input_name);

        layout_input_email = (TextInputLayout)findViewById(R.id.layout_input_email);
        layout_input_name = (TextInputLayout)findViewById(R.id.layout_input_name);

        button_enter = (Button)findViewById(R.id.btnJoin);

        progressBar = (ProgressBar)findViewById(R.id.progressbar);
        assert progressBar != null;
        progressBar.setProgress(50);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary),
                PorterDuff.Mode.MULTIPLY);
        progressBar.setVisibility(View.GONE);
    }

    private void requestFocus(View view){
        if (view.requestFocus()){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validateName(){
        if (input_name.getText().toString().trim().isEmpty()){
            layout_input_name.setError(getString(R.string.error_enter_name));
            requestFocus(input_name);
            return false;
        }else {
            layout_input_name.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateEmail(){
        String email = input_email.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)){
            layout_input_email.setError(getString(R.string.error_enter_email));
            requestFocus(input_email);
            return false;
        }else {
            layout_input_email.setErrorEnabled(false);
        }
        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private class CustomTextWatcher implements TextWatcher{

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
                case R.id.input_email:
                    validateEmail();
                    break;
                case R.id.input_name:
                    validateName();
                    break;
            }
        }
    }
}
