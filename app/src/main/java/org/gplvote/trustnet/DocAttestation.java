package org.gplvote.trustnet;

public class DocAttestation extends DocSigned {
    public static final String DOC_TYPE = "ATTESTATION";

    public String type = DOC_TYPE;

    public PacketAttestation get_packet(String sign, String sign_pub_key_id) {
        return((PacketAttestation) super.get_packet(sign, sign_pub_key_id));
    }
}
