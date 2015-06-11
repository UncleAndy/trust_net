package org.gplvote.trustnet;

import com.google.gson.annotations.Expose;

public class PacketResponseMessagesList {
    @Expose public DocMessagesList doc;
    @Expose public String status;
    @Expose public String error;
    @Expose public Long time;
}
