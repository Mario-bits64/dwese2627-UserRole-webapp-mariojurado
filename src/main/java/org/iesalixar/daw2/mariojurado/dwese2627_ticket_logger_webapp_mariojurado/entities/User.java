
package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

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

    private Long id;

    @NotBlank(message = "{msg.user.username.required}")
    @Size(min = 3, max = 40, message = "{msg.user.username.size}")
    private String username;

    private String password;
    private Boolean active = true;
    private Boolean accountNonLocked = true;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime lastPasswordChange;
    private LocalDateTime passwordExpiresAt;
    @Min(value = 0, message = "{msg.user.failedLoginAttempts.min}")
    private Integer failedLoginAttempts = 0;
    private Boolean emailVerified = false;
    private Boolean mustChangePassword = false;
    private Role role = new Role();
}
