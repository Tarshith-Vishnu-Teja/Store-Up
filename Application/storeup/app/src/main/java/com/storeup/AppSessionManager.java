package com.storeup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

public class AppSessionManager {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private static final String PREF_FILENAME = "CalTrackPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_EMAIL = "email";
    public static final String USER_NAME = "";
    Context _context;

    public AppSessionManager(Context context){
        this._context = context;
        sharedPref = _context.getSharedPreferences(PREF_FILENAME, context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public void createLoginSession(String email){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public void createUserSession(String uName){
        editor.putString(USER_NAME,uName);
        editor.commit();
    }

    public String getUserName(){
        return sharedPref.getString(USER_NAME,null);
    }

    public String getKeyEmail() {
        return sharedPref.getString(KEY_EMAIL,null);
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_EMAIL, sharedPref.getString(KEY_EMAIL, null));
        return user;
    }

    public boolean isLoggedIn(){
        return sharedPref.getBoolean(IS_LOGIN, false);
    }

    public void checkLoginStatus(){
        if(!this.isLoggedIn()){
            Intent i = new Intent(_context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }

    }

    public void logoutUser(){
        editor.clear();
        editor.commit();

        Intent i = new Intent(_context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }
}
