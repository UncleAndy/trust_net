package org.gplvote.trustnet;

import android.util.Log;

import com.google.gson.annotations.Expose;

public class PacketPing extends PacketBase {
    @Expose public DocPing doc;

    public PacketPing() {
        super();
        Log.d("PacketPing", "Constructor start");
        doc = new DocPing();
        doc.source = "test";
    }
}
