package org.gplvote.trustnet;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

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
}
