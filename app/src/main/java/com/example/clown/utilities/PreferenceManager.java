package com.example.clown.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.clown.models.User;
import com.google.gson.Gson;

public class PreferenceManager {
    public static final String TAG = PreferenceManager.class.getName();

    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_REFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void registerChangesListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        Log.e(TAG, "PreferenceManager listener registered!");
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unRegisterChangesListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        Log.e(TAG, "PreferenceManager listener unregistered!");
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    // region -------------- ACCESSORS --------------
    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public void putUser(User value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(value);
        editor.putString(Constants.KEY_CURRENT_USER, json);
        editor.apply();
    }

    public User getUser() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Constants.KEY_CURRENT_USER, null);
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
