package org.gplvote.trustnet;

public class DocAttestation extends DocSigned {
    public static final String DOC_TYPE = "ATTESTATION";

    public DocAttestation() {
        type = DOC_TYPE;
    }
}
