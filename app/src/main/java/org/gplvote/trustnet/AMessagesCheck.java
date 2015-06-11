package org.gplvote.trustnet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Получение и расшифровка сообщений
public class AMessagesCheck extends ActionBarActivity {
    private static class Message {
        public String id;
        public String enc_text;
    }

    private static TaskCheckMessages task = null;

    private static DataPersonalInfo personal_info;

    private ArrayList<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress);

        personal_info = Settings.getInstance(this).getPersonalInfo();

        messages = new ArrayList<Message>();

        TextView txtInfo = (TextView) findViewById(R.id.txtProgressInfo);
        txtInfo.setText(R.string.txt_messages_check);

        task = new TaskCheckMessages();
        task.execute();
    }

    private void decrypt_messages() {
        // Отправляем на расшифровку только первое сообщение
        // Далее - в виде итерации

        if (messages.size() > 0) {
            Intent intent = new Intent("org.gplvote.signdoc.DO_DECRYPT");
            intent.putExtra("EncryptedText", messages.get(0).enc_text);
            startActivityForResult(intent, 1);
        } else {
            AMessages.instance.reload_data();
            finish();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;

        if (resultCode == RESULT_OK) {
            SQLiteDatabase db = AMain.db.getWritableDatabase();
            String dec_text = data.getStringExtra("DECRYPTED_TEXT");

            Message msg = messages.get(0);

            // Сохраняем в базу расшифрованный текст

            ContentValues cv = new ContentValues();
            cv.put("dec_text", dec_text);
            cv.put("is_decrypted", 1);
            db.update("message_inbox", cv, "id = ?", new String[]{msg.id});

            messages.remove(0);

            decrypt_messages();
        }
    }

    private class TaskCheckMessages extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // 1. Формируем массив не расшифрованных сообщений из локальной БД
            // 2. С ограничением по времени делается проход по нескольким серверам
            // 3. С серверов скачиваются сообщения для данного пользователя и добавляются в БД как НЕ расшифрованные
            // 4. Новые не расшифрованные сообщения добавляются в массив не расшифрованных сообщений
            // 5. Отправлаем все сообщения на расшифровку в Sign Doc и удачно расшифрованные обновляем в БД
            // 6. Обновляем данные в списке входящих сообщений
            SQLiteDatabase db = AMain.db.getWritableDatabase();
            Gson gson = new Gson();

            // 1. Формируем массив не расшифрованных сообщений из локальной БД
            Cursor c = db.query("message_inbox", new String[]{"id", "doc", "t_create"}, "is_decrypted = 0", null, null, null, "t_create desc", "100");
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        String doc_json = c.getString(c.getColumnIndex("doc"));
                        DocSigned doc = gson.fromJson(doc_json, DocSigned.class);
                        String[] doc_data = gson.fromJson(doc.dec_data, String[].class);

                        Message msg = new Message();
                        msg.id = c.getString(c.getColumnIndex("id"));
                        msg.enc_text = doc_data[2];

                        messages.add(msg);
                    } while (c.moveToNext());
                }
                c.close();
            }

            // 2. Делается проход по нескольким серверам и скачиваются сообщения для вашего ключа
            HttpProcessor http = new HttpProcessor();
            ArrayList<String> servers = Servers.for_check_random(5);
            for(String server: servers) {
                String url_str = "http://" +server+Servers.URI_GET_MESSAGES_LIST+"?id="+personal_info.public_key_id;

                String response = http.getData(url_str);

                // В ответе - массив со списком идентификаторов сообщений
                if (response == null || response.isEmpty()) continue;

                PacketResponseMessagesList packet = gson.fromJson(response, PacketResponseMessagesList.class);

                if (packet.doc == null || packet.doc.list == null || packet.doc.list.length <= 0)
                    continue;

                // Определяем какие сообщения новые
                ArrayList<String> new_msg_ids = new ArrayList<String>();
                for (String msg_id: packet.doc.list) {
                    // Проверяем наличие такого сообщения в БД
                    c = db.query("message_inbox", new String[]{"id"}, "msg_id = ?", new String[]{"msg_id"}, null, null, null, "1");
                    if (c == null || !c.moveToFirst())
                        new_msg_ids.add(msg_id);
                    if (c != null) c.close();
                }

                // Запрашиваем новые сообщения с сервера
                for (String msg_id: new_msg_ids) {
                    url_str = "http://" +server+Servers.URI_GET_MESSAGE+"?id="+msg_id;

                    response = http.getData(url_str);

                    if (response == null || response.isEmpty()) continue;

                    PacketResponseMessage packet_msg = gson.fromJson(response, PacketResponseMessage.class);

                    if (packet_msg.doc == null)
                        continue;

                    // Сразу сохраняем сообщение в message_inbox как нерасшифрованное и добавляем его в массив messages
                    ContentValues cv = new ContentValues();
                    cv.put("msg_id", msg_id);
                    cv.put("doc", gson.toJson(packet_msg.doc));
                    cv.put("from", packet_msg.sign_pub_key_id);
                    cv.put("t_create", System.currentTimeMillis());
                    long row_id = db.insert("message_inbox", null, cv);

                    String doc_json = c.getString(c.getColumnIndex("doc"));
                    DocSigned doc = gson.fromJson(doc_json, DocSigned.class);
                    String[] doc_data = gson.fromJson(doc.dec_data, String[].class);

                    Message msg = new Message();
                    msg.id = String.valueOf(row_id);
                    msg.enc_text = doc_data[2];
                    messages.add(msg);
                }
            }

            return(null);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Для всех сообщений из messages производим расшифровку

            AMessagesCheck.this.decrypt_messages();

            task = null;
        }
    }
}
