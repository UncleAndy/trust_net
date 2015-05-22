package org.gplvote.trustnet;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Servers {
    public static final String SOURCE_DIRECT = "direct";
    public static final String SOURCE_SERVER = "server";

    public static final String URI_GET_SERVERS = "/get/servers";
    public static final String URI_GET_TIME = "/get/time";
    public static final String URI_SEND_PACKET = "/put/packet";

    public static ArrayList<String> for_send() {
        return for_send(0, null);
    }

    public static ArrayList<String> for_send(String skip_server) {
        return for_send(0, skip_server);
    }

    // Возвращает список севреров для отправки пакета
    public static ArrayList<String> for_send(int skip_count, String skip_server) {
        ArrayList<String> servers = new ArrayList<String>();

        SQLiteDatabase db = AMain.db.getWritableDatabase();

        Cursor c;
        if (skip_server.isEmpty()) {
            c = db.query("servers", new String[]{"id", "host"}, null, null, null, null, "RANDOM()", "10");
        } else {
            c = db.query("servers", new String[]{"id", "host"}, "host != ?", new String[]{skip_server}, null, null, "RANDOM()", "10");
        }

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    servers.add(c.getString(c.getColumnIndex("host")));
                } while (c.moveToNext());
            }
            c.close();
        }

        if (servers.size() > 0) return(servers);
        return(null);
    }

    // Добавлем сервер в локальную БД
    public static boolean add(String host, String source) {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("host", host);
        cv.put("source", source);
        cv.put("t_create", System.currentTimeMillis());
        long row_id = db.insert("servers", null, cv);

        return(row_id > 0);
    }

    // Делаем запрос на сервер и получаем в ответ список его доверенных серверов
    // Доверенные сервера добавляем в локальный список серверов
    public static int add_from_server(String host) {
        String[] servers = null;

        HttpProcessor httpprocessor = new HttpProcessor();
        String response = httpprocessor.getData("http://"+host+Servers.URI_GET_SERVERS);
        if (!response.isEmpty()) {
            Gson gson = new Gson();
            PacketResponseServers pack = gson.fromJson(response, PacketResponseServers.class);

            if (pack.doc != null && pack.doc.list != null)
                servers = pack.doc.list;
        }

        int counter = 0;
        if (servers != null && servers.length > 0) {
            for (String server : servers) {
                add(server, SOURCE_SERVER);
                counter++;
            }
        }

        return(counter);
    }

    // Установка для сервера последнего времени в онлайн
    public static boolean set_online(String host) {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        Cursor c = db.query("servers", new String[]{"id", "host"}, "host = ?", new String[]{host}, null, null, null, "1");
        if (c != null) {
            if (c.moveToFirst()) {
                Long row_id = c.getLong(c.getColumnIndex("id"));

                ContentValues cv = new ContentValues();
                cv.put("t_last_online", System.currentTimeMillis());

                return(db.update("docs", cv, "id = ?", new String[] {String.valueOf(row_id)}) > 0);
            }
            c.close();
        }
        return(false);
    }
}
