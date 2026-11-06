package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos;

import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.Role;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UserDAOImpl implements UserDAO {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setActive(rs.getBoolean("active"));
        user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
        Timestamp lastPasswordChange = rs.getTimestamp("last_password_change");
        Timestamp passwordExpiresAt = rs.getTimestamp("password_expires_at");
        LocalDateTime lastPasswordChangeValue = lastPasswordChange == null
                ? null : lastPasswordChange.toLocalDateTime();
        LocalDateTime passwordExpiresAtValue = passwordExpiresAt == null
                ? null : passwordExpiresAt.toLocalDateTime();
        user.setLastPasswordChange(lastPasswordChangeValue);
        user.setPasswordExpiresAt(passwordExpiresAtValue == null && lastPasswordChangeValue != null
                ? lastPasswordChangeValue.plusMonths(3) : passwordExpiresAtValue);
        user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        user.setEmailVerified(rs.getBoolean("email_verified"));
        user.setMustChangePassword(rs.getBoolean("must_change_password"));
        Long roleId = rs.getObject("role_id", Long.class);
        if (roleId == null) {
            user.setRole(new Role());
        } else {
            user.setRole(new Role(roleId, rs.getString("role_name")));
        }
        return user;
    };

    public UserDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT u.id, u.username, u.password_hash, u.active, "
                + "u.account_non_locked, u.last_password_change, u.password_expires_at, "
                + "u.failed_login_attempts, u.email_verified, u.must_change_password, "
                + "u.role_id, r.name AS role_name FROM users u LEFT JOIN roles r ON r.id = u.role_id "
                + "ORDER BY u.username";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public User findById(Long id) {
        String sql = "SELECT u.id, u.username, u.password_hash, u.active, "
                + "u.account_non_locked, u.last_password_change, u.password_expires_at, "
                + "u.failed_login_attempts, u.email_verified, u.must_change_password, "
                + "u.role_id, r.name AS role_name FROM users u LEFT JOIN roles r ON r.id = u.role_id "
                + "WHERE u.id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)", Integer.class, username);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByUsernameAndNotId(String username, Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?) AND id <> ?",
                Integer.class, username, id);
        return count != null && count > 0;
    }

    @Override
    public void insert(User user) {
        String sql = "INSERT INTO users (username, password_hash, active, account_non_locked, last_password_change, "
                + "password_expires_at, failed_login_attempts, email_verified, must_change_password, role_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getUsername(), user.getPasswordHash(),
                Boolean.TRUE.equals(user.getActive()), Boolean.TRUE.equals(user.getAccountNonLocked()),
                user.getLastPasswordChange(), user.getPasswordExpiresAt(), user.getFailedLoginAttempts(),
                Boolean.TRUE.equals(user.getEmailVerified()), Boolean.TRUE.equals(user.getMustChangePassword()),
                user.getRole() == null ? null : user.getRole().getId());
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, active = ?, account_non_locked = ?, "
                + "last_password_change = ?, password_expires_at = ?, failed_login_attempts = ?, "
                + "email_verified = ?, must_change_password = ?, role_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getUsername(), user.getPasswordHash(),
                Boolean.TRUE.equals(user.getActive()), Boolean.TRUE.equals(user.getAccountNonLocked()),
                user.getLastPasswordChange(), user.getPasswordExpiresAt(), user.getFailedLoginAttempts(),
                Boolean.TRUE.equals(user.getEmailVerified()), Boolean.TRUE.equals(user.getMustChangePassword()),
                user.getRole() == null ? null : user.getRole().getId(), user.getId());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }
}
