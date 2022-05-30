package com.example.clown.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.clown.models.User;
import com.google.gson.Gson;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_REFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // region -------------- ACCESSORS --------------
    public void putBoolean(String key,Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key,false);
    }

    public void putUser( User value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(value);
        editor.putString(Constants.KEY_USER, json);
        editor.apply();
    }

    public User getUser() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Constants.KEY_USER, null);
        return gson.fromJson(json, User.class);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }
    // endregion
}
