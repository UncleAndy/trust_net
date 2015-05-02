package org.gplvote.trustnet;

import java.util.ArrayList;

public class DocSigned extends DocBase {
    public String type;
    public String site;
    public String doc_id;       // Внутренний (клиента) идентификатор документа
    public String user_key_id;  // Идентификатор открытого ключа пользователя
    public String data;         // Данные - это зашифрованный JSON массив строк
    public String dec_data;     // Расшифрованные данные в строковом виде (JSON массив строк)
    public String template;     // Формат: До первого перевода строки - код типа шаблона:
    // LIST - список строк с описанием каждого значения документа (по порядку, построчно)
    // HTML - html шаблон со значаениями вставляеммыми на место <%data_N%> (N - номер элемента в массиве данных начиная с 0)
    public String sign_url;     // Если заполнено, то подпись нужно отправлять по данному URL POST запросом

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
                return(null);
        }
    }

    public PacketBase get_packet(String sign, String sign_pub_key_id) {
        switch (this.type) {
            case DocAttestation.DOC_TYPE:
                PacketAttestation packet = new PacketAttestation();
                packet.doc = (DocAttestation) this;
                packet.sign = sign;
                packet.sign_pub_key_id = sign_pub_key_id;
                return(packet);
            case DocTrust.DOC_TYPE:
                PacketTrust packet = new PacketTrust();
                packet.doc = (DocTrust) this;
                packet.sign = sign;
                packet.sign_pub_key_id = sign_pub_key_id;
                return(packet);
            case DocTag.DOC_TYPE:
                PacketTag packet = new PacketTag();
                packet.doc = (DocTag) this;
                packet.sign = sign;
                packet.sign_pub_key_id = sign_pub_key_id;
                return(packet);
            case DocMessage.DOC_TYPE:
                PacketMessage packet = new PacketMessage();
                packet.doc = (DocMessage) this;
                packet.sign = sign;
                packet.sign_pub_key_id = sign_pub_key_id;
                return(packet);
        }
        return(null);
    }
}
