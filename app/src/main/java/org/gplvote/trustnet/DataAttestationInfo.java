package org.gplvote.trustnet;

import com.google.gson.annotations.Expose;

public class DataAttestationInfo {
    @Expose public String nm;   // name
    @Expose public String bd;   // birthday
    @Expose public String tn;  // tax_id
    @Expose public String sn;  // social_id
    @Expose public String pkid; // public_key_id
    @Expose public String pid; // personal_id
}

