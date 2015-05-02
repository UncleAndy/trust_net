package org.gplvote.trustnet;

public class DocTrust extends DocSigned {
    public static final String DOC_TYPE = "TRUST";

    public String type = DOC_TYPE;

    public PacketTrust get_packet(String sign, String sign_pub_key_id) {
        return((PacketTrust) super.get_packet(sign, sign_pub_key_id));
    }
}
