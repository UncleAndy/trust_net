package org.gplvote.trustnet;

import com.google.gson.annotations.Expose;

public class PacketList extends PacketBase {
    @Expose public DocPacketsList doc;
    @Expose public String sign;
    @Expose public String sign_pub_key_id;
}
