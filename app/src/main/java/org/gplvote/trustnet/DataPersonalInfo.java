package org.gplvote.trustnet;

import android.os.AsyncTask;
import android.util.Base64;

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
        BigInteger salt = new BigInteger("0");
        byte[] hash = null;
        int order_counter = PERSONAL_ID_HASH_ORDER;
        while (true) {
            salt = salt.add(new BigInteger("1"));
            BCrypt B = new BCrypt();
            hash = B.crypt_raw(personal_data.getBytes("UTF-8"), big_int_to_bytes(salt, 16), PERSONAL_ID_BCRYPT_COST);
            if (is_zero_bits(hash, PERSONAL_ID_ZERO_BITS)) {
                order_counter--;
                if (order_counter <= 0)
                    return(Integer.toHexString(PERSONAL_ID_BCRYPT_COST)+"$"+Integer.toHexString(PERSONAL_ID_HASH_ORDER)+"$"+Integer.toHexString(PERSONAL_ID_ZERO_BITS)+"$"+Base64.encodeToString(hash, Base64.NO_WRAP));
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

        // Если еще остались биты для проверки - проверяем их в следующем байте
        if ((bits_count > 0) && (hash.length >= z_bytes_count)) {
            byte last = hash[z_bytes_count];
            byte mask = (byte) 255;
            mask = (byte) (mask << (8 - bits_count));
            if ((last & mask) > 0)
                return(false);
        }

        return(true);
    }

    private byte[] big_int_to_bytes(BigInteger big_int, int bytes_count) {
        byte[] array = big_int.toByteArray();
        byte[] tmp = new byte[bytes_count];
        Arrays.fill(tmp, (byte) 0);
        System.arraycopy(array, 0, tmp, 0, array.length);
        return(tmp);
    }
}
