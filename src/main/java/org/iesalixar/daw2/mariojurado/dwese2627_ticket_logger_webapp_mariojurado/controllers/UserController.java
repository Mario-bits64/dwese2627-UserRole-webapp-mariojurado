package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos.RoleDAO;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.daos.UserDAO;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.Role;
import org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.entities.User;
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

import java.time.LocalDateTime;
import java.util.Locale;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final MessageSource messageSource;

    public UserController(UserDAO userDAO, RoleDAO roleDAO, MessageSource messageSource) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userDAO.findAll());
        return "views/user/user-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("availableRoles", roleDAO.findAll());
        return "views/user/user-form";
    }

    @GetMapping("/edit")
    public String editForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttributes,
                           Locale locale) {
        User user = userDAO.findById(id);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user.notFound", null, locale));
            return "redirect:/users";
        }
        model.addAttribute("user", user);
        model.addAttribute("availableRoles", roleDAO.findAll());
        return "views/user/user-form";
    }

    @PostMapping("/insert")
    public String insert(@Valid @ModelAttribute("user") User user, BindingResult result,
                         @RequestParam(name = "password", required = false) String password,
                         Model model, RedirectAttributes redirectAttributes, Locale locale) {
        if (user.getRole() == null) {
            user.setRole(new Role());
        }
        if (userDAO.existsByUsername(user.getUsername() == null ? "" : user.getUsername().trim())) {
            result.rejectValue("username", "msg.user.username.duplicate");
        }
        Role selectedRole = user.getRole() == null || user.getRole().getId() == null
                ? null : roleDAO.findById(user.getRole().getId());
        if (selectedRole == null) {
            result.rejectValue("role.id", "msg.user.role.required");
        } else {
            user.setRole(selectedRole);
        }
        if (user.getLastPasswordChange() == null && !result.hasFieldErrors("lastPasswordChange")) {
            result.rejectValue("lastPasswordChange", "msg.user.lastPasswordChange.required");
        }
        boolean hasPasswordError = password == null || password.isBlank();
        if (hasPasswordError) {
            model.addAttribute("passwordError",
                    messageSource.getMessage("msg.user.password.required", null, locale));
        }
        if (result.hasErrors() || hasPasswordError) {
            user.setUsername(user.getUsername() == null ? null : user.getUsername().trim());
            model.addAttribute("availableRoles", roleDAO.findAll());
            return "views/user/user-form";
        }

        user.setUsername(user.getUsername().trim());
        user.setPassword(password);
        LocalDateTime passwordChangedAt = user.getLastPasswordChange();
        user.setPasswordExpiresAt(passwordChangedAt.plusMonths(3));
        if (user.getFailedLoginAttempts() == null) {
            user.setFailedLoginAttempts(0);
        }
        user.setActive(Boolean.TRUE.equals(user.getActive()));
        user.setAccountNonLocked(Boolean.TRUE.equals(user.getAccountNonLocked()));
        user.setEmailVerified(Boolean.TRUE.equals(user.getEmailVerified()));
        user.setMustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()));
        userDAO.insert(user);
        redirectAttributes.addFlashAttribute("successMessage",
                messageSource.getMessage("msg.user.created", null, locale));
        return "redirect:/users";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("user") User user, BindingResult result,
                         @RequestParam(name = "password", required = false) String password,
                         Model model, RedirectAttributes redirectAttributes, Locale locale) {
        if (user.getRole() == null) {
            user.setRole(new Role());
        }
        User current = userDAO.findById(user.getId());
        if (current == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user.notFound", null, locale));
            return "redirect:/users";
        }

        String username = user.getUsername() == null ? "" : user.getUsername().trim();
        if (userDAO.existsByUsernameAndNotId(username, user.getId())) {
            result.rejectValue("username", "msg.user.username.duplicate");
        }
        Role selectedRole = user.getRole() == null || user.getRole().getId() == null
                ? null : roleDAO.findById(user.getRole().getId());
        if (selectedRole == null) {
            result.rejectValue("role.id", "msg.user.role.required");
        } else {
            user.setRole(selectedRole);
        }
        if (result.hasErrors()) {
            user.setUsername(username);
            model.addAttribute("availableRoles", roleDAO.findAll());
            return "views/user/user-form";
        }

        user.setUsername(username);
        boolean passwordChanged = password != null && !password.isBlank();
        user.setPassword(passwordChanged ? password : current.getPassword());
        LocalDateTime passwordChangedAt = passwordChanged ? LocalDateTime.now() : current.getLastPasswordChange();
        user.setLastPasswordChange(passwordChangedAt);
        user.setPasswordExpiresAt(passwordChanged
                ? passwordChangedAt.plusMonths(3) : current.getPasswordExpiresAt());
        user.setFailedLoginAttempts(current.getFailedLoginAttempts());
        user.setActive(Boolean.TRUE.equals(user.getActive()));
        user.setAccountNonLocked(Boolean.TRUE.equals(user.getAccountNonLocked()));
        user.setEmailVerified(Boolean.TRUE.equals(user.getEmailVerified()));
        user.setMustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()));
        userDAO.update(user);
        redirectAttributes.addFlashAttribute("successMessage",
                messageSource.getMessage("msg.user.updated", null, locale));
        return "redirect:/users";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes redirectAttributes, Locale locale) {
        userDAO.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                messageSource.getMessage("msg.user.deleted", null, locale));
        return "redirect:/users";
    }
}
