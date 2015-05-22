package org.gplvote.trustnet;

import com.google.gson.annotations.Expose;

public class PacketResponseServers extends PacketResponse {
    @Expose public DocServers doc;
    @Expose public String status;
    @Expose public String error;
    @Expose public Long time;
}
