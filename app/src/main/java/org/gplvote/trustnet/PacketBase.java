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
    public DocBase doc;

    public boolean send_hosts(String hosts) {
        if (hosts.isEmpty()) return(false);

        Boolean sended = false;

        if (hosts.equals("*")) {
            sended = send();
        } else {
            String[] host_list = hosts.split(",");

            sended = true;
            for (String host : host_list) {
                if (host.equals("*")) {
                    sended = sended && send();
                } else if (!host.isEmpty()) {
                    sended = sended && send(host);
                }
            }
        }

        return(sended);
    }

    public boolean send() {
        Log.d("PacketBase", "send to servers all");

        int skip = 0;
        boolean sended = false;
        ArrayList<String> servers;
        do {
            servers = Servers.for_send(skip);
            for (int i = 0; i < servers.size(); i++) {
                Log.d("PacketBase", "Send to servers: Host = "+servers.get(i));
                if (send(servers.get(i)))
                    sended = true;
            }
            skip += servers.size();
        } while (!sended && (servers.size() > 0));
        return(sended);
    }

    public boolean send(String host) {
        Log.d("PacketBase", "send to server: "+host);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
        String json = gson.toJson(this, this.getClass());

        Log.d("PacketBase send", "Json = " + json);

        HttpProcessor httpprocessor = new HttpProcessor();
        return (httpprocessor.postData("http://"+host+"/get/time", json));
    }
}
