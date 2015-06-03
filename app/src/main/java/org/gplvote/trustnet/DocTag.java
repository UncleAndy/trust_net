package org.gplvote.trustnet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DocTag extends DocSigned {
    public static final String DOC_TYPE = "TAG";

    public DocTag() {
        type = DOC_TYPE;
    }

    @Override
    public String content_id() {
        Gson gson = new Gson();
        String[] data_array = gson.fromJson(dec_data, new TypeToken<String[]>(){}.getType());

        String hash_str = String.format("%s:%s:%s", data_array[0], data_array[2], data_array[3]);

        return(content_id(hash_str));
    }
}
