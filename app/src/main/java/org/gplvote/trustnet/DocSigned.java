package org.gplvote.trustnet;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class DocSigned {
    @Expose public String type;
    @Expose public String site;
    @Expose public String doc_id;       // Внутренний (клиента) идентификатор документа
    @Expose public String user_key_id;  // Идентификатор открытого ключа пользователя
    @Expose public String data;         // Данные - это зашифрованный JSON массив строк
    @Expose public String dec_data;     // Расшифрованные данные в строковом виде (JSON массив строк)
    @Expose public String template;     // Формат: До первого перевода строки - код типа шаблона:
    // LIST - список строк с описанием каждого значения документа (по порядку, построчно)
    // HTML - html шаблон со значаениями вставляеммыми на место <%data_N%> (N - номер элемента в массиве данных начиная с 0)
    @Expose public String sign_url;     // Если заполнено, то подпись нужно отправлять по данному URL POST запросом

    public ArrayList<DocSigned> list_from_db() {
        return null;
    }

    public static DocSigned new_doc_by_type(String type) {
        switch (type) {
            case DocAttestation.DOC_TYPE:
                return(new DocAttestation());
            case DocTrust.DOC_TYPE:
                return(new DocTrust());
            case DocTag.DOC_TYPE:
                return(new DocTag());
            case DocMessage.DOC_TYPE:
                return(new DocMessage());
            default:
                return(new DocSigned());
        }
    }

    public PacketSigned get_packet(String sign, String sign_pub_key_id) {
        Gson gson = new Gson();
        String[] desc_data_array = gson.fromJson(this.dec_data, String[].class);
        PacketSigned packet_pk = new PacketSigned();
        packet_pk.doc = this;
        packet_pk.sign = sign;
        packet_pk.sign_pub_key_id = sign_pub_key_id;
        packet_pk.sign_personal_id = desc_data_array[0];
        return(packet_pk);
    }

    public PacketSigned get_packet() {
        return(get_packet(null, null));
    }

    // Генерирует хэш sha512 от данных, которые идентифицируют пакет как принадлежащий к одному контенту
    // Пакеты одного контента при повторении в более позднее время заменяют друг друга:
    // старая версия становится не актуальна. Content_id у разных версий одного пакета должен быть одинаковый.
    // Данные, используемые для формирования этого content_id зависят от типа пакета
    public String content_id() {
        // Данные располагаются в строке через ":"
        Gson gson = new Gson();
        String[] data_array = gson.fromJson(dec_data, new TypeToken<String[]>(){}.getType());

        String hash_str = "";
        String sep = "";
        for (String str : data_array) {
            hash_str = hash_str + sep + str;
            sep = ":";
        }

        return(content_id(hash_str));
    }

    public String content_id(String hashed_string) {
        MessageDigest digest = null;
        byte[] hash = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            hash = new byte[0];
            hash = digest.digest(hashed_string.getBytes("UTF-8"));
            return(Base64.encodeToString(hash, Base64.NO_WRAP));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return(null);
    }
}
