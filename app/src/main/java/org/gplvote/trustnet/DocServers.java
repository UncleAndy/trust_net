package org.gplvote.trustnet;

import com.google.gson.annotations.Expose;

public class DocServers extends DocBase {
    @Expose String type;
    @Expose String[] list;
}
