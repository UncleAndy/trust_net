package org.gplvote.trustnet;

import org.apache.http.HttpResponse;

public interface HttpProcessor {
    public boolean onResponse(int id, HttpResponse response);
}
