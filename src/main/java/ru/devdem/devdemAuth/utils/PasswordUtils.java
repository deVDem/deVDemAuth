package ru.devdem.devdemAuth.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordUtils {

    private static final int SALT_LENGTH = 16; // 16 байт
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // bits
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Генерация соли
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Хеширование пароля с солью
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null || salt.isBlank()) {
            throw new IllegalArgumentException("Password and salt must not be empty");
        }
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    Base64.getDecoder().decode(salt),
                    ITERATIONS,
                    KEY_LENGTH
            );
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    // Проверка пароля
    public static boolean verifyPassword(String password, String salt, String expectedHash) {
        if (password == null || salt == null || expectedHash == null) {
            return false;
        }

        try {
            byte[] actualHash = Base64.getDecoder().decode(hashPassword(password, salt));
            byte[] expectedHashBytes = Base64.getDecoder().decode(expectedHash);
            return MessageDigest.isEqual(actualHash, expectedHashBytes);
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
