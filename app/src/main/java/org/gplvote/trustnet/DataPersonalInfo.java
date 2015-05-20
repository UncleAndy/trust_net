package org.gplvote.trustnet;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DataPersonalInfo {
    public String name;
    public String birthday;
    public String tax_number;
    public String social_number;
    public String public_key_id;
    public String public_key;

    public String personal_id() {
        String hashed_str = birthday+":"+tax_number+":"+social_number;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(hashed_str.getBytes());
            byte[] hashBytes = md.digest();
            return(Base64.encodeToString(hashBytes, Base64.NO_WRAP));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return(null);
        }
    }
}
