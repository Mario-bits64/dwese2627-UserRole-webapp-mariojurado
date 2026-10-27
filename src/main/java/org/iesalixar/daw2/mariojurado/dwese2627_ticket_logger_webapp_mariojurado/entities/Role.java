package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Role {
    private Long id;

    @NotBlank(message = "{msg.role.name.required}")
    @Size(max = 50, message = "{msg.role.name.size}")
    private String name;
}
