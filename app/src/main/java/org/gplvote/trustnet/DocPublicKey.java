package org.gplvote.trustnet;

public class DocPublicKey extends DocSigned {
    public static final String DOC_TYPE = "PUBLIC_KEY";

    public String type = DOC_TYPE;

    public PacketPublicKey get_packet(String sign, String sign_pub_key_id) {
        return((PacketPublicKey) super.get_packet(sign, sign_pub_key_id));
    }
}
