package org.gplvote.trustnet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/* Activity for send doc for sign and send doc to server after sign */
public class ASendSign extends Activity {

    private static class QueueRecord {
        public String doc_id;
        public boolean sign_req;
        public boolean send_req;
        public String hosts;

        public QueueRecord(String _doc_id, String _hosts, Boolean _sign_req) {
            doc_id = _doc_id;
            hosts = _hosts;
            sign_req = _sign_req;
            send_req = (hosts != null && !hosts.equals(""));
        }
    }

    private static ArrayList<QueueRecord> queue;

    // hosts:
    // null || "" - send not required
    // 'host.ru,*' - to server host.ru and some random servers set (set size - from setup values)
    // 'host.ru' - only to server.host.ru
    // '*' - to any random servers set (set size - from setup values)
    static public void add_to_queue(DocSigned doc, String hosts, Boolean sign_req) {
        if (queue == null)
            queue = new ArrayList<QueueRecord>();

        QueueRecord queue_doc = new QueueRecord(doc.doc_id, hosts, sign_req);

        queue.add(queue_doc);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.servers);

        // 1. Из очереди документов queue выбираются те, которые требуют подписания
        // 2. По БД проверяется что они еще не имеют подписи
        // 3. Список документов, требующих подписания, отправляется на подписание
        // 4. После получения подписей докумениов (или пустого списка) все подписанные документы
        // отправляются на нужные сервера в соответствии с hosts

        ArrayList<DocSigned> documents_for_sign = new ArrayList<DocSigned>();

        for (int i = 0; i < queue.size(); i++) {
            QueueRecord req = queue.get(i);
            if (req.sign_req) {
                PacketSigned pack = new PacketSigned(req.doc_id);
                if (pack.doc != null && pack.sign.isEmpty()) {
                    pack.doc.type = "SIGN REQUEST";
                    documents_for_sign.add(pack.doc);
                }
            }
        }

        if (documents_for_sign.size() > 0) {
            // Если есть документы на подписание - вызываем для них процедуру подписания
            sign_docs(documents_for_sign);
        } else {
            // Иначе сразу вызываем процедуру отправки документов из очереди на сервер(а)
            send_queue_docs();
        }
    }

    private void sign_docs(ArrayList<DocSigned> docs) {
        Intent intent = new Intent("org.gplvote.signdoc.DO_SIGN", Uri.parse("signdoc://sign_array"));

        Gson gson = new Gson();
        intent.putExtra("DocsList", gson.toJson(docs));
        intent.putExtra("LastRecvTime", "");

        startActivityForResult(intent, 1);
    }

    private void send_queue_docs() {
        // Цикл по очереди, выборка документов из базы и отправка всех подписанных по одному
        for(int i=0; i < queue.size();i++) {
            QueueRecord req = queue.get(i);
            if (req.send_req) {
                PacketSigned pack = new PacketSigned(req.doc_id);
                if (pack.doc != null && !pack.sign.isEmpty()) {
                    // Отправляем пакет
                    if (pack.send_hosts(req.hosts)) {
                        // После удачной отправки удаляем пакет из очереди
                        queue.remove(i);
                        i--;
                    }
                }
            }
        }

        if (queue.size() > 0) {
            String doc_ids = "";
            String sep = "";
            for(int i=0;i<queue.size();i++) {
                QueueRecord req = queue.get(i);
                Log.e("SEND_QUEUE", "Can not send doc "+req.doc_id);
            }
        }

        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;

        if (resultCode == RESULT_OK) {
            String json_signs = data.getStringExtra("SIGNS");

            Log.d("DOSIGN", "GET SIGN DOCS = " + json_signs);

            DataPersonalInfo pi = AMain.settings.getPersonalInfo();

            ArrayList<DocSign> documents;
            Gson gson = new Gson();
            documents = gson.fromJson(json_signs, new TypeToken<ArrayList<DocSign>>() {}.getType());

            ArrayList<DocSignConfirm> confirms = new ArrayList<DocSignConfirm>();
            for (int i = 0; i < documents.size(); i++) {
                DocSign sign = documents.get(i);

                DocSignConfirm doc_confirm = new DocSignConfirm();

                doc_confirm.site = sign.site;
                doc_confirm.doc_id = sign.doc_id;

                confirms.add(doc_confirm);

                PacketSigned pack = new PacketSigned(sign.doc_id);
                pack.sign = sign.sign;
                pack.sign_pub_key_id = pi.public_key_id;
                pack.sign_personal_id = pi.personal_id();
                pack.update_sign();
            }

            // В приложение подписания документов отправляем подтверждение об обработке
            if (confirms.size() > 0) {
                Intent intent = new Intent("org.gplvote.signdoc.DO_SIGN", Uri.parse("signdoc://sign_array"));
                intent.putExtra("DocsList", gson.toJson(confirms));
                intent.putExtra("ConfirmMode", "1");
                startActivity(intent);
            }

            send_queue_docs();
        }
    }
}