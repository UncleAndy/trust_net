package org.gplvote.trustnet;

public class DocMessage extends DocSigned {
    public static final String DOC_TYPE = "MESSAGE";

    public DocMessage() {
        type = DOC_TYPE;
    }
}
