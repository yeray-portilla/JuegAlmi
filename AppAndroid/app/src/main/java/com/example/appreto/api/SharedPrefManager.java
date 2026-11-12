package com.example.appreto.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "juegalmi_prefs";
    private static final String KEY_TOKEN = "key_token";
    private static final String KEY_USERNAME = "key_username";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_ROLES = "key_roles";

    private static SharedPrefManager instance;
    private Context ctx;

    private SharedPrefManager(Context context) {
        ctx = context.getApplicationContext();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void saveUser(String token, String username, String email, List<String> roles) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        Set<String> conjuntoRoles = new HashSet<>();
        if (roles != null) {
            conjuntoRoles.addAll(roles);
        }
        editor.putStringSet(KEY_ROLES, conjuntoRoles);
        editor.apply();
    }

    public String getToken() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public String getUsername() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, "Usuario");
    }

    public String getEmail() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_EMAIL, "email@ejemplo.com");
    }

    public Set<String> obtenerRoles() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet(KEY_ROLES, new HashSet<>());
    }

    public boolean esAdmin() {
        return obtenerRoles().contains("ROLE_ADMIN");
    }

    public void clear() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
