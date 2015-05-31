package org.gplvote.trustnet;

public class DocTrust extends DocSigned {
    public static final String DOC_TYPE = "TRUST";

    public DocTrust() {
        type = DOC_TYPE;
    }
}
