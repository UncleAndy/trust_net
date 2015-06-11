package org.gplvote.trustnet;

import com.google.gson.annotations.Expose;

public class PacketResponseMessage {
    @Expose public DocMessage doc;
    @Expose public String status;
    @Expose public String error;
    @Expose public Long time;
    @Expose public String sign;
    @Expose public String sign_pub_key_id;
    @Expose public String sign_personal_id;
    @Expose public String pow_nonce;
}
