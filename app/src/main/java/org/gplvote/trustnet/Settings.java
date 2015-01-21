package org.gplvote.trustnet;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

public class Settings {
    public static final String APP_PREFERENCES = "org.gplvote.trustnet";

    private final static String PREF_KEY_PERSONAL_INFO = "personal_info";

    private static SharedPreferences sPref;
    private static Settings instance;
    private static Gson gson;

    public static Settings getInstance(Context context) {
        synchronized (Settings.class) {
            if (instance == null) {
                try {
                    instance = new Settings(context);
                } catch (Exception e) {
                    Log.e("Settings/Singleton", e.getMessage(), e);
                    instance = new Settings(context);
                }
            }
            return instance;
        }
    }

    public Settings(Context context) {
        if (sPref == null) sPref = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public String get(String key) {
        String val = sPref.getString(key, "");
        return(val);
    }

    public void set(String key, String val) {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(key, val);
        ed.commit();
    }

    public void setPersonalInfo(PersonalInfo personal_info) {
        if (personal_info == null) return;

        String json_pers_info = gson.toJson(personal_info);
        this.set(PREF_KEY_PERSONAL_INFO, json_pers_info);
    }

    public PersonalInfo getPersonalInfo() {
        String json_pers_info = this.get(PREF_KEY_PERSONAL_INFO);

        PersonalInfo personal_info = gson.fromJson(json_pers_info, PersonalInfo.class);

        return(personal_info);
    }
}
