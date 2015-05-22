package org.gplvote.trustnet;

import com.google.gson.annotations.Expose;

public class PacketResponse extends PacketBase {
    @Expose public DocBase doc;
    @Expose public String status;
    @Expose public String error;
    @Expose public Long time;
}
