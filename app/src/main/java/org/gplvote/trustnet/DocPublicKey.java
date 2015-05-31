package org.gplvote.trustnet;

public class DocPublicKey extends DocSigned {
    public static final String DOC_TYPE = "PUBLIC_KEY";

    public DocPublicKey() {
        type = DOC_TYPE;
    }
}
