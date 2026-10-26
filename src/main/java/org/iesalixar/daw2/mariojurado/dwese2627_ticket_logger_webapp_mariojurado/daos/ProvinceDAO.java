package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos;

import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.Province;

import java.util.List;

public interface ProvinceDAO {
    List<Province> listAllProvinces();
    void insertProvince(Province province);
    void updateProvince(Province province);
    boolean existsProvinceByCode(String code);
    Province getProvinceById(Long id);
    boolean existsProvinceByCodeAndNotId(String code, Long id);
    void deleteProvince(Long id);
}
