package org.gplvote.trustnet;

public class DocSign extends DocBase {
    public String type = "SIGN";
    public String site;
    public String doc_id;   // Внутренний (клиента) идентификатор документа
    public String sign;     // Подпись в Base64 для sha256(данные+шаблон)
}
