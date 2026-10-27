package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.services;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/** Creates salted PBKDF2 password hashes for users managed by this application. */
@Component
public class PasswordHasher {

    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
            return "pbkdf2-sha256$" + ITERATIONS + "$"
                    + Base64.getEncoder().withoutPadding().encodeToString(salt) + "$"
                    + Base64.getEncoder().withoutPadding().encodeToString(hash);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se pudo generar el hash de la contraseña.", exception);
        } finally {
            spec.clearPassword();
        }
    }
}
