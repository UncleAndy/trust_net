package org.gplvote.trustnet;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class DataPersonalInfo {
    public static final int PERSONAL_ID_BCRYPT_COST = 8;
    public static final int PERSONAL_ID_HASH_ORDER = 1;
    public static final int PERSONAL_ID_ZERO_BITS = 8;

    @Expose public String name;
    @Expose public String birthday;
    @Expose public String tax_number;
    @Expose public String social_number;
    @Expose public String public_key_id;
    @Expose public String public_key;
    @Expose public String cancel_public_key_id;
    @Expose public String cancel_public_key;
    @Expose public String personal_id;

    public String gen_personal_id() {
        String hashed_str = birthday+":"+tax_number+":"+social_number;
        Log.d("gen_personal_id", "For str = " + hashed_str);

        try {
            return(BCryptHashMining.mine_hash(hashed_str, PERSONAL_ID_BCRYPT_COST, PERSONAL_ID_HASH_ORDER, PERSONAL_ID_ZERO_BITS, false));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return(null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return(null);
        }
    }
}
