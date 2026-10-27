package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos;

import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoleDAOImpl implements RoleDAO {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Role> rowMapper = (rs, rowNum) -> new Role(rs.getLong("id"), rs.getString("name"));

    public RoleDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Role> findAll() {
        return jdbcTemplate.query("SELECT id, name FROM roles ORDER BY name", rowMapper);
    }

    @Override
    public Role findById(Long id) {
        List<Role> roles = jdbcTemplate.query("SELECT id, name FROM roles WHERE id = ?", rowMapper, id);
        return roles.isEmpty() ? null : roles.getFirst();
    }

    @Override
    public boolean existsByName(String name) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM roles WHERE LOWER(name) = LOWER(?)", Integer.class, name);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByNameAndNotId(String name, Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM roles WHERE LOWER(name) = LOWER(?) AND id <> ?", Integer.class, name, id);
        return count != null && count > 0;
    }

    @Override
    public void insert(Role role) {
        jdbcTemplate.update("INSERT INTO roles (name) VALUES (?)", role.getName().trim());
    }

    @Override
    public void update(Role role) {
        jdbcTemplate.update("UPDATE roles SET name = ? WHERE id = ?", role.getName().trim(), role.getId());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM roles WHERE id = ?", id);
    }
}
