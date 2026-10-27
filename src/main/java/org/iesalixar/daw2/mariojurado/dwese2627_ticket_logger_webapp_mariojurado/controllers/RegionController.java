package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos.RegionDAO;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos.RegionDAOImpl;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/regions")
public class RegionController {

    private static final Logger logger = LoggerFactory.getLogger(RegionDAOImpl.class);

    @Autowired
    private RegionDAO regionDAO;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String listRegions(Model model) {
        logger.info("Solicitando la lista de todas las regiones...");
        List<Region> listRegions = null;
        try {
            listRegions = regionDAO.listAllRegions();
            logger.info("Se han cargado {} regiones.", listRegions.size());
        } catch (SQLException e) {
            logger.error("Error al listar las regiones: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al listar las regiones.");
        }

        model.addAttribute("listRegions", listRegions);
        return "views/region/region-list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nueva region.");
        model.addAttribute("region", new Region());
        return "views/region/region-form";
    }

    /**
     * Inserta una nueva región en la base de datos.
     *
     * @param region              Objeto que contiene los datos del formulario.
     * @param redirectAttributes  Atributos para mensajes flash de redirección.
     * @param result              Resultado de la validación del formulario.
     * @param locale              Objeto que representa la configuración regional actual del usuario,
     *                            utilizado para obtener los mensajes traducidos desde {@code messageSource}.
     * @return Redirección a la lista de regiones.
     */
    @PostMapping("/insert")
    public String insertRegion(@Valid @ModelAttribute("region") Region region, BindingResult result, RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Insertando nueva región con código {}", region.getCode());
        try {
            if (result.hasErrors()) {
                return "views/region/region-form";  // Devuelve el formulario para mostrar los errores de validación
            }
            if (regionDAO.existsRegionByCode(region.getCode())) {
                logger.warn("El código de la región {} ya existe.", region.getCode());
                String errorMessage = messageSource.getMessage("msg.region-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/regions/new";
            }
            regionDAO.insertRegion(region);
            logger.info("Región {} insertada con éxito.", region.getCode());
        } catch (Exception e) {
            logger.error("Error al insertar la región {}: {}", region.getCode(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.region-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/regions"; // Redirigir a la lista de regiones
    }




    /**
     * Actualiza una región existente en la base de datos.
     *
     * @param region              Objeto que contiene los datos del formulario.
     * @param redirectAttributes  Atributos para mensajes flash de redirección
     * @param result              Resultado de la validación del formulario.
     * @param locale              Objeto que representa la configuración regional actual del usuario,
     *                            utilizado para obtener los mensajes traducidos desde {@code messageSource}.
     * @return Redirección a la lista de regiones.
     */
    @PostMapping("/update")
    public String updateRegion(@Valid @ModelAttribute("region") Region region,  BindingResult result, RedirectAttributes redirectAttributes,  Locale locale) {
        logger.info("Actualizando región con ID {}", region.getId());
        try {
            if (result.hasErrors()) {
                return "views/region/region-form";  // Devuelve el formulario para mostrar los errores de validación
            }
            if (regionDAO.existsRegionByCodeAndNotId(region.getCode(), region.getId())) {
                logger.warn("El código de la región {} ya existe para otra región.", region.getCode());
                String errorMessage = messageSource.getMessage("msg.region-controller.update.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/regions/edit?id=" + region.getId();
            }
            regionDAO.updateRegion(region);
            logger.info("Región con ID {} actualizada con éxito.", region.getId());
        } catch (Exception e) {
            logger.error("Error al actualizar la región con ID {}: {}", region.getId(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.region-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/regions"; // Redirigir a la lista de regiones
    }


    @PostMapping("/delete")
    public String deleteRegion(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        logger.info("Eliminando region con ID {}", id);
        try {
            regionDAO.deleteRegion(id);
            logger.info("Region con ID {} eliminado con exito", id);
        } catch (SQLException e) {
            logger.error("Error al eliminar la region con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la region.");
        }
        return "redirect:/regions";
    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model){
        logger.info("Mostrando formulario de edicion para la region con ID {}", id);
        Region region = null;
        try {
            region = regionDAO.getRegionById(id);
            if (region == null){
                logger.warn("No se encontro la region con ID {}", id);
            }
        } catch (SQLException e){
            logger.error("Error al obtener la region con ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Error al obtener la region");
        }
        model.addAttribute("region", region);
        return "views/region/region-form";
    }
}
