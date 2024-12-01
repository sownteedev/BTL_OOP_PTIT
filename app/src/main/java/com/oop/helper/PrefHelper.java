package com.oop.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefHelper {

    public static final String PREF_NAME = "user-settings.pref";
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor sharedPrefEditor;

    public PrefHelper(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.sharedPrefEditor = preferences.edit();
    }

    public void setPreference(String key, int value) {
        sharedPrefEditor.putInt(key, value);
        sharedPrefEditor.commit();
    }

    public void setPreference(String key, String value) {
        sharedPrefEditor.putString(key, value);
        sharedPrefEditor.commit();
    }

    public int getIntPreference(String key, int failSafe) {
        return preferences.getInt(key, failSafe);
    }


    public double getDoublePreference(String key) {
        return Double.parseDouble(preferences.getString(key, "-1"));
    }
}
