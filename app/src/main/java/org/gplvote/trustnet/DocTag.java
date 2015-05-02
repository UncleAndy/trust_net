package org.gplvote.trustnet;

public class DocTag extends DocSigned {
    public static final String DOC_TYPE = "TAG";

    public String type = DOC_TYPE;

    public PacketTag get_packet(String sign, String sign_pub_key_id) {
        return((PacketTag) super.get_packet(sign, sign_pub_key_id));
    }
}
