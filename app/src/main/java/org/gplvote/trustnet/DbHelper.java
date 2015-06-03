package org.gplvote.trustnet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = "DB";

    private static DbHelper instance;

    public static DbHelper getInstance(Context context) {
        synchronized (DbHelper.class) {
            if (instance == null) {
                try {
                    instance = new DbHelper(context);
                } catch (Exception e) {
                    Log.e("Settings/Singleton", e.getMessage(), e);
                    instance = new DbHelper(context);
                }
            }
            return instance;
        }
    }

    public DbHelper(Context context) {
        super(context, "TrustNet", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "--- onCreate database ---");

        // servers
        db.execSQL("CREATE TABLE servers ("
                + "id integer primary key autoincrement,"
                + "host text,"
                + "source text,"
                + "t_last_online INTEGER,"
                + "t_create INTEGER"
                + ");");
        db.execSQL("CREATE INDEX servers_source_t_last_idx ON servers (source, t_last_online)");

        // signed docs
        db.execSQL("CREATE TABLE docs ("
                + "id integer primary key autoincrement,"
                + "type text,"
                + "doc text,"
                + "sign text,"
                + "sign_pub_key_id,"
                + "sign_personal_id text,"
                + "content_id text,"
                + "t_create INTEGER"
                + ");");
        db.execSQL("CREATE INDEX docs_type_t_create_idx ON docs (type, t_create)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2 && newVersion >= 2) {
            db.beginTransaction();
            db.execSQL("CREATE TABLE docs ("
                    + "id integer primary key autoincrement,"
                    + "type text,"
                    + "doc text,"
                    + "sign text,"
                    + "sign_pub_key_id,"
                    + "t_create INTEGER"
                    + ");");
            db.execSQL("CREATE INDEX docs_type_t_create_idx ON docs (type, t_create)");
            db.setTransactionSuccessful();
            db.endTransaction();
        } else if (oldVersion < 3 && newVersion >= 3) {
            db.beginTransaction();
            db.execSQL("ALTER TABLE docs ADD COLUMN sign_personal_id text;");
            db.setTransactionSuccessful();
            db.endTransaction();
        } else if (oldVersion < 4 && newVersion >= 4) {
            db.beginTransaction();
            db.execSQL("ALTER TABLE docs ADD COLUMN content_id text;");
            db.execSQL("CREATE INDEX docs_content_id_t_create ON docs (content_id, t_create)");
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }
}
