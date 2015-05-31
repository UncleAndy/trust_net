package org.gplvote.trustnet;

import com.google.gson.annotations.Expose;

public class PacketResponse extends PacketBase {
    public DocBase doc;
    public String status;
    public String error;
    public Long time;
}
