package org.gplvote.trustnet;

import android.util.Base64;

import com.google.gson.annotations.Expose;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    @Expose public String personal_id;

    public String gen_personal_id() {
        String hashed_str = birthday+":"+tax_number+":"+social_number;

        try {
            return(personal_hash(hashed_str));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return(null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return(null);
        }
    }

    public String personal_hash(String personal_data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] hashBytes = null;

        BigInteger salt = new BigInteger("0");
        byte[] hash = null;
        int order_counter = PERSONAL_ID_HASH_ORDER;
        while (true) {
            salt = salt.add(new BigInteger("1"));
            BCrypt B = new BCrypt();
            hash = B.crypt_raw(personal_data.getBytes("UTF-8"), salt.toByteArray(), PERSONAL_ID_BCRYPT_COST);
            if (is_zero_bits(hash, PERSONAL_ID_ZERO_BITS)) {
                order_counter--;
                if (order_counter <= 0)
                    return(Integer.toHexString(PERSONAL_ID_BCRYPT_COST)+"$"+Integer.toHexString(PERSONAL_ID_HASH_ORDER)+"$"+Integer.toHexString(PERSONAL_ID_ZERO_BITS)+"$"+Base64.encodeToString(hashBytes, Base64.NO_WRAP));
            }
        }
    }

    private boolean is_zero_bits(byte[] hash, int bits_count) {
        // Сначала проверяем целые байты
        int z_bytes_count = bits_count / 8;
        bits_count -= z_bytes_count*8;
        if (z_bytes_count > 0) {
            for(int i=0; i < z_bytes_count; i++) {
                if (hash[i] != (byte) 0)
                    return(false);
            }
        }

        // Если еще остались биты для проверки - проверяем их в последнем байте
        if ((bits_count > 0) && (hash.length >= z_bytes_count)) {
            byte last = hash[z_bytes_count];
            int mask = 255;
            mask = mask << (8 - bits_count);
            if ((last & mask) > 0)
                return(false);
        }

        return(true);
    }
}
