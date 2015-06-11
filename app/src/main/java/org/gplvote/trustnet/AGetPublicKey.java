package org.gplvote.trustnet;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

// TODO: Получение публичного ключа по идентификатору
public class AGetPublicKey extends ActionBarActivity {
    private static TaskGetPublicKey task = null;

    private Gson gson;

    String public_key_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress);

        gson = new Gson();

        TextView txtInfo = (TextView) findViewById(R.id.txtProgressInfo);
        txtInfo.setText(R.string.txt_progress_get_public_key);

        String public_key = null;
        public_key_id = getIntent().getStringExtra("PublicKeyId");

        // Сначала ищем в базе
        public_key = find_public_key_in_db();

        if (public_key != null && !public_key.isEmpty()) {
            return_result(public_key);
        } else {
            // Если не найден в базе - сканирование серверов и получение публичного ключа с них
            task = new TaskGetPublicKey();
            task.execute(public_key_id);
        }
    }

    private void return_result(String public_key) {
        Intent intent = new Intent();
        intent.putExtra("PUBLIC_KEY", public_key);
        setResult(RESULT_OK, intent);
        finish();
    }

    private String find_public_key_in_db() {
        SQLiteDatabase db = DbHelper.getInstance(this).getWritableDatabase();

        String public_key = null;
        Cursor c = db.query("public_keys", new String[]{"key"}, "id = ?", new String[]{public_key_id}, null, null, null, "1");
        if (c != null) {
            if (c.moveToFirst()) {
                public_key = c.getString(c.getColumnIndex("key"));
            }
            c.close();
        }
        return(public_key);
    }

    private void add_public_key_to_db(String public_key) {
        SQLiteDatabase db = DbHelper.getInstance(this).getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("id", public_key_id);
        cv.put("key", public_key);
        cv.put("t_create", System.currentTimeMillis());
        db.insert("public_keys", null, cv);
    }

    private class TaskGetPublicKey extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            SQLiteDatabase db = AMain.db.getWritableDatabase();

            String public_key = null;
            String pub_key_id = params[0];

            // Делаем проход по нескольким серверам и скачиваем ключ
            HttpProcessor http = new HttpProcessor();
            ArrayList<String> servers = Servers.for_check_random(5);
            for(String server: servers) {
                String url_str = null;
                try {
                    url_str = "http://" +server+ Servers.URI_GET_PUBLIC_KEY+"?id=" + URLEncoder.encode(pub_key_id, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String response = http.getData(url_str);

                if (response == null || response.isEmpty()) continue;

                PacketResponsePublicKey packet = gson.fromJson(response, PacketResponsePublicKey.class);

                if (packet.doc == null || packet.doc.public_key == null || packet.doc.public_key.isEmpty())
                    continue;

                public_key = packet.doc.public_key;
            }

            return(public_key);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null && !result.isEmpty()) {
                // Кэшируем в локальной базе
                add_public_key_to_db(result);
            }

            return_result(result);

            task = null;
        }
    }
}
