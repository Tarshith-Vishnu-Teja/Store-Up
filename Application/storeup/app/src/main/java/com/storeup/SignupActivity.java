package com.storeup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.storeup.Extras.CustomJSONObjectRequest;
import com.storeup.Extras.Utils;
import com.storeup.Extras.VolleyController;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private Button btn_signup;
    private EditText et_email;
    private EditText et_username;
    private EditText et_password;
    private EditText et_street;
    private EditText et_city;
    private EditText et_state;
    private EditText et_zipcode;
    private EditText et_phone;



    private String url = "http://10.0.2.2:3000/loginRegister/register";
    private static String KEY_SUCCESS = "success";
    private static String KEY_USERID  = "userid";
    private Thread thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initUI();

        thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(3500); // As I am using LENGTH_LONG in Toast
                    Intent login = new Intent(SignupActivity.this, LoginActivity.class);
                    login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(login);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };


        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(et_email.getText()) || !android.util.Patterns.EMAIL_ADDRESS.matcher(et_email.getText()).matches()) {
                    Toast.makeText(getApplicationContext(), "Please enter an valid email", Toast.LENGTH_LONG).show();
                }else if(TextUtils.isEmpty(et_password.getText()) || et_password.getText().length() < 8 || et_password.getText().length() > 32) {
                    Toast.makeText(getApplicationContext(), "Your password must contain 8-32 character.", Toast.LENGTH_LONG).show();
                }else if(TextUtils.isEmpty(et_username.getText()) || et_username.getText().length() < 2 || et_password.getText().length() > 32) {
                    Toast.makeText(getApplicationContext(), "Your username must contain 2-32 character.", Toast.LENGTH_LONG).show();
                }/*else if(TextUtils.isEmpty(et_phone.getText()) || et_phone.getText().length() < 10 || et_password.getText().length() > 10){
                    Toast.makeText(getApplicationContext(), "Enter a valid phone number", Toast.LENGTH_LONG).show();
                }*/
                else{
                    CustomJSONObjectRequest rq = new CustomJSONObjectRequest(Request.Method.POST, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getString(KEY_SUCCESS) != null) {
                                            int success = Integer.parseInt(response.getString(KEY_SUCCESS));
                                            if (success == 1) {
                                                Toast.makeText(getApplicationContext(), R.string.registered, Toast.LENGTH_LONG).show();
                                                thread.start();
                                            } else if (success == 0) {
                                                Toast.makeText(getApplicationContext(), R.string.email_exists, Toast.LENGTH_LONG).show();
                                            }else if (success == 2) {
                                                Toast.makeText(getApplicationContext(), R.string.username_exists, Toast.LENGTH_LONG).show();
                                            }else {
                                                Toast.makeText(getApplicationContext(), R.string.invalid_post, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Response Error", error.toString());
                            Toast.makeText(getApplicationContext(), R.string.invalid_post, Toast.LENGTH_LONG).show();
                        }
                    }) {

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/x-www-form-urlencoded");
                            return headers;
                        }

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("tag", "register");
                            params.put("email", et_email.getText().toString());
                            params.put("username", et_username.getText().toString());
                            //params.put("password", Utils.md5(et_password.getText().toString()));
                            params.put("password", et_password.getText().toString());
                            params.put("street", et_street.getText().toString());
                            params.put("city", et_city.getText().toString());
                            params.put("state", et_state.getText().toString());
                            params.put("zipcode", et_zipcode.getText().toString());
                            params.put("phone", et_phone.getText().toString());
                            return params;
                        }

                    };

                    VolleyController.getInstance(getApplicationContext()).addToRequestQueue(rq);
                }

            }
        });
    }
    private void initUI() {
        btn_signup = (Button) findViewById(R.id.registrationBtn);
        et_email = (EditText) findViewById(R.id.email);
        et_username = (EditText) findViewById(R.id.user_name);
        et_password = (EditText) findViewById(R.id.user_password);
        et_street = (EditText) findViewById(R.id.street);
        et_city = (EditText) findViewById(R.id.city);
        et_state = (EditText) findViewById(R.id.state);
        et_zipcode = (EditText) findViewById(R.id.zipcode);
        et_phone = (EditText) findViewById(R.id.phonenumber);
    }
}
