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
        super(context, "TrustNet", null, 10);
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
                + "current integer DEFAULT 0,"
                + "pow_nonce text,"
                + "t_create INTEGER"
                + ");");
        db.execSQL("CREATE INDEX docs_type_t_create_idx ON docs (type, t_create)");

        // names
        db.execSQL("CREATE TABLE names ("
                + "id integer primary key autoincrement,"
                + "name text,"
                + "personal_id text,"
                + "t_create INTEGER"
                + ");");
        db.execSQL("CREATE INDEX names_personal_id_idx ON names (personal_id)");

        // tags info
        db.execSQL("CREATE TABLE tags_info ("
                + "id text primary key,"
                + "name text,"
                + "info text,"
                + "count INTEGER DEFAULT 0,"
                + "t_update INTEGER"
                + "t_create INTEGER"
                + ");");
        db.execSQL("CREATE INDEX tags_info_id_idx ON tags_info (id)");

        // public_keys
        db.execSQL("CREATE TABLE public_keys ("
                + "id text primary key,"
                + "key text,"
                + "name text,"
                + "t_create INTEGER"
                + ");");
        db.execSQL("CREATE INDEX public_keys_id_idx ON public_keys (id)");

        // message_inbox
        db.execSQL("CREATE TABLE message_inbox ("
                + "id integer primary key autoincrement,"
                + "msg_id text,"
                + "doc text,"
                + "sender text,"
                + "dec_text text,"
                + "t_create INTEGER"
                + ");");
        db.execSQL("CREATE INDEX message_inbox_t_create_idx ON message_inbox (t_create)");
        db.execSQL("CREATE INDEX message_inbox_msg_id_idx ON message_inbox (msg_id)");
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
        }
        if (oldVersion < 3 && newVersion >= 3) {
            db.beginTransaction();
            db.execSQL("ALTER TABLE docs ADD COLUMN sign_personal_id text;");
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        if (oldVersion < 4 && newVersion >= 4) {
            db.beginTransaction();
            db.execSQL("ALTER TABLE docs ADD COLUMN content_id text;");
            db.execSQL("CREATE INDEX docs_content_id_t_create ON docs (content_id, t_create)");
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        if (oldVersion < 5 && newVersion >= 5) {
            db.beginTransaction();
            db.execSQL("CREATE TABLE names ("
                    + "id integer primary key autoincrement,"
                    + "name text,"
                    + "personal_id text,"
                    + "t_create INTEGER"
                    + ");");
            db.execSQL("CREATE INDEX names_personal_id_idx ON names (personal_id)");
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        if (oldVersion < 6 && newVersion >= 6) {
            db.beginTransaction();
            db.execSQL("ALTER TABLE docs ADD COLUMN current integer DEFAULT 0;");
            db.execSQL("CREATE INDEX docs_content_id_t_create ON docs (current, t_create)");
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        if (oldVersion < 7 && newVersion >= 7) {
            db.beginTransaction();
            db.execSQL("ALTER TABLE docs ADD COLUMN pow_nonce text;");
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        if (oldVersion < 8 && newVersion >= 8) {
            db.beginTransaction();
            db.execSQL("CREATE TABLE tags_info ("
                    + "id text primary key,"
                    + "name text,"
                    + "info text,"
                    + "t_create INTEGER"
                    + ");");
            db.execSQL("CREATE INDEX tags_info_id_idx ON tags_info (id)");
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        if (oldVersion < 9 && newVersion >= 9) {
            db.beginTransaction();
            db.execSQL("ALTER TABLE tags_info ADD COLUMN count INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE tags_info ADD COLUMN t_update INTEGER");
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        if (oldVersion < 10 && newVersion >= 10) {
            db.beginTransaction();
            db.execSQL("CREATE TABLE public_keys ("
                    + "id text primary key,"
                    + "key text,"
                    + "name text,"
                    + "t_create INTEGER"
                    + ");");
            db.execSQL("CREATE INDEX public_keys_id_idx ON public_keys (id)");
            db.execSQL("CREATE TABLE message_inbox ("
                    + "id integer primary key autoincrement,"
                    + "msg_id text,"
                    + "doc text,"
                    + "sender text,"
                    + "dec_text text,"
                    + "is_decrypted INTEGER DEFAULT 0,"
                    + "t_create INTEGER"
                    + ");");
            db.execSQL("CREATE INDEX message_inbox_msg_id_idx ON message_inbox (msg_id)");
            db.execSQL("CREATE INDEX message_inbox_t_create_idx ON message_inbox (t_create)");
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }
}
