package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos;

import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.Role;

import java.util.List;

public interface RoleDAO {

    List<Role> findAll();

    Role findById(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndNotId(String name, Long id);

    void insert(Role role);

    void update(Role role);

    void delete(Long id);
}
