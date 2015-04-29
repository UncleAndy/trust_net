package org.gplvote.trustnet;

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
    }

    public boolean send(String host) {
        Gson gson = new Gson();
        String json = gson.toJson(this);

        Log.d("PacketBase send", "Json = " + json);

        HttpProcessor httpprocessor = new HttpProcessor();
        return (httpprocessor.postData(host, json))
    }
}
