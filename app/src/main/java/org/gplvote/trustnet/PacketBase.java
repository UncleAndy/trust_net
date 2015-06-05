package org.gplvote.trustnet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class PacketBase {
    public transient DocBase doc;

    public String get_uri() {
        return(Servers.URI_GET_TIME);
    }

    public boolean send_hosts(String hosts) {
        if (hosts.isEmpty()) return(false);

        Boolean sended = false;

        if (hosts.equals("*")) {
            sended = send();
        } else {
            String[] host_list = hosts.split(",");

            Boolean sended_to_all = false;
            String server = null;
            for (String host : host_list) {
                if (host.equals("*")) {
                    sended_to_all = true;
                } else if (!host.isEmpty()) {
                    server = host;
                }
            }

            if (server != null) {
                sended = sended || send(server);
            }

            if (sended_to_all) {
                if (server != null) {
                    send_excluded(server);
                } else {
                    send();
                }
            }
        }

        return(sended);
    }

    // TODO: Исправить зацикливание при неудачно отправке на сервер
    public boolean send_excluded(String exclude_host) {
        Log.d("PacketBase", "send to servers all");

        int skip = 0;
        int try_count = 0;
        boolean sended = false;
        ArrayList<String> servers = Servers.for_send(0, exclude_host);
        if (servers != null) {
            for (int i = 0; i < servers.size(); i++) {
                Log.d("PacketBase", "Send to servers: Host = " + servers.get(i));
                if (send(servers.get(i)))
                    sended = true;
                try_count++;
            }
        }
        return(sended);
    }

    public boolean send() {
        return(send_excluded(null));
    }

    public boolean send(String host) {
        Log.d("PacketBase", "send to server: "+host);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
        String json = gson.toJson(this, this.getClass());

        Log.d("PacketBase send", "Json = " + json);

        HttpProcessor httpprocessor = new HttpProcessor();
        String uri = this.get_uri();
        if (httpprocessor.postData("http://"+host+uri, json)) {
            Servers.set_online(host);
            return (true);
        }
        return (false);
    }
}
