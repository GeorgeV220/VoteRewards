package com.georgev22.voterewards.utilities;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UtilsX {

    /**
     * Generates a deterministic UUID from a given seed using the SHA-256 hash function.
     *
     * @param seed the input seed used to generate the UUID
     * @return a UUID generated from the seed
     */
    @Contract("_ -> new")
    public static @NotNull UUID generateUUID(@NotNull String seed) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(seed.getBytes(StandardCharsets.UTF_8));
            byte[] hash = md.digest();
            long msb = 0;
            long lsb = 0;
            for (int i = 0; i < 8; i++)
                msb = (msb << 8) | (hash[i] & 0xff);
            for (int i = 8; i < 16; i++)
                lsb = (lsb << 8) | (hash[i] & 0xff);
            return new UUID(msb, lsb);
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }
    }

}
