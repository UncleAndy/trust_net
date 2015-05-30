package org.gplvote.trustnet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
        return(sPref.getString(key, ""));
    }

    public void set(String key, String val) {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(key, val);
        ed.apply();
    }

    public void setPersonalInfo(DataPersonalInfo personal_info) {
        if (personal_info == null) return;

        // Сравниваем старые персональные данные с новыми и запускаем генерацию нового персонального идентификатора если не совпадают
        DataPersonalInfo old_pi = getPersonalInfo();
        if (!old_pi.birthday.equals(personal_info.birthday) ||
            !old_pi.social_number.equals(personal_info.social_number) ||
            !old_pi.tax_number.equals(personal_info.tax_number)) {
            personal_info.personal_id = personal_info.gen_personal_id();
            Log.i("PERSONAL_ID", "Generated value is "+personal_info.personal_id);
        }

        String json_pers_info = gson.toJson(personal_info);
        Log.i("SETTINGS", "Save JSON: "+json_pers_info);
        this.set(PREF_KEY_PERSONAL_INFO, json_pers_info);
    }

    public DataPersonalInfo getPersonalInfo() {
        String json_pers_info = this.get(PREF_KEY_PERSONAL_INFO);

        DataPersonalInfo personal_info = gson.fromJson(json_pers_info, DataPersonalInfo.class);
        if (personal_info == null)
            personal_info = new DataPersonalInfo();

        return(personal_info);
    }
}
