package org.gplvote.trustnet;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Settings {
    public static final String APP_PREFERENCES = "org.gplvote.trustnet";

    private final static String PREF_KEY_FIRST_NAME = "first_name";
    private final static String PREF_KEY_MIDDLE_NAME = "middle_name";
    private final static String PREF_KEY_LAST_NAME = "last_name";
    private final static String PREF_KEY_BIRTHDAY = "birthday";
    private final static String PREF_KEY_TAX_NUMBER = "tax_number";

    private static SharedPreferences sPref;
    private static Settings instance;

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

        this.set(PREF_KEY_FIRST_NAME, personal_info.first_name);
        this.set(PREF_KEY_MIDDLE_NAME, personal_info.middle_name);
        this.set(PREF_KEY_LAST_NAME, personal_info.last_name);
        this.set(PREF_KEY_BIRTHDAY, personal_info.birthday);
        this.set(PREF_KEY_TAX_NUMBER, personal_info.tax_number);
    }

    public PersonalInfo getPersonalInfo(PersonalInfo personal_info) {
        personal_info.first_name = this.get(PREF_KEY_FIRST_NAME);
        personal_info.middle_name = this.get(PREF_KEY_MIDDLE_NAME);
        personal_info.last_name = this.get(PREF_KEY_LAST_NAME);
        personal_info.birthday = this.get(PREF_KEY_BIRTHDAY);
        personal_info.tax_number = this.get(PREF_KEY_TAX_NUMBER);

        return(personal_info);
    }

    public PersonalInfo getPersonalInfo() {
        return(getPersonalInfo(new PersonalInfo()));
    }
}
