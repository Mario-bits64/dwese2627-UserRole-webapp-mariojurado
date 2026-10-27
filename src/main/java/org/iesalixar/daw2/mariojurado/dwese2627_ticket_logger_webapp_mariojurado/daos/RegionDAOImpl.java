package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos;

import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.Region;

import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Repository
public class RegionDAOImpl implements RegionDAO {

    private static final Logger logger = LoggerFactory.getLogger(RegionDAOImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public RegionDAOImpl(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Lista todas las regiones de la base de datos.
     * @return Lista de regiones
     * @throws SQLException
     */
    public List<Region> listAllRegions() throws SQLException {
        logger.info("Listing all regions from the database");
        String sql = "SELECT * FROM regions";
        List<Region> regions = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Region.class));
        logger.info("Retrieved {} regions from the database." + regions.size());
        return regions;
    }

    /**
     * Recupera una región por su ID.
     * @param id ID de la región a recuperar
     * @return Región encontrada o null si no existe
     */
    @Override
    public Region getRegionById(Long id) {
        logger.info("Retrieving region by id: {}" + id);
        String sql = "SELECT * FROM regions WHERE id = ?";
        try {
            Region region = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Region.class), id);
            logger.info("Region retrieved: {} - {}", region.getCode(), region.getName());
            return region;
        } catch (Exception e) {
            logger.info("No region found with id: {}", id);
            return null;
        }
    }


    public void insertRegion(Region region) throws SQLException {
        logger.info("Inserting region with code: {} and name: {}", region.getCode(), region.getName());
        String sql = "INSERT INTO regions (code, name) VALUES (?, ?)";
        int rowsAffected = jdbcTemplate.update(sql, region.getCode(), region.getName());
        logger.info("Inserted region. Rows affected: {}" , rowsAffected);
    }

    /**
     * Verifica si una región con el código especificado ya existe en la base de datos,
     * ignorando mayúsculas.
     *
     * @param code el código de la región a verificar.
     * @return true si una región con el código ya existe, false de lo contrario.
     * @throws SQLException si ocurre un error en la consulta SQL.
     */
    @Override
    public boolean existsRegionByCode(String code) throws SQLException {
        logger.info("Checking if region with code: {} exists" , code);
        String sql = "SELECT COUNT(*) FROM regions WHERE UPPER(code) = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code.toUpperCase());
        boolean exists = count != null && count > 0;
        logger.info("Region with code: {} exists: {}");
        return exists;
    }

    @Override
    public boolean existsRegionByCodeAndNotId(String code, Long id) throws SQLException {
        logger.info("Checking if region with code: {} exists excluding id: {}", code, id);
        String sql = "SELECT COUNT(*) FROM regions WHERE UPPER(code) = ? AND id <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code.toUpperCase(), id);
        boolean exists = count != null && count > 0;
        return exists;
    }

    /**
     * Actualiza una región existente en la base de datos.
     * @param region Región a actualizar
     * @throws SQLException
     */
    public void updateRegion(Region region) throws SQLException {
        logger.info("Updating region with id: {}" , region.getId());
        String sql = "UPDATE regions SET code = ?, name = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, region.getCode(), region.getName(), region.getId());
        logger.info("Updated region. Rows affected: {}" , rowsAffected);
    }

    @Override
    public void deleteRegion(Long id) throws SQLException {
        logger.info("Deleting region with id: {}" , id);
        String sql = "DELETE FROM regions WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info("Deleted region. Rows affected: {}" , rowsAffected);
    }
}