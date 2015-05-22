package org.gplvote.trustnet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.util.ArrayList;

public class PacketSigned extends PacketBase {
    public DocSigned doc;
    public String sign;
    public String sign_pub_key_id;
    public String sign_personal_id;

    // Create doc from DB
    public PacketSigned(String doc_id) {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        Cursor c = db.query("docs", new String[]{"type", "doc", "sign", "sign_pub_key_id", "sign_personal_id", "t_create"}, "id = ?", new String[]{doc_id}, null, null, null, "1");
        if (c != null) {
            if (c.moveToFirst()) {
                sign = c.getString(c.getColumnIndex("sign"));
                sign_pub_key_id = c.getString(c.getColumnIndex("sign_pub_key_id"));
                sign_personal_id = c.getString(c.getColumnIndex("sign_personal_id"));

                String doc_json = c.getString(c.getColumnIndex("doc"));

                Gson gson = new Gson();
                doc = gson.fromJson(doc_json, DocSigned.class);
            }
            c.close();
        }
    }

    public PacketSigned() {
    }

    // Сохранение подписи документа в базу
    public void update_sign() {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        DocSigned doc_s = (DocSigned) doc;

        ContentValues cv = new ContentValues();
        cv.put("sign", sign);
        cv.put("sign_pub_key_id", sign_pub_key_id);
        cv.put("sign_personal_id", sign_personal_id);

        db.update("docs", cv, "id = ?", new String[] { doc_s.doc_id });
    }


    // Вставляем в базу данные текущего пакета с указанным типом
    // Возвращает идентификатор пакеты или -1
    // В процессе вставки id из базы назначается так-же и в doc.doc_id
    public long insert(String type) {
        SQLiteDatabase db = AMain.db.getWritableDatabase();
        long row_id = db.insert("docs", null, null);

        ((DocSigned) doc).doc_id = String.valueOf(row_id);

        Gson gson = new Gson();
        ContentValues cv = new ContentValues();
        cv.put("type", type);
        cv.put("doc", gson.toJson(doc));
        cv.put("sign", sign);
        cv.put("sign_pub_key_id", sign_pub_key_id);
        cv.put("sign_personal_id", sign_personal_id);

        db.update("docs", cv, "id = ?", new String[] {String.valueOf(row_id)});

        return(row_id);
    }


    public static ArrayList<PacketSigned> db_list() {
        return(db_list(null));
    }

    public static ArrayList<PacketSigned> db_list(String type) {
        ArrayList<PacketSigned> list = new ArrayList<PacketSigned>();

        SQLiteDatabase db = AMain.db.getWritableDatabase();

        Cursor c;
        if (type == null || type.isEmpty()) {
            c = db.query("docs", new String[]{"id", "type", "doc", "sign", "sign_pub_key_id"}, null, null, null, null, null, null);
        } else {
            c = db.query("docs", new String[]{"id", "type", "doc", "sign", "sign_pub_key_id"}, "type = ?", new String[]{type}, null, null, null, null);
        }

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    String doc_type = c.getString(c.getColumnIndex("type"));

                    DocSigned doc = DocSigned.new_doc_by_type(doc_type);

                    String json_doc = c.getString(c.getColumnIndex("doc"));
                    String sign = c.getString(c.getColumnIndex("sign"));
                    String sign_pub_key_id = c.getString(c.getColumnIndex("sign_pub_key_id"));
                    String sign_personal_id = c.getString(c.getColumnIndex("sign_personal_id"));

                    Gson gson = new Gson();
                    doc = gson.fromJson(json_doc, doc.getClass());

                    PacketSigned packet = doc.get_packet(sign, sign_pub_key_id);

                    list.add(packet);

                } while (c.moveToNext());
            }
            c.close();
        }

        return(list);
    }
}
