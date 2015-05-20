package org.gplvote.trustnet;

import com.google.gson.annotations.Expose;

public class PacketTag extends PacketSigned {
    @Expose public DocTag doc;
    @Expose public String sign;
    @Expose public String sign_pub_key_id;
    @Expose public String sign_personal_id;
}
