package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos;

import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.Region;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

public interface RegionDAO {

    List<Region> listAllRegions() throws SQLException;

    void insertRegion(Region region) throws SQLException;

    void updateRegion(Region region) throws SQLException;

    void deleteRegion(Long region) throws SQLException;

    boolean existsRegionByCode(String code) throws SQLException;

    boolean existsRegionByCodeAndNotId(String code, Long id) throws SQLException;

    Region getRegionById(Long id) throws SQLException;
}
