package org.gplvote.trustnet;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.UUID;

// TODO: Из QR-кода извлекаем публичный ключ и на него формируем сообщение
public class AMessageCreate extends Activity implements View.OnClickListener {
    public static final int MESSAGE_BCRYPT_COST = 8;
    public static final int MESSAGE_HASH_ORDER = 1;
    public static final int MESSAGE_ZERO_BITS = 4;

    private Gson gson;

    private TextView txtPublicKeyId;
    private EditText edtMessage;
    private Button btnBack;
    private Button btnSend;

    private String target_public_key;
    private String target_public_key_id;

    private String message_text;
    private DocSign sign;

    private static TaskCalcPow task_pow = null;
    private static TaskSendMessage task_send = null;

    private final int RESULT_ID_SIGN = 1;
    private final int RESULT_ID_ENCRYPT = 2;
    private final int RESULT_ID_PUBLIC_KEY = 3;
    private final int RESULT_ID_PUBLIC_KEY_FOR_SEND = 4;

    private DataPersonalInfo personal_info;
    private DocMessage doc;
    private PacketSigned packet;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_create);

        gson = new Gson();

        txtPublicKeyId = (TextView) findViewById(R.id.txtMessageCreateTargetPK);
        edtMessage = (EditText) findViewById(R.id.edtMessageCreateText);

        btnBack = (Button) findViewById(R.id.btnMessageCreateBack);
        btnSend = (Button) findViewById(R.id.btnMessageCreateSend);

        btnBack.setOnClickListener(this);
        btnSend.setOnClickListener(this);

        personal_info = AMain.settings.getPersonalInfo();

        Intent i = getIntent();
        String msg_to = i.getStringExtra("MessageTo");
        String msg_text = i.getStringExtra("MessageText");

        if (msg_to == null || msg_to.isEmpty()) {
            // Получение данных только из URL
            Uri d = null;
            if (i != null) d = i.getData();

            target_public_key = null;
            if (d != null && d.getScheme().equals("trustnet") && d.getHost().equals("message")) {
                target_public_key = d.getPath();
                target_public_key = target_public_key.replaceAll("^/", "");

                if (target_public_key == null) {
                    Log.e("ConfirmOther", "Error when get attestation info from URL");
                    throw new RuntimeException("ConfirmOther: Error when get attestation info from URL");
                }

                target_public_key_id = AMain.getPublicKeyIdBase64(target_public_key);

                txtPublicKeyId.setText(target_public_key_id);
                edtMessage.setText("");
            } else if (d != null && d.getScheme().equals("trustnet") && d.getHost().equals("msg")) {
                target_public_key_id = d.getPath();
                target_public_key_id = target_public_key_id.replaceAll("^/", "");

                Intent intent = new Intent(this, AGetPublicKey.class);
                intent.putExtra("PublicKeyId", target_public_key_id);
                startActivityForResult(intent, RESULT_ID_PUBLIC_KEY_FOR_SEND);
            }
        } else {
            target_public_key = null;
            target_public_key_id = msg_to;

            txtPublicKeyId.setText(target_public_key_id);
            edtMessage.setText(text_quoted(msg_text));
        }
    }

    private String text_quoted(String text) {
        text = text.replaceAll("^", "> ");
        text = text.replaceAll("\n", "\n> ");
        return(text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnMessageCreateBack:
                finish();
                break;
            case R.id.btnMessageCreateSend:
                // 1. Подписание письма в открытом виде
                // 2. Шифрование текста письма и замен его в DocMessage.dec_data на шифрованную версию
                // 3. Формирование PoW для пакета. Сложность PoW определяет время хранения письма на серверах
                // 4. Формирование и отправка пакета с письмом

                message_text = edtMessage.getText().toString();

                doc = new DocMessage();
                String[] data_array = new String[] {String.valueOf(System.currentTimeMillis()), target_public_key_id, message_text};
                doc.site = AMain.SIGN_DOC_APP_TYPE;
                doc.dec_data = gson.toJson(data_array);
                doc.template = getString(R.string.template_doc_message);

                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA-256");
                    md.update(doc.dec_data.getBytes());
                    doc.doc_id = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    doc.doc_id = UUID.randomUUID().toString();
                }

                // Отправляем документ на подписание
                ArrayList<DocSigned> docs = new ArrayList<DocSigned>(1);
                docs.add(doc);
                sign_docs(docs);
                break;
        }
    }

    private void sign_docs(ArrayList<DocSigned> docs) {
        Intent intent = new Intent("org.gplvote.signdoc.DO_SIGN");

        intent.putExtra("DocsList", gson.toJson(docs));
        intent.putExtra("LastRecvTime", "");

        startActivityForResult(intent, RESULT_ID_SIGN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;

        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_ID_SIGN) {
                String json_signs = data.getStringExtra("SIGNS");

                Log.d("DOSIGN", "GET SIGN DOCS = " + json_signs);

                DataPersonalInfo pi = AMain.settings.getPersonalInfo();

                ArrayList<DocSign> documents;
                documents = gson.fromJson(json_signs, new TypeToken<ArrayList<DocSign>>() {
                }.getType());

                sign = documents.get(0);

                DocSignConfirm doc_confirm = new DocSignConfirm();

                doc_confirm.site = sign.site;
                doc_confirm.doc_id = sign.doc_id;

                ArrayList<DocSignConfirm> confirms = new ArrayList<DocSignConfirm>();
                confirms.add(doc_confirm);

                Log.d("DOSIGN", "Confirms...");

                // В приложение подписания документов отправляем подтверждение об обработке
                if (confirms.size() > 0) {
                    Intent intent = new Intent("org.gplvote.signdoc.DO_SIGN");
                    intent.putExtra("DocsList", gson.toJson(confirms));
                    intent.putExtra("Command", "SendConfirms");
                    startActivity(intent);
                }

                Log.d("DOSIGN", "Confirmed...");

                if ((target_public_key == null || target_public_key.isEmpty()) && (target_public_key_id != null && !target_public_key_id.isEmpty())) {
                    // Если публичный ключ отсутствует, а его идентификатор есть:
                    // Вызываем специальный активити для получения публичного ключа по идентификатору
                    Intent intent = new Intent(this, AGetPublicKey.class);
                    intent.putExtra("PublicKeyId", target_public_key_id);
                    startActivityForResult(intent, RESULT_ID_PUBLIC_KEY);
                } else {
                    // Далее шифруем текст сообщения
                    encrypt_message();
                }
            } else if (requestCode == RESULT_ID_PUBLIC_KEY) {
                target_public_key = data.getStringExtra("PUBLIC_KEY");

                if (target_public_key != null && !target_public_key.isEmpty()) {
                    encrypt_message();
                } else {
                    setContentView(R.layout.error);
                    TextView txtError = (TextView) findViewById(R.id.txtError);
                    txtError.setText(R.string.error_no_public_key_found);
                };
            } else if (requestCode == RESULT_ID_PUBLIC_KEY_FOR_SEND) {
                target_public_key = data.getStringExtra("PUBLIC_KEY");

                if (target_public_key == null || target_public_key.isEmpty()) {
                    setContentView(R.layout.error);
                    TextView txtError = (TextView) findViewById(R.id.txtError);
                    txtError.setText(R.string.error_no_public_key_found);
                } else {
                    txtPublicKeyId.setText(target_public_key_id);
                    edtMessage.setText("");
                };
            } else if (requestCode == RESULT_ID_ENCRYPT) {
                String encrypted_text = data.getStringExtra("ENCRYPTED_TEXT");

                // После шифрования формируем пакет и делаем для него PoW в фоновой задаче
                String[] data_array = gson.fromJson(doc.dec_data, new TypeToken<String[]>(){}.getType());
                data_array[2] = encrypted_text;
                doc.dec_data = gson.toJson(data_array);

                packet = doc.get_packet(sign.sign, personal_info.public_key_id);

                task_pow = new TaskCalcPow();
                task_pow.execute(doc);
            }
        }
    }

    public void encrypt_message() {
        Log.d("DOSIGN", "Encrypt...");
        Intent intent = new Intent("org.gplvote.signdoc.DO_SIGN");

        intent.putExtra("Command", "Encrypt");
        intent.putExtra("Text", message_text);
        intent.putExtra("PublicKey", target_public_key);

        startActivityForResult(intent, RESULT_ID_ENCRYPT);
    }

    public void send_message(PacketSigned packet) {
        // Отправляем пакет на сервера без сохранения (т.к. читать его нет смысла - текст уже зашифрован)
        // В расшифрованном виде текст надо сохранять сразу после подписания или использовать данные из Sign Doc

        task_send = new TaskSendMessage();
        task_send.execute(packet);
    }

    private class TaskCalcPow extends AsyncTask<DocSigned, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Показываем прогресс формирования PoW для сообщения
            AMessageCreate.this.setContentView(R.layout.progress);
            TextView progress_text = (TextView) AMessageCreate.this.findViewById(R.id.txtProgressInfo);
            progress_text.setText(getString(R.string.txt_message_gen_pow));
        }

        @Override
        protected String doInBackground(DocSigned... params) {
            BigInteger salt = null;
            try {
                salt = BCryptHashMining.mine_salt(params[0].doc_id+":"+params[0].dec_data+":"+params[0].template, MESSAGE_BCRYPT_COST, MESSAGE_HASH_ORDER, MESSAGE_ZERO_BITS);
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (salt != null)
                return(salt.toString());
            else
                return("");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            packet.pow_nonce = result;

            send_message(packet);

            task_pow = null;
        }
    }

    private class TaskSendMessage extends AsyncTask<PacketSigned, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Показываем прогресс отправки
            AMessageCreate.this.setContentView(R.layout.progress);
            TextView progress_text = (TextView) AMessageCreate.this.findViewById(R.id.txtProgressInfo);
            progress_text.setText(getString(R.string.txt_message_send));
        }

        @Override
        protected Boolean doInBackground(PacketSigned... params) {
            Boolean sended = false;

            // Делаем отправку сообщения на несколько серверов
            sended = params[0].send();

            return(sended);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            AMessageCreate.this.finish();

            task_send = null;
        }
    }
}
