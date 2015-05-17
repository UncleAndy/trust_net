package org.gplvote.trustnet;

import com.google.gson.annotations.Expose;

public class DocPing extends DocBase {
    public static final String DOC_TYPE = "PING";

    @Expose String type = DOC_TYPE;
    @Expose String source;
}
