
package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un usuario de la tabla users.
 *
 * @author Mario Jurado
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;

    @NotBlank(message = "{msg.user.username.required}")
    @Size(min = 3, max = 40, message = "{msg.user.username.size}")
    private String username;

    private String passwordHash;
    private Boolean active = true;
    private Boolean accountNonLocked = true;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime lastPasswordChange;
    private LocalDateTime passwordExpiresAt;
    @Min(value = 0, message = "{msg.user.failedLoginAttempts.min}")
    private Integer failedLoginAttempts = 0;
    private Boolean emailVerified = false;
    private Boolean mustChangePassword = false;
    private List<Role> roles = new ArrayList<>();

    public String getLastPasswordChangeDisplay() {
        return formatDate(lastPasswordChange);
    }

    public String getPasswordExpiresAtDisplay() {
        return formatDate(passwordExpiresAt);
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "—" : value.format(DISPLAY_DATE_FORMAT);
    }
}
