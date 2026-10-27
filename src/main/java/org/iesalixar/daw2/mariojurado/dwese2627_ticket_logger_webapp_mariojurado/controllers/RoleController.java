package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos.RoleDAO;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.Role;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/roles")
public class RoleController {

    private final RoleDAO roleDAO;
    private final MessageSource messageSource;

    public RoleController(RoleDAO roleDAO, MessageSource messageSource) {
        this.roleDAO = roleDAO;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roles", roleDAO.findAll());
        return "views/role/role-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("role", new Role());
        return "views/role/role-form";
    }

    @GetMapping("/edit")
    public String editForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttributes,
                           Locale locale) {
        Role role = roleDAO.findById(id);
        if (role == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role.notFound", null, locale));
            return "redirect:/roles";
        }
        model.addAttribute("role", role);
        return "views/role/role-form";
    }

    @PostMapping("/insert")
    public String insert(@Valid @ModelAttribute("role") Role role, BindingResult result,
                         RedirectAttributes redirectAttributes, Locale locale) {
        if (roleDAO.existsByName(role.getName() == null ? "" : role.getName().trim())) {
            result.rejectValue("name", "msg.role.name.duplicate");
        }
        if (result.hasErrors()) {
            return "views/role/role-form";
        }
        role.setName(role.getName().trim());
        roleDAO.insert(role);
        redirectAttributes.addFlashAttribute("successMessage",
                messageSource.getMessage("msg.role.created", null, locale));
        return "redirect:/roles";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("role") Role role, BindingResult result,
                         RedirectAttributes redirectAttributes, Locale locale) {
        if (roleDAO.findById(role.getId()) == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role.notFound", null, locale));
            return "redirect:/roles";
        }
        if (roleDAO.existsByNameAndNotId(role.getName() == null ? "" : role.getName().trim(), role.getId())) {
            result.rejectValue("name", "msg.role.name.duplicate");
        }
        if (result.hasErrors()) {
            return "views/role/role-form";
        }
        role.setName(role.getName().trim());
        roleDAO.update(role);
        redirectAttributes.addFlashAttribute("successMessage",
                messageSource.getMessage("msg.role.updated", null, locale));
        return "redirect:/roles";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes redirectAttributes, Locale locale) {
        roleDAO.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                messageSource.getMessage("msg.role.deleted", null, locale));
        return "redirect:/roles";
    }
}
