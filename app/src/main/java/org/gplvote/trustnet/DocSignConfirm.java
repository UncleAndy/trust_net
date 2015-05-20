package org.gplvote.trustnet;

public class DocSignConfirm extends DocBase {
    public String type = "SIGN_CONFIRM";
    public String site;
    public String doc_id;   // Внутренний (клиента) идентификатор документа
}
