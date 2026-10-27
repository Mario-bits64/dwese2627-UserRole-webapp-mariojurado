package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos;

import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.User;

import java.util.List;

public interface UserDAO {

    List<User> findAll();

    User findById(Long id);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndNotId(String username, Long id);

    void insert(User user, List<Long> roleIds);

    void update(User user, List<Long> roleIds);

    void delete(Long id);
}
