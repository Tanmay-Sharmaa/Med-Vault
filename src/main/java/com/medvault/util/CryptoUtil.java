package com.medvault.util;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.GCMParameterSpec;  
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

@Component
public class CryptoUtil {

    private final byte[] keyBytes;
    private static final String ALG = "AES";
    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LEN = 12;

    public CryptoUtil(@Value("${medvault.crypto.key}") String keyHex) {
        this.keyBytes = hexToBytes(keyHex);
        if (this.keyBytes.length != 32) {
            throw new IllegalArgumentException("Key must be 32 bytes (256-bit) in hex (64 hex chars)");
        }
    }

    public void encrypt(InputStream in, OutputStream out) throws Exception {
        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);

        // write IV in plaintext at start
        out.write(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORM);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, ALG), new GCMParameterSpec(GCM_TAG_BITS, iv));

        try (CipherOutputStream cout = new CipherOutputStream(out, cipher)) {
            in.transferTo(cout);
        }
    }

    public void decrypt(InputStream in, OutputStream out) throws Exception {
        byte[] iv = in.readNBytes(IV_LEN);
        if (iv.length != IV_LEN) {
            throw new IllegalStateException("Invalid ciphertext: missing IV");
        }

        Cipher cipher = Cipher.getInstance(TRANSFORM);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, ALG), new GCMParameterSpec(GCM_TAG_BITS, iv));

        try (CipherInputStream cin = new CipherInputStream(in, cipher)) {
            cin.transferTo(out);
        }
    }

    private static byte[] hexToBytes(String s) {
        int len = s.length();
        if (len % 2 != 0) throw new IllegalArgumentException("Hex length must be even");
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
