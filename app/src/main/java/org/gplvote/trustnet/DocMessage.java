package org.gplvote.trustnet;

public class DocMessage extends DocSigned {
    public static final String DOC_TYPE = "MESSAGE";

    public String type = DOC_TYPE;

    public PacketMessage get_packet(String sign, String sign_pub_key_id) {
        return((PacketMessage) super.get_packet(sign, sign_pub_key_id));
    }
}
