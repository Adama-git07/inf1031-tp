package com.example.citoyenactif.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF = "citoyen_actif_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_AGENT = "is_agent";

    public static void saveCitizen(Context context, long userId) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putLong(KEY_USER_ID, userId)
                .putBoolean(KEY_IS_AGENT, false)
                .apply();
    }

    public static void saveAgent(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putLong(KEY_USER_ID, -1L)
                .putBoolean(KEY_IS_AGENT, true)
                .apply();
    }

    public static long getUserId(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getLong(KEY_USER_ID, -1L);
    }

    public static boolean isAgent(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_AGENT, false);
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}