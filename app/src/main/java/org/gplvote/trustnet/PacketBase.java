package org.gplvote.trustnet;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

public class PacketBase {
    public DocBase doc;
    public String sign;
    public String sign_pub_key_id;

    public boolean send() {
        Log.d("PacketBase send to servers", "");

        int skip = 0;
        boolean sended = false;
        ArrayList<String> servers;
        do {
            servers = Servers.for_send(skip);
            for (int i = 0; i < servers.size(); i++) {
                Log.d("PacketBase send to servers", "Host = "+servers.get(i));
                if (send(servers.get(i)))
                    sended = true;
            }
            skip += servers.size();
        } while (!sended && (servers.size() > 0));
        return(sended);
    }

    public boolean send(String host) {
        Gson gson = new Gson();
        String json = gson.toJson(this);

        Log.d("PacketBase send", "Json = " + json);

        HttpProcessor httpprocessor = new HttpProcessor();
        return (httpprocessor.postData(host, json));
    }

    public static ArrayList<PacketBase> db_list() {
        return(db_list(null));
    }

    public static ArrayList<PacketBase> db_list(String type) {
        ArrayList<PacketBase> list = new ArrayList<PacketBase>();

        SQLiteDatabase db = AMain.db.getWritableDatabase();

        Cursor c;
        if (type == null || type.isEmpty()) {
            c = db.query("docs", new String[]{"id", "type", "doc", "sign", "sign_pub_key_id"}, null, null, null, null, null, null);
        } else {
            c = db.query("docs", new String[]{"id", "type", "doc", "sign", "sign_pub_key_id"}, "type = ?", new String[]{type}, null, null, null, null);
        }

        if (c != null && c.moveToFirst()) {
            do {
                String doc_type = c.getString(c.getColumnIndex("type"));

                DocSigned doc = DocSigned.new_doc_by_type(doc_type);

                String json_doc = c.getString(c.getColumnIndex("doc"));
                String sign = c.getString(c.getColumnIndex("sign"));
                String sign_pub_key_id = c.getString(c.getColumnIndex("sign_pub_key_id"));

                Gson gson = new Gson();
                doc = gson.fromJson(json_doc, doc.getClass());

                PacketBase packet = doc.get_packet(sign, sign_pub_key_id);

                list.add(packet);

            } while (c.moveToNext());
        }

        return(list);
    }
}
