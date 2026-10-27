package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos;

import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.Role;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Repository
public class UserDAOImpl implements UserDAO {

    private static final String USER_COLUMNS = "id, username, password_hash, active, account_non_locked, "
            + "last_password_change, password_expires_at, failed_login_attempts, email_verified, must_change_password";

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setActive(rs.getBoolean("active"));
        user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
        LocalDateTime lastPasswordChange = toLocalDateTime(rs.getTimestamp("last_password_change"));
        LocalDateTime passwordExpiresAt = toLocalDateTime(rs.getTimestamp("password_expires_at"));
        user.setLastPasswordChange(lastPasswordChange);
        user.setPasswordExpiresAt(passwordExpiresAt == null && lastPasswordChange != null
                ? lastPasswordChange.plusMonths(3) : passwordExpiresAt);
        user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        user.setEmailVerified(rs.getBoolean("email_verified"));
        user.setMustChangePassword(rs.getBoolean("must_change_password"));
        return user;
    };

    public UserDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {
        List<User> users = jdbcTemplate.query("SELECT " + USER_COLUMNS + " FROM users ORDER BY username", userRowMapper);
        users.forEach(user -> user.setRoles(findRoles(user.getId())));
        return users;
    }

    @Override
    public User findById(Long id) {
        List<User> users = jdbcTemplate.query("SELECT " + USER_COLUMNS + " FROM users WHERE id = ?", userRowMapper, id);
        if (users.isEmpty()) {
            return null;
        }
        User user = users.getFirst();
        user.setRoles(findRoles(id));
        return user;
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
    @Transactional
    public void insert(User user, List<Long> roleIds) {
        String sql = "INSERT INTO users (username, password_hash, active, account_non_locked, last_password_change, "
                + "password_expires_at, failed_login_attempts, email_verified, must_change_password) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            bindUser(statement, user, false);
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("La base de datos no devolvió el ID del usuario creado.");
        }
        user.setId(key.longValue());
        replaceRoles(user.getId(), roleIds);
    }

    @Override
    @Transactional
    public void update(User user, List<Long> roleIds) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, active = ?, account_non_locked = ?, "
                + "last_password_change = ?, password_expires_at = ?, failed_login_attempts = ?, "
                + "email_verified = ?, must_change_password = ? WHERE id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            bindUser(statement, user, true);
            return statement;
        });
        replaceRoles(user.getId(), roleIds);
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    private List<Role> findRoles(Long userId) {
        return jdbcTemplate.query("SELECT r.id, r.name FROM roles r "
                        + "INNER JOIN user_role ur ON ur.role_id = r.id WHERE ur.user_id = ? ORDER BY r.name",
                (rs, rowNum) -> new Role(rs.getLong("id"), rs.getString("name")), userId);
    }

    private void replaceRoles(Long userId, List<Long> roleIds) {
        jdbcTemplate.update("DELETE FROM user_role WHERE user_id = ?", userId);
        Set<Long> uniqueIds = new LinkedHashSet<>(roleIds == null ? List.of() : roleIds);
        for (Long roleId : uniqueIds) {
            if (roleId != null) {
                jdbcTemplate.update("INSERT INTO user_role (user_id, role_id) VALUES (?, ?)", userId, roleId);
            }
        }
    }

    private void bindUser(PreparedStatement statement, User user, boolean includeId) throws java.sql.SQLException {
        statement.setString(1, user.getUsername());
        statement.setString(2, user.getPasswordHash());
        statement.setBoolean(3, Boolean.TRUE.equals(user.getActive()));
        statement.setBoolean(4, Boolean.TRUE.equals(user.getAccountNonLocked()));
        setTimestamp(statement, 5, user.getLastPasswordChange());
        setTimestamp(statement, 6, user.getPasswordExpiresAt());
        statement.setInt(7, user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts());
        statement.setBoolean(8, Boolean.TRUE.equals(user.getEmailVerified()));
        statement.setBoolean(9, Boolean.TRUE.equals(user.getMustChangePassword()));
        if (includeId) {
            statement.setLong(10, user.getId());
        }
    }

    private static void setTimestamp(PreparedStatement statement, int index, LocalDateTime value)
            throws java.sql.SQLException {
        if (value == null) {
            statement.setTimestamp(index, null);
        } else {
            statement.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
