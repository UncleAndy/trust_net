package org.gplvote.trustnet;

import com.google.gson.annotations.Expose;

public class PacketResponsePublicKey {
    @Expose public DocResponsePublicKey doc;
    @Expose public String status;
    @Expose public String error;
    @Expose public Long time;
}
