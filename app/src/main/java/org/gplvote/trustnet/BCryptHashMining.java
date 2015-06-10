package org.gplvote.trustnet;

import android.os.Message;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class BCryptHashMining {
    private static class MineResult {
        String hash;
        BigInteger salt;
    }

    public static String mine_hash(String data, int bcrypt_cost, int mine_hash_order, int mine_zero_bits_count, boolean loop_mode) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MineResult result = mine(data, bcrypt_cost, mine_hash_order, mine_zero_bits_count, loop_mode);
        return(result.hash);
    }

    public static BigInteger mine_salt(String data, int bcrypt_cost, int mine_hash_order, int mine_zero_bits_count) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MineResult result = mine(data, bcrypt_cost, mine_hash_order, mine_zero_bits_count, false);
        return(result.salt);
    }

    public static MineResult mine(String data, int bcrypt_cost, int mine_hash_order, int mine_zero_bits_count, boolean loop_mode) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        BigInteger salt = new BigInteger("0");
        byte[] hash = null;
        int order_counter = mine_hash_order;
        while (true) {
            salt = salt.add(new BigInteger("1"));
            BCrypt B = new BCrypt();
            String loop_data = "";
            if (loop_mode && hash != null) {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(hash);
                loop_data = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
            }
            hash = B.crypt_raw((data+loop_data).getBytes("UTF-8"), big_int_to_bytes(salt, 16), bcrypt_cost);
            if (is_zero_bits(hash, mine_zero_bits_count)) {
                order_counter--;
                if (order_counter <= 0) {
                    MineResult result = new MineResult();
                    result.hash = Integer.toHexString(bcrypt_cost) + "$" + Integer.toHexString(mine_hash_order) + "$" + Integer.toHexString(mine_zero_bits_count) + "$" + Base64.encodeToString(hash, Base64.NO_WRAP);
                    result.salt = salt;
                    // Log.d("MINING", "Found for string = "+data);
                    Log.d("MINING", "Found hash = "+byteArrayToHex(hash));
                    Log.d("MINING", "Found salt = "+byteArrayToHex(big_int_to_bytes(salt, 16)));
                    return (result);
                }
            }
        }
    }

    // For DBG
    private static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    private static boolean is_zero_bits(byte[] hash, int bits_count) {
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

            if ((last & mask) != 0)
                return(false);
        }

        return(true);
    }

    // Используется бинарное представление числа типа Big-endian
    private static byte[] big_int_to_bytes(BigInteger big_int, int bytes_count) {
        byte[] array = big_int.toByteArray();
        byte[] tmp = new byte[bytes_count];
        Arrays.fill(tmp, (byte) 0);
        System.arraycopy(array, 0, tmp, (bytes_count - array.length), array.length);
        return(tmp);
    }
}
